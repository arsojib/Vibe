package com.beat.core.utils.network

import android.util.Log
import com.beat.core.data.model.LoginTokenResponse
import com.beat.core.data.storage.PreferenceManager
import com.beat.core.utils.CoreConstants
import kotlinx.coroutines.*
import okhttp3.*

class SupportInterceptor constructor(
    private val apiServiceHolder: ApiServiceHolder,
    private val preferenceManager: PreferenceManager,
    private val clintId: String,
    private val clientSecret: String
) : Interceptor, Authenticator {

    private var fetchingToken = false

    override fun intercept(chain: Interceptor.Chain): Response? {
        val response = chain.proceed(chain.request())
        val code = response.code()
        if (code == 400) {
            return chain.proceed(authenticate(null, response)!!)
        }
        return response
    }

    override fun authenticate(route: Route?, response: Response): Request? {
        if (!fetchingToken) {
            fetchingToken = true
            setAuth()
        }
        Log.d("SupportInterceptor", "waiting")
        while (true) {
            Thread.sleep(500)
            if (!fetchingToken) break
        }
        Log.d("SupportInterceptor", "waiting end " + getAccessToken())
        return response.request().newBuilder()
            .removeHeader("Authorization")
            .addHeader("Authorization", getAccessToken())
            .build()
    }

    private fun setAuth() {
        val userId: String = preferenceManager.getUserId()
        val apiService = apiServiceHolder.getApiService()!!

        Log.d("SupportInterceptor", "start")

        GlobalScope.launch {
            val tokenResponse: retrofit2.Response<LoginTokenResponse>
            tokenResponse = if (userId.isEmpty()) {
                apiService.guestToken(
                    CoreConstants.GRANT_TYPE_CLIENT_CREDENTIALS,
                    clintId,
                    clientSecret
                )
            } else {
                val refreshToken = preferenceManager.getRefreshToken()
                apiService.refreshToken(
                    CoreConstants.GRANT_TYPE_REFRESH_TOKEN,
                    clintId,
                    clientSecret,
                    refreshToken
                )
            }
            if (tokenResponse.isSuccessful && !tokenResponse.body()?.access_token.isNullOrEmpty()) {
                preferenceManager.setGuestOauth(tokenResponse.body()!!)
                Log.d("SupportInterceptor", tokenResponse.body()?.access_token!!)
            }
            fetchingToken = false
            Log.d("SupportInterceptor", "end")
        }
    }

    private fun getAccessToken(): String {
        return preferenceManager.getAuth()
    }

}