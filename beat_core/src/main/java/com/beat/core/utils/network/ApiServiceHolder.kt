package com.beat.core.utils.network

import com.beat.core.data.rest.ApiService

class ApiServiceHolder {

    private var apiService: ApiService? = null

    fun getApiService(): ApiService? {
        return apiService
    }

    fun setApiService(apiService: ApiService?) {
        this.apiService = apiService
    }

}