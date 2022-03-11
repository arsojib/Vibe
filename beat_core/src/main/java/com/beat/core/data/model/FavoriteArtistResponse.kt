package com.beat.core.data.model

import com.beat.core.utils.CoreConstants
import com.google.gson.annotations.SerializedName

data class FavoriteArtistResponse(@SerializedName("artists") val artists: List<Artists>) {

    data class Artists(
        @SerializedName("id") val id: String,
        @SerializedName("name") val name: String,
        @SerializedName("url") val url: String,
        @SerializedName("image") val image: Image,
        @SerializedName("favourite") val favourite: Boolean
    )

    data class Image(
        @SerializedName(CoreConstants.IMAGE_SIZE_WIDTH_PARAMETER) val width: String,
        @SerializedName(CoreConstants.IMAGE_SIZE_HEIGHT_PARAMETER) val height: String
    )

}