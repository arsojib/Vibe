package com.beat.util.listener

import com.beat.data.model.Track

interface ContinuePlayingMenuListener {

    fun onPlayNow(track: Track)

    fun onPlayNext(track: Track)

    fun addToQueue(track: Track)

}