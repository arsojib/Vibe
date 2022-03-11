package com.beat.core.data.model

import com.beat.core.utils.CoreConstants.IMAGE_SIZE_HEIGHT_PARAMETER
import com.beat.core.utils.CoreConstants.IMAGE_SIZE_WIDTH_PARAMETER
import com.google.gson.annotations.SerializedName

data class TopReleaseByArtistResponse(@SerializedName("releases") val releases : List<Releases>) {

    data class Releases (
        @SerializedName("id") val id : String,
        @SerializedName("title") val title : String,
        @SerializedName("cover") val cover : Cover,
        @SerializedName("artist") val artist : Artist,
        @SerializedName("actors") val actors : List<Actors>,
        @SerializedName("type") val type : String,
        @SerializedName("favourite") val favourite : Boolean,
        @SerializedName("url") val url : String,
        @SerializedName("copyright") val copyright : String,
        @SerializedName("release_date") val release_date : String
    )

    data class Cover (
        @SerializedName(IMAGE_SIZE_WIDTH_PARAMETER) val width : String,
        @SerializedName(IMAGE_SIZE_HEIGHT_PARAMETER) val height : String,
        @SerializedName("release_id") val release_id : Int
    )

    data class Artist (
        @SerializedName("id") val id : String,
        @SerializedName("name") val name : String,
        @SerializedName("url") val url : String
    )

    data class Actors (
        @SerializedName("id") val id : String,
        @SerializedName("name") val name : String,
        @SerializedName("sort_name") val sort_name : String,
        @SerializedName("role") val role : String
    )

}