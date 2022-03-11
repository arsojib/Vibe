package com.beat.core.data.model

import com.beat.core.utils.CoreConstants.IMAGE_SIZE_HEIGHT_PARAMETER
import com.beat.core.utils.CoreConstants.IMAGE_SIZE_WIDTH_PARAMETER
import com.google.gson.annotations.SerializedName

data class VideoReleaseGroupResponse(@SerializedName("group") val group: Group) {

    data class Group(
        @SerializedName("id") val id: String,
        @SerializedName("name") val name: String,
        @SerializedName("visible") val visible: Boolean,
        @SerializedName("banners") val banners: List<Banners>
    )

    data class Banners(
        @SerializedName("id") val id: String,
        @SerializedName("image") val image: Image,
        @SerializedName("link") val link: Link,
        @SerializedName("title") val title: String,
        @SerializedName("visible") val visible: Boolean
    )

    data class Image(
        @SerializedName(IMAGE_SIZE_WIDTH_PARAMETER) val width: String,
        @SerializedName(IMAGE_SIZE_HEIGHT_PARAMETER) val height: String
    )

    data class Link(
        @SerializedName("release-group") val releaseGroup: ReleaseGroup
    )

    data class ReleaseGroup(
        @SerializedName("id") val id: String
    )

}