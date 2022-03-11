package com.beat.view.common.subscribtion

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beat.core.data.model.Resource
import com.beat.core.data.model.SubscriptionProductResponse
import com.beat.core.data.rest.Repository
import com.beat.data.model.SubscriptionPlan
import kotlinx.coroutines.launch
import javax.inject.Inject

class SubscriptionViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {

    private val subscriptionPlans =
        MutableLiveData<Resource<List<SubscriptionPlan>>>()
    private val response = MutableLiveData<Resource<String>>()

    fun getSubscriptionProducts() {
        viewModelScope.launch {
            subscriptionPlans.value = Resource.loading(null)
            val productsResponse = repository.getSubscriptionProducts()
            val currentPlanResponse = repository.getCurrentSubscription()
            if (productsResponse.status == Resource.Status.SUCCESS) {
                var productId = ""
                var subscriptionId = ""
                var state = 0
                if (currentPlanResponse.status == Resource.Status.SUCCESS) {
                    currentPlanResponse.data.let {
                        it?.subscriptions.let { list ->
                            list?.let {
                                if (list.isNotEmpty()) {
                                    list[0].let {
                                        productId =
                                            currentPlanResponse.data?.subscriptions?.get(0)!!.product_id
                                        subscriptionId =
                                            currentPlanResponse.data?.subscriptions?.get(0)!!.id
                                        state =
                                            currentPlanResponse.data?.subscriptions?.get(0)!!.state
                                    }
                                }
                            }
                        }
                    }
                }
                val list = getSubscriptionPlanFromResponse(
                    productsResponse.data,
                    productId,
                    subscriptionId,
                    state
                )
                subscriptionPlans.value = Resource.success(list)
            } else {
                subscriptionPlans.value =
                    Resource.error(null, productsResponse.message!!, productsResponse.code!!)
            }
        }
    }

    fun subscribe(subscriptionId: String) {
        viewModelScope.launch {
            response.value = Resource.loading(null)
            response.value = repository.activateSubscription(subscriptionId)
        }
    }

    fun unsubscribe(subscriptionId: String) {
        viewModelScope.launch {
            response.value = Resource.loading(null)
            response.value = repository.deleteSubscription(subscriptionId)
        }
    }

    fun getSubscriptionPlans(): LiveData<Resource<List<SubscriptionPlan>>> =
        subscriptionPlans

    fun getResponse(): MutableLiveData<Resource<String>> =
        response

    private fun getSubscriptionPlanFromResponse(
        subscriptionProductResponse: SubscriptionProductResponse?,
        productId: String,
        subscriptionId: String,
        state: Int
    ): ArrayList<SubscriptionPlan> {
        val list: ArrayList<SubscriptionPlan> = ArrayList()
        subscriptionProductResponse?.products?.forEach {
            if (it.description != null) {
                list.add(
                    SubscriptionPlan(
                        it.id,
                        if (productId == it.id) subscriptionId else "",
                        it.name,
                        it.description,
                        it.surface_id,
                        it.type,
                        if (productId == it.id) state else 0,
                        it.recurring_price,
                        it.recurring_interval,
                        it.recurring_interval_unit,
                        it.trial_price,
                        ""
                    )
                )
            }
        }
        return list
    }

}