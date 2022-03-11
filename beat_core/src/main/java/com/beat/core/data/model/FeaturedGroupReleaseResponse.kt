package com.beat.core.data.model

import com.google.gson.annotations.SerializedName

data class FeaturedGroupReleaseResponse(@SerializedName("groups") val groups : List<Groups>) {

    data class Groups (
        @SerializedName("id") val id : String,
        @SerializedName("name") val name : String,
        @SerializedName("permission") val permission : String,
        @SerializedName("favourite") val favourite : Boolean,
        @SerializedName("releases") val releases : List<Releases>
    )

    data class Releases (
        @SerializedName("id") val id : String
    )

}