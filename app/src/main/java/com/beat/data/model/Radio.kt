package com.beat.data.model

data class Radio(
    val bannerId: String,
    val bannerImage: String,
    val playlistId: String,
    val playlistTitle: String,
    val trackCount: Int,
    val favourite: Boolean
)