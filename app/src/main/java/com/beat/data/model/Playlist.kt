package com.beat.data.model

data class Playlist(
    val playlistId: String,
    val playlistTitle: String,
    val playlistImage: String,
    val trackCount: Int,
    val url: String,
    val permission: String,
    var favourite: Boolean
)