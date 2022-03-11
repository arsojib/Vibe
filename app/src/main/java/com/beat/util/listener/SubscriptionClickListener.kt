package com.beat.util.listener

import com.beat.data.model.SubscriptionPlan

interface SubscriptionClickListener {

    fun onSubscriptionPlanClick(subscriptionPlan: SubscriptionPlan)

}