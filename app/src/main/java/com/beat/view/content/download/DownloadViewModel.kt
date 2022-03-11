package com.beat.view.content.download

import android.content.Context
import androidx.lifecycle.*
import com.beat.core.data.model.PlaylistDetailsWithTrackResponse
import com.beat.core.data.model.Resource
import com.beat.core.data.rest.Repository
import com.beat.core.data.storage.OfflineDownloadConnection
import com.beat.core.data.storage.PreferenceManager
import com.beat.core.utils.CoreConstants
import com.beat.data.model.Playlist
import com.beat.data.model.Release
import com.beat.data.model.Track
import com.beat.di.module.contentDetails.ContentScope
import com.beat.util.*
import com.example.android.uamp.common.MusicServiceConnection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@ContentScope
class DownloadViewModel @Inject constructor(
    context: Context,
    private val repository: Repository,
    private val preferenceManager: PreferenceManager,
    private val offlineDownloadConnection: OfflineDownloadConnection
) : ViewModel() {

    private val serviceConnection: MusicServiceConnection = provideMusicServiceConnection(
        context,
        preferenceManager.getAuth(),
        getAudioQuality()
    )

    private val trackList = MutableLiveData<Resource<List<Track>>>()
    private val releaseList = MutableLiveData<Resource<List<Release>>>()
    private val playlistList = MutableLiveData<Resource<List<Playlist>>>()

    fun fetchTrackList() {
        viewModelScope.launch {
            trackList.value = Resource.loading(null)
            val list = getTrackListFromRawData(offlineDownloadConnection.getAllTrack())
            if (list.size != 0) {
                trackList.value = Resource.success(list)
            } else {
                trackList.value = Resource.error(null, "no data", -1)
            }
        }
    }

    fun fetchReleaseList() {
        viewModelScope.launch {
            releaseList.value = Resource.loading(null)
            val list = getReleaseListFromRawData(offlineDownloadConnection.getAllRelease())
            if (list.size != 0) {
                releaseList.value = Resource.success(list)
            } else {
                releaseList.value = Resource.error(null, "no data", -1)
            }
        }
    }

    fun fetchPlaylistList() {
        viewModelScope.launch {
            playlistList.value = Resource.loading(null)
            val list = getPlaylistListFromRawData(offlineDownloadConnection.getAllPlaylist())
            if (list.size != 0) {
                playlistList.value = Resource.success(list)
            } else {
                playlistList.value = Resource.error(null, "no data", -1)
            }
        }
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

    fun patchFavorite(id: String, type: String, favorite: Boolean) {
        viewModelScope.launch {
            repository.patchFavorite(id, type, favorite)
            if (type == CoreConstants.FAVORITE_TRACK)
                offlineDownloadConnection.setTrackFavorite(id, favorite)
            else
                offlineDownloadConnection.setReleaseFavorite(id, favorite)
        }
    }

    fun playTrackList(list: ArrayList<Track>, track: Track) {
        serviceConnection.addMediaToPlayer(
            isRadioInitialed,
            tracksToOfflineMusicJson(list),
            true,
            track.trackId
        )
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

    fun getDownloadObserver(
        trackId: String,
        observer: Observer<Int>,
        lifecycleOwner: LifecycleOwner
    ) {
        offlineDownloadConnection.getObserver(trackId, observer, lifecycleOwner)
    }

    fun getDataUpdatedObserver(
        observer: Observer<Boolean>,
        lifecycleOwner: LifecycleOwner
    ) {
        offlineDownloadConnection.getDataUpdatedObserver(observer, lifecycleOwner)
    }

    fun removeTrack(trackId: String) {
        offlineDownloadConnection.removeTrack(trackId)
    }

    fun removePlaylist(playlistId: String) {
        offlineDownloadConnection.removePlaylist(playlistId)
    }

    fun getTrackList(): LiveData<Resource<List<Track>>> = trackList

    fun getReleaseList(): LiveData<Resource<List<Release>>> = releaseList

    fun getPlaylistList(): LiveData<Resource<List<Playlist>>> = playlistList

    private fun getTrackListFromRawData(rawData: List<com.beat.core.data.storage.database.entity.Track>): ArrayList<Track> {
        val trackList = ArrayList<Track>()
        rawData.forEach {
            trackList.add(
                Track(
                    it.trackId,
                    it.trackTitle,
                    it.trackImage,
                    it.duration,
                    it.releaseId ?: "",
                    it.artistId,
                    it.artistName,
                    it.url,
                    it.favorite
                )
            )
        }
        return trackList
    }

    private fun getReleaseListFromRawData(rawData: List<com.beat.core.data.storage.database.entity.Release>): ArrayList<Release> {
        val releaseList = ArrayList<Release>()
        rawData.forEach {
            releaseList.add(
                Release(
                    it.releaseId,
                    it.releaseTitle,
                    it.releaseImage,
                    it.releaseDate,
                    it.artistId,
                    it.artistName,
                    it.type,
                    it.copyRight,
                    it.url,
                    it.favorite
                )
            )
        }
        return releaseList
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

    private fun getPlaylistListFromRawData(rawData: List<com.beat.core.data.storage.database.entity.Playlist>): ArrayList<Playlist> {
        val playlistList = ArrayList<Playlist>()
        rawData.forEach {
            playlistList.add(
                Playlist(
                    it.playlistId,
                    it.playlistTitle,
                    it.playlistImage,
                    it.trackCount,
                    it.url,
                    it.permission,
                    it.favourite
                )
            )
        }
        return playlistList
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

}