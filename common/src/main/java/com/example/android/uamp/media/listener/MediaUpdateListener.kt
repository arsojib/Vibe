package com.example.android.uamp.media.listener

import android.support.v4.media.MediaMetadataCompat

interface MediaUpdateListener {

    fun onMediaAdded(parentId: String, list: List<MediaMetadataCompat>)

    fun onMediaRemoved(parentId: String)

    fun addToNext(parentId: String, mediaMetadataCompat: MediaMetadataCompat, position: Int)

    fun addToQueue(parentId: String, mediaMetadataCompat: MediaMetadataCompat)

}