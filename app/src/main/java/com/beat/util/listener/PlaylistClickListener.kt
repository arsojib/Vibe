package com.beat.util.listener

import com.beat.data.model.Playlist

interface PlaylistClickListener {

    fun onPlaylistClick(playlist: Playlist, position: Int)

}