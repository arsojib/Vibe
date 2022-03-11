package com.beat.core.data.storage

import com.beat.core.data.model.LoginTokenResponse

interface LocalStorage {

    //Write data function
    fun writeUserOauthData(loginTokenResponse: LoginTokenResponse)
    fun writeGuestOauthData(loginTokenResponse: LoginTokenResponse)

    fun writeStringData(key: String, value: String)

    fun writeBooleanData(key: String, value: Boolean)

    fun writeIntData(key: String, value: Int)


    //Read data function
    fun readStringData(key: String): String

    fun readBooleanData(key: String): Boolean

    fun readIntData(key: String): Int

    fun clearPreference()
}