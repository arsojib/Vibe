package com.beat.core.data.model

import com.google.gson.annotations.SerializedName

data class ArtistBioResponse(@SerializedName("bio") val bio: Bio) {

    data class Bio(
        @SerializedName("summary") val summary: String,
        @SerializedName("url") val url: String,
        @SerializedName("source") val source: String
    )

}