package com.beat.core.data.model

import com.google.gson.annotations.SerializedName

data class Subscription(
    @SerializedName("id") val id: String,
    @SerializedName("product_id") val product_id: String,
    @SerializedName("surface_id") val surface_id: String,
    @SerializedName("biller_id") val biller_id: String,
    @SerializedName("start_date") val start_date: String,
    @SerializedName("expiry_date") val expiry_date: String,
    @SerializedName("next_billing_date") val next_billing_date: String,
    @SerializedName("state") val state: Int,
    @SerializedName("offline_media_expiry_date") val offline_media_expiry_date: String,
    @SerializedName("children") val children: List<String>
)