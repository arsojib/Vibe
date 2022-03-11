package com.beat.view.splash

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beat.core.data.model.LoginTokenResponse
import com.beat.core.data.model.Resource
import com.beat.core.data.rest.Repository
import com.beat.core.data.storage.PreferenceManager
import com.beat.core.utils.CoreConstants
import com.beat.util.Constants
import kotlinx.coroutines.launch
import javax.inject.Inject

class SplashViewModel @Inject constructor(
    private val repository: Repository,
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    private var guestTokenResponse = MutableLiveData<Resource<LoginTokenResponse>>()
    private var refreshTokenResponse = MutableLiveData<Resource<LoginTokenResponse>>()

    fun checkUserLoggedIn() {
        if (preferenceManager.isGuestUser()) {
            //Holding temporary for UI update showing purpose
            guestTokenRequest()
        } else {
            refreshTokenRequest()
        }
    }

    private fun guestTokenRequest() {
        viewModelScope.launch {
            guestTokenResponse.value = Resource.loading(null)
            guestTokenResponse.value = repository.getGuestToken(
                CoreConstants.GRANT_TYPE_CLIENT_CREDENTIALS,
                Constants.CLIENT_ID,
                Constants.CLIENT_SECRET
            )
        }
    }

    private fun refreshTokenRequest() {
        viewModelScope.launch {
            refreshTokenResponse.value = Resource.loading(null)
            refreshTokenResponse.value = repository.getRefreshToken(
                CoreConstants.GRANT_TYPE_REFRESH_TOKEN,
                Constants.CLIENT_ID,
                Constants.CLIENT_SECRET,
                preferenceManager.getRefreshToken()
            )
        }
    }

    fun setWrongToken() {
//        localStorage.writeStringData(CoreConstants.ACCESS_TOKEN, "fafahmhuphmuowefhmpufhpo")
    }

    fun userOauthSuccess(loginTokenResponse: LoginTokenResponse) {
        preferenceManager.setUserOauth(loginTokenResponse)
    }

    fun guestOauthSuccess(loginTokenResponse: LoginTokenResponse) {
        preferenceManager.setGuestOauth(loginTokenResponse)
    }

    fun getGuestTokenResponse(): LiveData<Resource<LoginTokenResponse>> {
        return guestTokenResponse
    }

    fun getRefreshTokenResponse(): LiveData<Resource<LoginTokenResponse>> {
        return refreshTokenResponse
    }

}