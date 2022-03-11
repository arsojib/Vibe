package com.beat.core.data.model

import com.google.gson.annotations.SerializedName

data class TrendingArtistResponse(@SerializedName("group") val group: Group) {

    data class Group(
        @SerializedName("id") val id: String,
        @SerializedName("name") val name: String,
        @SerializedName("visible") val visible: Boolean,
        @SerializedName("banners") val banners: List<Banners>
    )

    data class Link(
        @SerializedName("artist") val artist: Artist
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

    data class Artist(
        @SerializedName("id") val id: String,
        @SerializedName("name") val name: String,
        @SerializedName("url") val url: String,
        @SerializedName("image") val image: Image,
        @SerializedName("favourite") val favourite: Boolean
    )

}