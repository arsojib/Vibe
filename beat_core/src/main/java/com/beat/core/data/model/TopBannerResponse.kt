package com.beat.core.data.model

import com.google.gson.annotations.SerializedName

data class TopBannerResponse(@SerializedName("group") val group: Group) {

    data class Group(
        @SerializedName("id") val id: String,
        @SerializedName("name") val name: String,
        @SerializedName("visible") val visible: Boolean,
        @SerializedName("banners") val banners: List<Banners>
    )

    data class Link(
        @SerializedName("release") val release: Release?,
        @SerializedName("playlist") val playlist: Playlist?
    )

    data class Release(
        @SerializedName("id") val id: String,
        @SerializedName("title") val title: String,
        @SerializedName("artist") val artist: Artist,
        @SerializedName("actors") val actors: List<Actors>,
        @SerializedName("type") val type: String,
        @SerializedName("favourite") val favourite: Boolean,
        @SerializedName("url") val url: String,
        @SerializedName("copyright") val copyright: String,
        @SerializedName("release_date") val release_date: String,
        @SerializedName("genres") val genres: List<String>
    )

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
        @SerializedName("version") val version: String
    ) {
        val type: String get() = "playlist"
    }

    data class Image(
        @SerializedName("w50") val w50: String,
        @SerializedName("h50") val h50: String
    )

    data class Banners(
        @SerializedName("id") val id: String,
        @SerializedName("image") val image: Image,
        @SerializedName("link") val link: Link,
        @SerializedName("visible") val visible: Boolean
    )

    data class Artist(
        @SerializedName("id") val id: String,
        @SerializedName("name") val name: String,
        @SerializedName("url") val url: String
    )

    data class Actors(
        @SerializedName("id") val id: String,
        @SerializedName("name") val name: String,
        @SerializedName("sort_name") val sort_name: String,
        @SerializedName("role") val role: String
    )

}