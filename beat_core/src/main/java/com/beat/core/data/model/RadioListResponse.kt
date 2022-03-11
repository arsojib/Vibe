package com.beat.core.data.model

import com.google.gson.annotations.SerializedName

data class RadioListResponse(@SerializedName("group") val group: Group) {

    data class Group(
        @SerializedName("id") val id: String,
        @SerializedName("name") val name: String,
        @SerializedName("visible") val visible: Boolean,
        @SerializedName("banners") val banners: List<Banners>
    )

    data class Playlist(
        @SerializedName("id") val id: String,
        @SerializedName("title") val title: String,
        @SerializedName("description") val description: String,
        @SerializedName("permission") val permission: String,
        @SerializedName("modified") val modified: String,
        @SerializedName("track_count") val trackCount: Int,
        @SerializedName("duration") val duration: Int,
        @SerializedName("favourite") val favourite: Boolean,
        @SerializedName("url") val url: String,
        @SerializedName("version") val version: String
    )

    data class Link(
        @SerializedName("playlist") val playlist: Playlist
    )

    data class Image(
        @SerializedName("w50") val w50: String,
        @SerializedName("h50") val h50: String
    )

    data class Banners(
        @SerializedName("id") val id: String,
        @SerializedName("image") val image: Image,
        @SerializedName("link") val link: Link,
        @SerializedName("title") val title: String,
        @SerializedName("visible") val visible: Boolean
    )

}