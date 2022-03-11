package com.beat.view.content.favourite

import android.content.Context
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beat.R
import com.beat.core.data.model.*
import com.beat.core.data.rest.Repository
import com.beat.core.data.storage.OfflineDownloadConnection
import com.beat.core.data.storage.PreferenceManager
import com.beat.data.model.*
import com.beat.di.module.contentDetails.ContentScope
import com.beat.util.*
import com.example.android.uamp.common.EMPTY_PLAYBACK_STATE
import com.example.android.uamp.common.MusicServiceConnection
import com.example.android.uamp.common.NOTHING_PLAYING
import com.example.android.uamp.media.extensions.displayDescription
import com.example.android.uamp.media.extensions.duration
import com.example.android.uamp.media.extensions.id
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@ContentScope
class FavoriteViewModel @Inject constructor(
    context: Context,
    private val repository: Repository,
    private val preferenceManager: PreferenceManager,
    private val offlineDownloadConnection: OfflineDownloadConnection
) : ViewModel() {

    private val nowPlayingMediaItem = MutableLiveData<Track>()
    private var playbackState: PlaybackStateCompat = EMPTY_PLAYBACK_STATE

    private val serviceConnection: MusicServiceConnection = provideMusicServiceConnection(
        context,
        preferenceManager.getAuth(),
        getAudioQuality()
    )

    private val mediaMetadataObserver = Observer<MediaMetadataCompat> {
        updateState(it)
    }

    private val playbackStateObserver = Observer<PlaybackStateCompat> {
        playbackState = it ?: EMPTY_PLAYBACK_STATE
        val metadata = serviceConnection.nowPlaying.value ?: NOTHING_PLAYING
        updateState(metadata)
    }

    private val musicServiceConnection = serviceConnection.also {
        it.playbackState.observeForever(playbackStateObserver)
        it.nowPlaying.observeForever(mediaMetadataObserver)
    }

    private val favoriteAudioPatch = MutableLiveData<Resource<ArrayList<Patch>>>()
    private val favoriteVideosResponse = MutableLiveData<Resource<FavoriteVideosResponse>>()
    private val favoriteArtistResponse = MutableLiveData<Resource<FavoriteArtistResponse>>()
    private var deletePlaylistResponse = MutableLiveData<Resource<String>>()

    fun getFavoriteAudios(context: Context) {
        viewModelScope.launch {
            favoriteAudioPatch.value = Resource.loading(null)
            val tracks = async { repository.getFavoriteTracks() }
            val releases = async { repository.getFavoriteRelease() }
            val playlist = async { repository.getFavoritePlaylist() }
            val tracksResponse = tracks.await()
            val releasesResponse = releases.await()
            val playlistResponse = playlist.await()

            val audioPatch = ArrayList<Patch>()
            val trackList = getTracksFromResponse(tracksResponse.data)
            val releaseList = getReleaseFromResponse(releasesResponse.data)
            val playlistList = getPlaylistFromResponse(playlistResponse.data)
            if (releaseList.size != 0) audioPatch.add(
                Patch(
                    "",
                    context.getString(R.string.albums),
                    Constants.RELEASE_PATCH,
                    true,
                    releaseList
                )
            )
            if (playlistList.size != 0) audioPatch.add(
                Patch(
                    "",
                    context.getString(R.string.playlists),
                    Constants.PLAYLIST_PATCH,
                    true,
                    playlistList
                )
            )
            if (trackList.size != 0) audioPatch.add(
                Patch(
                    "",
                    context.getString(R.string.tracks),
                    Constants.TRACK_PATCH,
                    true,
                    trackList
                )
            )

            favoriteAudioPatch.value =
                if (audioPatch.size != 0) Resource.success(audioPatch) else Resource.error(
                    null,
                    "",
                    -1
                )
        }
    }

    fun getFavoriteVideos() {
        viewModelScope.launch {
            favoriteVideosResponse.value = Resource.loading(null)
            favoriteVideosResponse.value = repository.getFavoriteVideos()
        }
    }

    fun getFavoriteArtists() {
        viewModelScope.launch {
            favoriteArtistResponse.value = Resource.loading(null)
            favoriteArtistResponse.value = repository.getFavoriteArtists()
        }
    }

    fun patchFavorite(id: String, type: String, favorite: Boolean) {
        viewModelScope.launch {
            repository.patchFavorite(id, type, favorite)
        }
    }

    fun deletePlaylist(playlistId: String) {
        viewModelScope.launch {
            deletePlaylistResponse.value = Resource.loading(null)
            deletePlaylistResponse.value = repository.deletePlaylist(playlistId)
        }
    }

    fun fetchCurrentPlayingItem() {
        val nowPlaying = musicServiceConnection.nowPlaying.value
        if (!nowPlaying?.id.isNullOrEmpty()) {
            updateState(nowPlaying!!)
        }
    }

    fun playTrack(track: Track) {
        val list = ArrayList<Track>()
        list.add(track)
        musicServiceConnection.addMediaToPlayer(
            isRadioInitialed,
            tracksToMusicJson(list),
            true,
            track.trackId
        )
    }

    fun downloadPlaylist(playlistId: String) {
        viewModelScope.launch {
            val resource = repository.getPlaylistDetailsWithTrack(playlistId)
            if (resource.status == Resource.Status.SUCCESS) {
                val playlist = getPlaylistFromResponse(resource.data!!)
                val tracks = getTrackListFromPlaylistDetailsResponse(resource.data!!)
                if (playlist != null && tracks.size != 0) {
                    processPlaylistDownloadData(
                        playlist,
                        tracks
                    ) { downPlaylist, downTracks ->
                        offlineDownloadConnection.downloadPlaylist(
                            downPlaylist,
                            downTracks
                        )
                    }
                }
            }
        }
    }

    fun isPlaylistDownloaded(playlistId: String, callBack: (Boolean) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val isExist = offlineDownloadConnection.isPlaylistExist(playlistId)
            withContext(Dispatchers.Main) { callBack(isExist) }
        }
    }

    fun hasPermission(context: Context, callback: () -> Unit) {
        if (preferenceManager.hasGenericPermission()) {
            callback()
        } else {
            moveToSubscription(context)
        }
    }

    fun removePlaylist(playlistId: String) {
        offlineDownloadConnection.removePlaylist(playlistId)
    }

    fun playTrackToNext(track: Track, nowPlay: Boolean) {
        musicServiceConnection.addNextMediaToPlayer(trackToMusicJson(track), nowPlay)
    }

    fun addTrackToQueue(track: Track) {
        musicServiceConnection.addQueueMediaToPlayer(trackToMusicJson(track))
    }

    fun getFavoriteAudioPatch(): LiveData<Resource<ArrayList<Patch>>> {
        return favoriteAudioPatch
    }

    fun getFavoriteVideosResponse(): LiveData<Resource<FavoriteVideosResponse>> {
        return favoriteVideosResponse
    }

    fun getFavoriteArtistResponse(): LiveData<Resource<FavoriteArtistResponse>> {
        return favoriteArtistResponse
    }

    fun getDeletePlaylistResponse(): LiveData<Resource<String>> {
        return deletePlaylistResponse
    }

    fun getNowPlayingMediaItem(): LiveData<Track> {
        return nowPlayingMediaItem
    }

    fun getPlaybackStateCompat(): PlaybackStateCompat {
        return playbackState
    }

    private fun getTracksFromResponse(favoriteTracksResponse: FavoriteTracksResponse?): ArrayList<Track> {
        val list: ArrayList<Track> = ArrayList()
        favoriteTracksResponse?.tracks?.forEach { track ->
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

    private fun getReleaseFromResponse(favoriteReleaseResponse: FavoriteReleaseResponse?): ArrayList<Release> {
        val list: ArrayList<Release> = ArrayList()
        favoriteReleaseResponse?.releases?.forEach { release ->
            list.add(
                Release(
                    release.id,
                    release.title,
                    release.cover.width,
                    release.release_date,
                    release.artist.id,
                    release.artist.name,
                    release.type,
                    release.copyright,
                    release.url,
                    release.favourite
                )
            )
        }
        return list
    }

    fun getReleaseFromVideoResponse(favoriteVideosResponse: FavoriteVideosResponse?): ArrayList<Release> {
        val list: ArrayList<Release> = ArrayList()
        favoriteVideosResponse?.releases?.forEach { release ->
            list.add(
                Release(
                    release.id,
                    release.title,
                    release.cover.width,
                    release.release_date,
                    release.artist.id,
                    release.artist.name,
                    release.type,
                    release.copyright,
                    release.url,
                    release.favourite
                )
            )
        }
        return list
    }

    private fun getTrackListFromPlaylistDetailsResponse(playlistDetailsWithTrackResponse: PlaylistDetailsWithTrackResponse?): ArrayList<Track> {
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

    private fun getPlaylistFromResponse(favoritePlaylistResponse: FavoritePlaylistResponse?): ArrayList<Playlist> {
        val list: ArrayList<Playlist> = ArrayList()
        favoritePlaylistResponse?.playlists?.forEach { playlist ->
            list.add(
                Playlist(
                    playlist.id,
                    playlist.title,
                    if (playlist.covers.isNotEmpty()) playlist.covers[0].width else "",
                    playlist.track_count,
                    playlist.url,
                    playlist.permission,
                    playlist.favourite
                )
            )
        }
        return list
    }

    private fun getPlaylistFromResponse(playlistDetailsWithTrackResponse: PlaylistDetailsWithTrackResponse?): Playlist? {
        return if (playlistDetailsWithTrackResponse != null) {
            Playlist(
                playlistDetailsWithTrackResponse.playlist.id,
                playlistDetailsWithTrackResponse.playlist.title,
                if (playlistDetailsWithTrackResponse.playlist.tracks.isNotEmpty()) playlistDetailsWithTrackResponse.playlist.tracks[0].release.cover.width else "",
                playlistDetailsWithTrackResponse.playlist.track_count,
                playlistDetailsWithTrackResponse.playlist.url,
                playlistDetailsWithTrackResponse.playlist.permission,
                playlistDetailsWithTrackResponse.playlist.favourite
            )
        } else {
            null
        }
    }

    fun getArtistsFromResponse(favoriteArtistResponse: FavoriteArtistResponse?): ArrayList<Artist> {
        val list: ArrayList<Artist> = ArrayList()
        favoriteArtistResponse?.artists?.forEach { artist ->
            list.add(
                Artist(
                    "",
                    artist.image.width,
                    artist.id,
                    artist.name,
                    artist.favourite
                )
            )
        }
        return list
    }

    private suspend fun processPlaylistDownloadData(
        playlist: Playlist,
        tracks: List<Track>,
        callBack: ((
            com.beat.core.data.storage.database.entity.Playlist,
            List<com.beat.core.data.storage.database.entity.Track>
        ) -> Unit)
    ) = withContext(Dispatchers.Default) {
        val downPlaylist =
            com.beat.core.data.storage.database.entity.Playlist(
                playlist.playlistId,
                playlist.playlistTitle,
                playlist.playlistImage,
                playlist.trackCount,
                playlist.url,
                playlist.permission,
                playlist.favourite
            )
        val downTracks =
            ArrayList<com.beat.core.data.storage.database.entity.Track>()
        tracks.forEach { track ->
            downTracks.add(
                com.beat.core.data.storage.database.entity.Track(
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
            )
        }
        callBack(downPlaylist, downTracks)
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