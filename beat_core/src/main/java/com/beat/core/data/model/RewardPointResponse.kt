package com.beat.core.data.model

import com.google.gson.annotations.SerializedName

data class RewardPointResponse(@SerializedName("balances") val balances: List<Balances>) {

    data class Balances(
        @SerializedName("id") val id: Int,
        @SerializedName("value") val value: Int,
        @SerializedName("metric") val metric: Metric
    )

    data class Metric(
        @SerializedName("id") val id: Int,
        @SerializedName("name") val name: String
    )

}