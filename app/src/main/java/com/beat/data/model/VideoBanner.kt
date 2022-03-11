package com.beat.data.model

data class VideoBanner(
    val bannerId: String,
    val bannerImage: String,
    val releaseId: String,
    val releaseTitle: String,
    val type: String,
    val favourite: Boolean
)