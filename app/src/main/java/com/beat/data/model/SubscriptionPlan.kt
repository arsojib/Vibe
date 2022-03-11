package com.beat.data.model

data class SubscriptionPlan(
    val productId: String,
    val subscriptionId: String,
    val name: String,
    val description: String,
    val surfaceId: String,
    val type: Int,
    val state: Int,
    val recurringPrice: Int,
    val recurringInterval: String?,
    val recurringIntervalUnit: String?,
    val trialPrice: Int,
    val nextBillingDate: String
)