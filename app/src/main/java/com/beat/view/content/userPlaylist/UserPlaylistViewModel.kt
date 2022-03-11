package com.beat.view.content.userPlaylist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beat.core.data.model.*
import com.beat.core.data.rest.Repository
import com.beat.data.model.Playlist
import com.beat.data.model.Track
import kotlinx.coroutines.launch
import javax.inject.Inject

class UserPlaylistViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {

    private val userPlaylistResponse = MutableLiveData<Resource<UserPlaylistResponse>>()
    private val createPlaylistResponse =
        MutableLiveData<Event<Resource<CreateUserPlaylistResponse>>>()
    private val trackAddToPlaylistResponse = MutableLiveData<Resource<String>>()
    private val playlistDetailsWithTrackResponse =
        MutableLiveData<Resource<PlaylistDetailsWithTrackResponse>>()
    private val modifiedPlaylistDetailsWithTrackResponse =
        MutableLiveData<Resource<String>>()

    fun getUserPlaylists() {
        viewModelScope.launch {
            userPlaylistResponse.value = Resource.loading(null)
            userPlaylistResponse.value = repository.getUserPlaylist("playlists=0-10")
        }
    }

    fun getUserPlaylistsPagination(range: String) {
        viewModelScope.launch {
            userPlaylistResponse.value = Resource.loading(null)
            userPlaylistResponse.value = repository.getUserPlaylist(range)
        }
    }


    fun createPlaylist(title: String) {
        viewModelScope.launch {
            createPlaylistResponse.value = Event(Resource.loading(null))
            createPlaylistResponse.value = repository.createUserPlaylist(title)
        }
    }

    fun addTrackToPlaylist(playlistId: String, title: String, trackId: String) {
        viewModelScope.launch {
            trackAddToPlaylistResponse.value = Resource.loading(null)
            trackAddToPlaylistResponse.value =
                repository.addTracksToUserPlaylist(playlistId, trackId)
        }
    }

    fun getAllTrackOfPlaylist(playlistId: String) {
        viewModelScope.launch {
            playlistDetailsWithTrackResponse.value = Resource.loading(null)
            playlistDetailsWithTrackResponse.value =
                repository.getPlaylistDetailsWithTrack(playlistId)
        }
    }

    fun getAllTrackOfModifiedPlaylist(playlistId: String, data: String) {
        viewModelScope.launch {
            modifiedPlaylistDetailsWithTrackResponse.value = Resource.loading(null)
            modifiedPlaylistDetailsWithTrackResponse.value =
                repository.getModifiedPlaylistDetailsWithTrack(playlistId, data)
        }
    }

    fun getUserPlaylistResponse(): LiveData<Resource<UserPlaylistResponse>> {
        return userPlaylistResponse
    }

    fun getCreatePlaylistResponse(): LiveData<Event<Resource<CreateUserPlaylistResponse>>> {
        return createPlaylistResponse
    }

    fun getTrackAddToPlaylistResponse(): LiveData<Resource<String>> {
        return trackAddToPlaylistResponse
    }

    fun getPlaylistDetailsWithTrackResponse(): LiveData<Resource<PlaylistDetailsWithTrackResponse>> {
        return playlistDetailsWithTrackResponse
    }

    fun getModifiedPlaylistDetailsWithTrackResponse(): LiveData<Resource<String>> {
        return modifiedPlaylistDetailsWithTrackResponse
    }

    fun getPlaylistsFromResponse(userPlaylistResponse: UserPlaylistResponse?): ArrayList<Playlist> {
        val list: ArrayList<Playlist> = ArrayList()
        userPlaylistResponse?.playlists?.forEach { playlist ->
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

    fun getPlaylistFromResponse(createUserPlaylistResponse: CreateUserPlaylistResponse?): Playlist? {
        var playlist: Playlist? = null
        if (createUserPlaylistResponse != null) {
            playlist = Playlist(
                createUserPlaylistResponse.playlist.id,
                createUserPlaylistResponse.playlist.title,
                "",
                createUserPlaylistResponse.playlist.track_count,
                createUserPlaylistResponse.playlist.url,
                createUserPlaylistResponse.playlist.permission,
                createUserPlaylistResponse.playlist.favourite
            )
        }
        return playlist
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

}