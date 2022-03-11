package com.beat.core.data.model

import com.google.gson.annotations.SerializedName

data class Tracks(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("subtitle") val subtitle: String,
    @SerializedName("duration") val duration: Int,
    @SerializedName("artist") val artist: TopTrackByArtistResponse.Artist,
    @SerializedName("actors") val actors: List<TopTrackByArtistResponse.Actors>,
    @SerializedName("favourite") val favourite: Boolean,
    @SerializedName("url") val url: String,
    @SerializedName("release") val release: TopTrackByArtistResponse.Release
)