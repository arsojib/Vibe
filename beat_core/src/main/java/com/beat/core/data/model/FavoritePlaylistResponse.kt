package com.beat.core.data.model

import com.beat.core.utils.CoreConstants
import com.google.gson.annotations.SerializedName

data class FavoritePlaylistResponse(@SerializedName("playlists") val playlists : List<Playlists>) {

    data class Playlists (
        @SerializedName("id") val id : String,
        @SerializedName("title") val title : String,
        @SerializedName("description") val description : String,
        @SerializedName("permission") val permission : String,
        @SerializedName("modified") val modified : String,
        @SerializedName("track_count") val track_count : Int,
        @SerializedName("duration") val duration : Int,
        @SerializedName("covers") val covers : List<Covers>,
        @SerializedName("favourite") val favourite : Boolean,
        @SerializedName("url") val url : String,
        @SerializedName("version") val version : String
    )

    data class Covers (
        @SerializedName(CoreConstants.IMAGE_SIZE_WIDTH_PARAMETER) val width: String,
        @SerializedName(CoreConstants.IMAGE_SIZE_HEIGHT_PARAMETER) val height: String,
        @SerializedName("release_id") val release_id : Int
    )

}