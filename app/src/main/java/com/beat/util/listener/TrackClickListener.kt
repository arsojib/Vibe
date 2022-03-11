package com.beat.util.listener

import com.beat.data.model.Track

interface TrackClickListener {

    fun onTrackClick(track: Track, position: Int, isCurrentlyPlaying: Boolean)

    fun onTrackMenuClick(track: Track)

}