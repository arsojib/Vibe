package com.beat.core.data.model

import com.google.gson.annotations.SerializedName

data class Product(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String,
    @SerializedName("surface_id") val surface_id: String,
    @SerializedName("type") val type: Int,
    @SerializedName("recurring_price") val recurring_price: Int,
    @SerializedName("recurring_interval") val recurring_interval: String,
    @SerializedName("recurring_interval_unit") val recurring_interval_unit: String,
    @SerializedName("trial_price") val trial_price: Int,
    @SerializedName("trial_interval") val trial_interval: String,
    @SerializedName("trial_interval_unit") val trial_interval_unit: String,
    @SerializedName("lock_interval") val lock_interval: String,
    @SerializedName("lock_interval_unit") val lock_interval_unit: String
)