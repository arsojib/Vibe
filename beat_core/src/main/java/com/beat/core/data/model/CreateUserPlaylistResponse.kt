package com.beat.core.data.model

import com.google.gson.annotations.SerializedName

data class CreateUserPlaylistResponse(@SerializedName("playlist") val playlist: Playlist) {

    data class Playlist(
        @SerializedName("id") val id: String,
        @SerializedName("title") val title: String,
        @SerializedName("description") val description: String,
        @SerializedName("permission") val permission: String,
        @SerializedName("modified") val modified: String,
        @SerializedName("track_count") val track_count: Int,
        @SerializedName("duration") val duration: Int,
        @SerializedName("favourite") val favourite: Boolean,
        @SerializedName("url") val url: String,
        @SerializedName("version") val version: String,
        @SerializedName("tracks") val tracks: List<String>,
        @SerializedName("favouritees_count") val favouritees_count: Int
    )

}