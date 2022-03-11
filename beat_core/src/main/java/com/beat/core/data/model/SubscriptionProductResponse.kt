package com.beat.core.data.model

import com.google.gson.annotations.SerializedName

data class SubscriptionProductResponse(@SerializedName("products") val products: List<Product>) {

}