package com.beat.core.data.model

import com.google.gson.annotations.SerializedName

data class UserResponse(
    @SerializedName("user") val user: User
) {

    data class User(
        @SerializedName("id") val id: String,
        @SerializedName("favourite") val favourite: Boolean,
        @SerializedName("favouritees_count") val favouritees_count: Int,
        @SerializedName("favourite_users_count") val favourite_users_count: Int,
        @SerializedName("firstname") val firstname: String,
        @SerializedName("lastname") val lastname: String,
        @SerializedName("username") val username: String,
        @SerializedName("email") val email: String,
        @SerializedName("facebook_id") val facebook_id: String,
        @SerializedName("msisdn") val msisdn: String,
        @SerializedName("privacy_profile_private") val privacy_profile_private: Boolean,
        @SerializedName("payment_type") val payment_type: Int,
        @SerializedName("newsletter") val newsletter: Boolean
    )

}