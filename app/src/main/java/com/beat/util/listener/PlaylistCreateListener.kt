package com.beat.util.listener

import com.beat.data.model.Playlist

interface PlaylistCreateListener {

    fun onPlaylistCreated(playlist: Playlist)

}