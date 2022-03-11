package com.beat.core.data.model

import com.google.gson.annotations.SerializedName

data class StreamResponse(@SerializedName("stream") val stream: Stream) {

    data class Stream(
        @SerializedName("id") val id: String,
        @SerializedName("url") val url: String,
        @SerializedName("type") val type: String,
        @SerializedName("duration") val duration: Int
    )

}