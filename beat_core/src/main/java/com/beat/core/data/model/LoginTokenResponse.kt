package com.beat.core.data.model

import com.google.gson.annotations.SerializedName

data class LoginTokenResponse(
    @SerializedName("access_token") val access_token: String,
    @SerializedName("expires_in") val expires_in: Int,
    @SerializedName("token_type") val token_type: String,
    @SerializedName("scope") val scope: String,
    @SerializedName("refresh_token") val refresh_token: String?,
    @SerializedName("user_id") val user_id: String?
)