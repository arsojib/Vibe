package com.beat.util.listener

import com.beat.data.model.Release

interface ReleaseClickListener {

    fun onReleaseClick(release: Release, position: Int)

}