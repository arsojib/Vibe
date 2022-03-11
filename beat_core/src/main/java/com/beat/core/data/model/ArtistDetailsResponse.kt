package com.beat.core.data.model

import com.beat.core.utils.CoreConstants.IMAGE_SIZE_HEIGHT_PARAMETER
import com.beat.core.utils.CoreConstants.IMAGE_SIZE_WIDTH_PARAMETER
import com.google.gson.annotations.SerializedName

data class ArtistDetailsResponse(@SerializedName("artist") val artist: Artist) {

    data class Image(
        @SerializedName(IMAGE_SIZE_WIDTH_PARAMETER) val width: String,
        @SerializedName(IMAGE_SIZE_HEIGHT_PARAMETER) val height: String
    )

    data class Artist(
        @SerializedName("id") val id: String,
        @SerializedName("name") val name: String,
        @SerializedName("url") val url: String,
        @SerializedName("image") val image: Image,
        @SerializedName("favourite") val favourite: Boolean,
        @SerializedName("favouritees_count") val favouritees_count: Int
    )

}