package com.beat.core.data.model

import com.google.gson.annotations.SerializedName

data class CurrentSubscriptionResponse(@SerializedName("subscriptions") val subscriptions: List<Subscription>) {

}