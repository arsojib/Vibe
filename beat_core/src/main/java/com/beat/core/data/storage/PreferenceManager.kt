package com.beat.core.data.storage

import com.beat.core.data.model.LoginTokenResponse
import com.beat.core.data.model.Subscription
import com.beat.core.data.model.UserResponse
import com.beat.core.di.scope.CoreScope
import com.beat.core.utils.CoreConstants
import javax.inject.Inject

@CoreScope
class PreferenceManager @Inject constructor(private val localStorage: LocalStorage) {

    private var userResponse: UserResponse? = null
    private var subscription: Subscription? = null

    //Setter
    fun setUserOauth(loginTokenResponse: LoginTokenResponse) {
        localStorage.writeUserOauthData(loginTokenResponse)
    }

    fun setGuestOauth(loginTokenResponse: LoginTokenResponse) {
        localStorage.writeGuestOauthData(loginTokenResponse)
    }

    fun setUserResponse(userResponse: UserResponse) {
        this.userResponse = userResponse
    }

    fun setSubscription(subscription: Subscription) {
        this.subscription = subscription
    }

    fun setPlayOnOffline(active: Boolean) {
        localStorage.writeBooleanData(CoreConstants.PLAY_ON_OFFLINE, active)
    }

    fun setStreamOnlyOnOffline(active: Boolean) {
        localStorage.writeBooleanData(CoreConstants.STREAM_ONLY_ON_OFFLINE, active)
    }

    fun setDownloadOnlyOnOffline(active: Boolean) {
        localStorage.writeBooleanData(CoreConstants.DOWNLOAD_ONLY_ON_OFFLINE, active)
    }

    fun setAudioQuality(quality: String) {
        localStorage.writeStringData(CoreConstants.AUDIO_QUALITY, quality)
    }


    //Getter
//    fun getUserResponse() = userResponse

//    fun getSubscription() = subscription

    fun isGuestUser(): Boolean {
        return localStorage.readStringData(CoreConstants.USER_ID).isEmpty()
    }

    fun isSubscribedUser(): Boolean {
        return subscription != null && subscription!!.state == 3
    }

    //Permission = [Download, Favorite, Create, Edit, Delete]
    fun hasGenericPermission(): Boolean {
        return !isGuestUser() && isSubscribedUser()
    }

    fun getUserId() = localStorage.readStringData(CoreConstants.USER_ID)

    fun getAuth() =
        localStorage.readStringData(CoreConstants.TOKEN_TYPE) + " " + localStorage.readStringData(
            CoreConstants.ACCESS_TOKEN
        )

    fun getAudioQuality() = localStorage.readStringData(CoreConstants.AUDIO_QUALITY)
    fun getPlayOnOffline() = localStorage.readBooleanData(CoreConstants.PLAY_ON_OFFLINE)
    fun getStreamOnlyOnOffline() = localStorage.readBooleanData(CoreConstants.STREAM_ONLY_ON_OFFLINE)
    fun getDownloadOnlyOnOffline() = localStorage.readBooleanData(CoreConstants.DOWNLOAD_ONLY_ON_OFFLINE)
    fun getRefreshToken() = localStorage.readStringData(CoreConstants.REFRESH_TOKEN)


    //Unit
    fun logOut() {
        userResponse = null
        subscription = null
        localStorage.clearPreference()
    }

}