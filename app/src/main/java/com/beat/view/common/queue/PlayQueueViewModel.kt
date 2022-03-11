package com.beat.view.common.queue

import android.content.Context
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.beat.core.data.storage.PreferenceManager
import com.beat.data.model.Track
import com.beat.util.Constants
import com.beat.util.provideMusicServiceConnection
import com.beat.util.tracksToMusicJson
import com.example.android.uamp.common.MusicServiceConnection
import com.example.android.uamp.common.NOTHING_PLAYING
import com.example.android.uamp.media.extensions.displayDescription
import com.example.android.uamp.media.extensions.duration
import com.example.android.uamp.media.extensions.id
import com.google.gson.Gson
import javax.inject.Inject

class PlayQueueViewModel @Inject constructor(
    context: Context,
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    private val nowPlayingMediaItem = MutableLiveData<Track>()
    private val mediaItems = MutableLiveData<List<Track>>()

    private val serviceConnection: MusicServiceConnection = provideMusicServiceConnection(
        context,
        preferenceManager.getAuth(),
        getAudioQuality()
    )

    private val mediaMetadataObserver = Observer<MediaMetadataCompat> {
        updateState(it)
    }

    private val playbackStateObserver = Observer<PlaybackStateCompat> {
        val metadata = serviceConnection.nowPlaying.value ?: NOTHING_PLAYING
        updateState(metadata)
    }

    private val musicServiceConnection = serviceConnection.also {
        it.playbackState.observeForever(playbackStateObserver)
        it.nowPlaying.observeForever(mediaMetadataObserver)
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

    fun updatePlaylist(list: ArrayList<Track>, mediaId: String) {
        serviceConnection.replaceDataLoad(tracksToMusicJson(list), mediaId)
    }

    fun getMediaList(): LiveData<List<Track>> {
        return mediaItems
    }

    fun getNowPlayingMediaItem(): LiveData<Track> {
        return nowPlayingMediaItem
    }

    private fun getAudioQuality(): String {
        val quality = preferenceManager.getAudioQuality()
        return if (quality.isEmpty()) Constants.NORMAL else quality
    }

    private fun updateState(
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
    }

    override fun onCleared() {
        super.onCleared()
        musicServiceConnection.playbackState.removeObserver(playbackStateObserver)
        musicServiceConnection.nowPlaying.removeObserver(mediaMetadataObserver)
    }

}