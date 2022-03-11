package com.beat.util.listener

import com.beat.data.model.Release

interface VideoClickListener {

    fun onVideoClick(release: Release, position: Int)

}