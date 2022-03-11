package com.beat.view.audioPlayer

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.lifecycle.*
import com.beat.R
import com.beat.core.data.model.PlaylistDetailsWithTrackResponse
import com.beat.core.data.model.RadioListResponse
import com.beat.core.data.model.Resource
import com.beat.core.data.rest.Repository
import com.beat.core.data.storage.PreferenceManager
import com.beat.data.model.Radio
import com.beat.data.model.Track
import com.beat.util.*
import com.example.android.uamp.common.*
import com.example.android.uamp.media.extensions.*
import com.google.gson.Gson
import kotlinx.coroutines.launch
import javax.inject.Inject

class RadioPlayerViewModel @Inject constructor(
    context: Context,
    private val preferenceManager: PreferenceManager,
    private val repository: Repository
) : ViewModel() {

    private val serviceConnection: MusicServiceConnection = provideMusicServiceConnection(
        context,
        preferenceManager.getAuth(),
        getAudioQuality()
    )

    private val mediaButtonRes = MutableLiveData<Int>()

    private val handler = Handler(Looper.getMainLooper())

    private val nowPlayingMediaItem = MutableLiveData<Track>()

    private val mediaPosition = MutableLiveData<Int>().apply {
        postValue(0)
    }

    private var playbackState: PlaybackStateCompat = EMPTY_PLAYBACK_STATE

    private var updatePosition = true
    private val POSITION_UPDATE_INTERVAL_MILLIS = 100L

    private val mediaMetadataObserver = Observer<MediaMetadataCompat> {
        updateState(playbackState, it)
    }

    private val playbackStateObserver = Observer<PlaybackStateCompat> {
        playbackState = it ?: EMPTY_PLAYBACK_STATE
        val metadata = serviceConnection.nowPlaying.value ?: NOTHING_PLAYING
        updateState(playbackState, metadata)
    }

    private val musicServiceConnection = serviceConnection.also {
        it.playbackState.observeForever(playbackStateObserver)
        it.nowPlaying.observeForever(mediaMetadataObserver)
        checkPlaybackPosition()
    }

    private var radioListResponse = MutableLiveData<Resource<RadioListResponse>>()
    private val playlistDetailsWithTrackResponse =
        MutableLiveData<Resource<PlaylistDetailsWithTrackResponse>>()

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

    fun radioListRequest() {
        viewModelScope.launch {
            radioListResponse.value = Resource.loading(null)
            radioListResponse.value = repository.getRadioList(
                "6"
            )
        }
    }

    fun getAllTrackOfPlaylist(playlistId: String) {
        viewModelScope.launch {
            playlistDetailsWithTrackResponse.value = Resource.loading(null)
            playlistDetailsWithTrackResponse.value =
                repository.getPlaylistDetailsWithTrack(playlistId)
        }
    }

    fun fetchCurrentPlayingItem() {
        val nowPlaying = musicServiceConnection.nowPlaying.value
        if (!nowPlaying?.id.isNullOrEmpty()) {
            updateState(playbackState, nowPlaying!!)
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
                        Logger.d(
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

    fun playRadio(
        list: ArrayList<Track>,
        track: Track,
        playlistId: String,
        playlistTitle: String,
        playlistImage: String
    ) {
        musicServiceConnection.addRadioMediaToPlayer(
            isPlayerInitialed,
            radioTracksToMusicJson(list, playlistId, playlistTitle, playlistImage),
            true,
            track.trackId
        )
    }

    fun getRadioListResponse(): LiveData<Resource<RadioListResponse>> {
        return radioListResponse
    }

    fun getPlaylistDetailsWithTrackResponse(): LiveData<Resource<PlaylistDetailsWithTrackResponse>> {
        return playlistDetailsWithTrackResponse
    }

    fun getMediaPosition(): LiveData<Int> {
        return mediaPosition
    }

    fun getMediaButtonRes(): LiveData<Int> {
        return mediaButtonRes
    }

    fun getNowPlayingMediaItem(): LiveData<Track> {
        return nowPlayingMediaItem
    }

    fun getRadioListFromResponse(
        currentRadioId: String,
        radioListResponse: RadioListResponse?
    ): ArrayList<Radio> {
        val list: ArrayList<Radio> = ArrayList()
        radioListResponse?.group?.banners?.forEach { banner ->
            if (banner.link.playlist.id != currentRadioId) {
                list.add(
                    Radio(
                        banner.id,
                        banner.image.w50,
                        banner.link.playlist.id,
                        banner.link.playlist.title,
                        banner.link.playlist.trackCount,
                        banner.link.playlist.favourite
                    )
                )
            }
        }
        return list
    }

    fun getTrackListFromPlaylistDetailsResponse(playlistDetailsWithTrackResponse: PlaylistDetailsWithTrackResponse?): ArrayList<Track> {
        val list: ArrayList<Track> = ArrayList()
        playlistDetailsWithTrackResponse?.playlist?.tracks?.forEach { track ->
            list.add(
                Track(
                    track.id,
                    track.title,
                    track.release.cover.width,
                    track.duration,
                    track.release.id,
                    track.artist.id,
                    track.artist.name,
                    track.url,
                    track.favourite
                )
            )
        }
        return list
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

    fun getAudioQuality(): String {
        val quality = preferenceManager.getAudioQuality()
        return if (quality.isEmpty()) Constants.NORMAL else quality
    }

    override fun onCleared() {
        super.onCleared()
        updatePosition = false
        musicServiceConnection.playbackState.removeObserver(playbackStateObserver)
        musicServiceConnection.nowPlaying.removeObserver(mediaMetadataObserver)
    }

    private val TAG = "RadioPlayerViewModel"

}