package com.beat.core.data.model

import com.google.gson.annotations.SerializedName

data class FeaturedGroupPlaylistResponse(@SerializedName("groups") val groups : List<Groups>) {

    data class Groups (
        @SerializedName("id") val id : String,
        @SerializedName("name") val name : String,
        @SerializedName("image") val image : Image,
        @SerializedName("playlists") val playlists : List<Playlists>
    )

    data class Playlists (
        @SerializedName("id") val id : String
    )

    data class Image (
        @SerializedName("w50") val w50 : String,
        @SerializedName("h50") val h50 : String
    )

}