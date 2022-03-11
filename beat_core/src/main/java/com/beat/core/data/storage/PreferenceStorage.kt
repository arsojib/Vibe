package com.beat.core.data.storage

import android.content.SharedPreferences
import com.beat.core.data.model.LoginTokenResponse
import com.beat.core.utils.CoreConstants

class PreferenceStorage constructor(
    private val sharedPreferences: SharedPreferences
) : LocalStorage {

    override fun writeUserOauthData(loginTokenResponse: LoginTokenResponse) {
        val editor = sharedPreferences.edit()
        editor.putString(CoreConstants.ACCESS_TOKEN, loginTokenResponse.access_token)
        editor.putInt(CoreConstants.EXPIRE_IN, loginTokenResponse.expires_in)
        editor.putString(CoreConstants.TOKEN_TYPE, loginTokenResponse.token_type)
        editor.putString(CoreConstants.REFRESH_TOKEN, loginTokenResponse.refresh_token ?: "")
        editor.putString(CoreConstants.USER_ID, loginTokenResponse.user_id ?: "")
        editor.apply()
    }

    override fun writeGuestOauthData(loginTokenResponse: LoginTokenResponse) {
        val editor = sharedPreferences.edit()
        editor.putString(CoreConstants.ACCESS_TOKEN, loginTokenResponse.access_token)
        editor.putInt(CoreConstants.EXPIRE_IN, loginTokenResponse.expires_in)
        editor.putString(CoreConstants.TOKEN_TYPE, loginTokenResponse.token_type)
        editor.apply()
    }

    override fun writeStringData(key: String, value: String) {
        sharedPreferences.edit().putString(key, value).apply()
    }

    override fun writeBooleanData(key: String, value: Boolean) {
        sharedPreferences.edit().putBoolean(key, value).apply()
    }

    override fun writeIntData(key: String, value: Int) {
        sharedPreferences.edit().putInt(key, value).apply()
    }

    override fun readStringData(key: String): String {
        return sharedPreferences.getString(key, "") ?: ""
    }

    override fun readBooleanData(key: String): Boolean {
        return sharedPreferences.getBoolean(key, false)
    }

    override fun readIntData(key: String): Int {
        return sharedPreferences.getInt(key, -1)
    }

    override fun clearPreference() {
        sharedPreferences.edit().clear().apply()
    }

}