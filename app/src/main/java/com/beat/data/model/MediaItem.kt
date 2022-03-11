package com.beat.data.model

data class MediaItem(
    val mediaId: String,
    val title: String,
    val details: String,
    val imageUri: String,
    var progress: Int
)