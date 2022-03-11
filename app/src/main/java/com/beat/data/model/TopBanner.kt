package com.beat.data.model

data class TopBanner(
    val bannerId: String,
    val bannerImage: String,
    val releaseId: String,
    val releaseTitle: String,
    val artistId: String,
    val artistTitle: String,
    val playlistId: String,
    val playlistTitle: String,
    val type: String,
    val favourite: Boolean
)