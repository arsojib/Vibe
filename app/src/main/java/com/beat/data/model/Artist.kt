package com.beat.data.model

data class Artist(
    val bannerId: String,
    val bannerImage: String,
    val artistId: String,
    val artistTitle: String,
    var favourite: Boolean,
    var bio: String? = ""
)