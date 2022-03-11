package com.beat.util.listener

import com.beat.data.model.Playlist

interface PlaylistPopUpMenuClickListener {

    fun onPlaylistDownload(playlistId: String, download: Boolean)

    fun onAddToPlaylist(playlistId: String, playlistTitle: String)

    fun onEditPlaylist(playlistId: String, playlistTitle: String)

    fun onDeletePlaylist(playlist: Playlist)

    fun onShareOnSocialMedia(url: String)

}