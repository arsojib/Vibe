package com.beat.core.data.model

import com.google.gson.annotations.SerializedName

data class GuestTokenResponse(
    @SerializedName("access_token") val access_token: String,
    @SerializedName("expires_in") val expires_in: Int,
    @SerializedName("token_type") val token_type: String,
    @SerializedName("scope") val scope: String
)