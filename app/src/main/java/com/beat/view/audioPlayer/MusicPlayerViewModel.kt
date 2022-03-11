package com.beat.view.audioPlayer

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.support.v4.media.session.PlaybackStateCompat.*
import android.util.Log
import androidx.lifecycle.*
import com.beat.R
import com.beat.core.data.rest.Repository
import com.beat.core.data.storage.OfflineDownloadConnection
import com.beat.core.data.storage.PreferenceManager
import com.beat.data.model.Track
import com.beat.util.Constants
import com.beat.util.provideMusicServiceConnection
import com.beat.util.trackToMusicJson
import com.example.android.uamp.common.*
import com.example.android.uamp.media.extensions.*
import com.google.gson.Gson
import kotlinx.coroutines.launch
import javax.inject.Inject

class MusicPlayerViewModel @Inject constructor(
    context: Context,
    private val preferenceManager: PreferenceManager,
    private val repository: Repository,
    private val offlineDownloadConnection: OfflineDownloadConnection
) :
    ViewModel() {

    private val serviceConnection: MusicServiceConnection = provideMusicServiceConnection(
        context,
        preferenceManager.getAuth(),
        getAudioQuality()
    )

    private val handler = Handler(Looper.getMainLooper())

    private val mediaItems = MutableLiveData<List<Track>>()
    private val nowPlayingMediaItem = MutableLiveData<Track>()
    private val mediaButtonRes = MutableLiveData<Int>()
    private val repeatModeRes = MutableLiveData<Int>()
    private val shuffleModeTint = MutableLiveData<Int>()
    private val repeatModeTint = MutableLiveData<Int>()
    private val downloadProgress = MutableLiveData<Int>()
    private val networkError = Transformations.map(serviceConnection.networkFailure) { it }

    private val mediaPosition = MutableLiveData<Int>().apply {
        postValue(0)
    }

    private var playbackState: PlaybackStateCompat = EMPTY_PLAYBACK_STATE
    private var shuffleMode: Int = DEFAULT_SHUFFLE_MODE
    private var repeatMode: Int = DEFAULT_REPEAT_MODE

    private var updatePosition = true
    private val POSITION_UPDATE_INTERVAL_MILLIS = 100L

    private val subscriptionCallback = object : MediaBrowserCompat.SubscriptionCallback() {
        override fun onChildrenLoaded(
            parentId: String,
            children: List<MediaBrowserCompat.MediaItem>
        ) {
            val itemsList = children.map { child ->
                Gson().fromJson(
                    child.description.extras?.getString(MediaMetadataCompat.METADATA_KEY_DISPLAY_DESCRIPTION),
                    Track::class.java
                )

//                Track(
//                    child.mediaId!!,
//                    child.description.title.toString(),
//                    child.description.iconUri.toString(),
//                    0,
//                    "",
//                    "",
//                    subtitle.toString(),
//                    "",
//                    false
//                )
//                child.description.extras.getString(MediaMetadataCompat.METADATA_KEY_DISPLAY_DESCRIPTION)
            }
            mediaItems.value = itemsList
        }
    }

    private val mediaMetadataObserver = Observer<MediaMetadataCompat> {
        updateState(playbackState, it)
    }

    private val playbackStateObserver = Observer<PlaybackStateCompat> {
        playbackState = it ?: EMPTY_PLAYBACK_STATE
        val metadata = serviceConnection.nowPlaying.value ?: NOTHING_PLAYING
        updateState(playbackState, metadata)
    }

    private val shuffleModeObserver = Observer<Int> {
        shuffleMode = it ?: DEFAULT_SHUFFLE_MODE
        updateShuffleMode(shuffleMode)
    }

    private val repeatModeObserver = Observer<Int> {
        repeatMode = it ?: DEFAULT_REPEAT_MODE
        updateRepeatMode(repeatMode)
    }

    private val downloadProgressObserver = Observer<Int> {
        downloadProgress.postValue(it)
    }

    private val musicServiceConnection = serviceConnection.also {
        it.subscribe(parentId, subscriptionCallback)
        it.playbackState.observeForever(playbackStateObserver)
        it.nowPlaying.observeForever(mediaMetadataObserver)
        it.shuffleMode.observeForever(shuffleModeObserver)
        it.repeatMode.observeForever(repeatModeObserver)
        checkPlaybackPosition()
    }

    fun patchFavorite(id: String, type: String, favorite: Boolean, track: Track) {
        serviceConnection.setFavorite(trackToMusicJson(track), id)
        viewModelScope.launch {
            repository.patchFavorite(id, type, favorite)
        }
    }

    fun onPlay(mediaId: String) {
        val nowPlaying = musicServiceConnection.nowPlaying.value
        val transportControls = musicServiceConnection.transportControls

        val isPrepared = musicServiceConnection.playbackState.value?.isPrepared ?: false
        if (isPrepared && mediaId == nowPlaying?.id) {
            musicServiceConnection.playbackState.value?.let { playbackState ->
                when {
                    playbackState.isPlaying -> transportControls.pause()
                    playbackState.isPlayEnabled -> transportControls.play()
                    else -> {
                        Log.w(
                            TAG, "Playable item clicked but neither play nor pause are enabled!" +
                                    " (mediaId=$mediaId)"
                        )
                    }
                }
            }
        } else {
            transportControls.playFromMediaId(mediaId, null)
        }
    }

    fun onSeek(pos: Long) {
        musicServiceConnection.transportControls.seekTo(pos)
    }

    fun onPrevious() {
        musicServiceConnection.transportControls.skipToPrevious()
    }

    fun onNext() {
        musicServiceConnection.transportControls.skipToNext()
    }

    fun onShuffle() {
        musicServiceConnection.transportControls
            .setShuffleMode(if (shuffleMode == SHUFFLE_MODE_NONE) SHUFFLE_MODE_ALL else SHUFFLE_MODE_NONE)
    }

    fun onRepeat() {
        val mode = when (repeatMode) {
            REPEAT_MODE_ONE -> {
                REPEAT_MODE_ALL
            }
            REPEAT_MODE_ALL -> {
                REPEAT_MODE_NONE
            }
            REPEAT_MODE_NONE -> {
                REPEAT_MODE_ONE
            }
            else -> {
                REPEAT_MODE_NONE
            }
        }
        musicServiceConnection.transportControls.setRepeatMode(mode)
    }

    fun downloadTrack(track: Track) {
        val rawData = com.beat.core.data.storage.database.entity.Track(
            track.trackId,
            track.trackTitle,
            track.trackImage,
            track.duration,
            track.releaseId,
            "",
            track.artistId,
            track.artistName,
            track.url,
            track.favorite,
            false
        )
        offlineDownloadConnection.downloadTrack(rawData)
    }

    fun setDownloadObserver(
        it: Int,
        preTrackId: String,
        trackId: String,
        lifecycleOwner: LifecycleOwner
    ) {
        downloadProgress.postValue(it)
        offlineDownloadConnection.removeObserver(preTrackId, downloadProgressObserver)
        offlineDownloadConnection.getObserver(trackId, downloadProgressObserver, lifecycleOwner)
    }

    fun getNewDownloadAddObserver(
        observer: Observer<String>,
        lifecycleOwner: LifecycleOwner
    ) {
        offlineDownloadConnection.getNewIdAddObserver(observer, lifecycleOwner)
    }

    fun fetchMediaList() {
        val itemsList = musicServiceConnection.jsonSource.map { child ->
            Gson().fromJson(
                child.description.extras?.getString(MediaMetadataCompat.METADATA_KEY_DISPLAY_DESCRIPTION),
                Track::class.java
            )
        }
        mediaItems.value = itemsList
    }

    fun fetchCurrentPlayingItem() {
        val nowPlaying = musicServiceConnection.nowPlaying.value
        if (!nowPlaying?.id.isNullOrEmpty()) {
            updateState(playbackState, nowPlaying!!)
        }
    }

    private fun updateState(
        playbackState: PlaybackStateCompat,
        mediaMetadata: MediaMetadataCompat
    ) {

        // Only update media item once we have duration available
        if (mediaMetadata.duration != 0L && mediaMetadata.id != null) {
            val nowPlayingMetadata: Track =
                Gson().fromJson(
                    mediaMetadata.displayDescription,
                    Track::class.java
                )
//            val nowPlayingMetadata = Track(
//                mediaMetadata.id!!,
//                mediaMetadata.description.title.toString(),
//                mediaMetadata.description.iconUri.toString(),
//                mediaMetadata.duration.toInt(),
//                "",
//                "",
//                mediaMetadata.description.,
//                "",
//                false
//            )
            this.nowPlayingMediaItem.value = nowPlayingMetadata
        }

        // Update the media button resource ID
        mediaButtonRes.postValue(
            when (playbackState.isPlaying) {
                true -> R.drawable.ic_pause
                else -> R.drawable.ic_play
            }
        )
    }

    private fun updateShuffleMode(mode: Int) {
        shuffleModeTint.postValue(
            when (mode == SHUFFLE_MODE_ALL) {
                true -> R.color.colorPrimary
                else -> R.color.colorDarkGrey
            }
        )
    }

    private fun updateRepeatMode(mode: Int) {
        when (mode) {
            REPEAT_MODE_ONE -> {
                repeatModeRes.postValue(R.drawable.ic_repeat_one)
                repeatModeTint.postValue(R.color.colorPrimary)
            }
            REPEAT_MODE_ALL -> {
                repeatModeRes.postValue(R.drawable.ic_repeat)
                repeatModeTint.postValue(R.color.colorPrimary)
            }
            else -> {
                repeatModeRes.postValue(R.drawable.ic_repeat)
                repeatModeTint.postValue(R.color.colorDarkGrey)
            }
        }
    }

    /**
     * Internal function that recursively calls itself every [POSITION_UPDATE_INTERVAL_MILLIS] ms
     * to check the current playback position and updates the corresponding LiveData object when it
     * has changed.
     */
    private fun checkPlaybackPosition(): Boolean = handler.postDelayed({
        val currPosition = playbackState.currentPlayBackPosition
        if (mediaPosition.value != currPosition.toInt())
            mediaPosition.postValue(currPosition.toInt())
        if (updatePosition)
            checkPlaybackPosition()
    }, POSITION_UPDATE_INTERVAL_MILLIS)

    fun getMediaItem(): LiveData<List<Track>> {
        return mediaItems
    }

    fun getNowPlayingMediaItem(): LiveData<Track> {
        return nowPlayingMediaItem
    }

    fun getMediaButtonRes(): LiveData<Int> {
        return mediaButtonRes
    }

    fun getRepeatModeRes(): LiveData<Int> {
        return repeatModeRes
    }

    fun getsShuffleModeTint(): LiveData<Int> {
        return shuffleModeTint
    }

    fun getRepeatModeTint(): LiveData<Int> {
        return repeatModeTint
    }

    fun getDownloadProgress(): LiveData<Int> {
        return downloadProgress
    }

    fun getMediaPosition(): LiveData<Int> {
        return mediaPosition
    }

    fun getPlaybackState(): PlaybackStateCompat {
        return playbackState
    }

    fun setAudioQuality(quality: String) {
        serviceConnection.setAudioFormat(quality)
        preferenceManager.setAudioQuality(quality)
    }

    fun getAudioQuality(): String {
        val quality = preferenceManager.getAudioQuality()
        return if (quality.isEmpty()) Constants.NORMAL else quality
    }

    override fun onCleared() {
        super.onCleared()
        updatePosition = false
        musicServiceConnection.unsubscribe(parentId, subscriptionCallback)
        musicServiceConnection.playbackState.removeObserver(playbackStateObserver)
        musicServiceConnection.nowPlaying.removeObserver(mediaMetadataObserver)
        musicServiceConnection.shuffleMode.removeObserver(shuffleModeObserver)
        musicServiceConnection.repeatMode.removeObserver(repeatModeObserver)
    }

    private val TAG = "MusicPlayerViewModel"

}