package com.beat.view.authentication

import androidx.lifecycle.*
import com.beat.core.data.model.Event
import com.beat.core.data.model.LoginTokenResponse
import com.beat.core.data.model.Resource
import com.beat.core.data.rest.Repository
import com.beat.core.data.storage.PreferenceManager
import com.beat.core.utils.CoreConstants
import com.beat.util.Constants
import kotlinx.coroutines.launch
import javax.inject.Inject

class AuthenticationViewModel @Inject constructor(
    private val repository: Repository,
    private val preferenceManager: PreferenceManager
) :
    ViewModel() {

    var signInMobileNumber = MutableLiveData<String>()
    var signUpMobileNumber = MutableLiveData<String>()
    var resetMobileNumber = MutableLiveData<String>()
    var code = MutableLiveData<String>()
    var signUpCode = MutableLiveData<String>()
    var password = MutableLiveData<String>()
    var newPassword = MutableLiveData<String>()

    private var loginTokenResponse = MutableLiveData<Resource<LoginTokenResponse>>()
    private var signUpTokenResponse = MutableLiveData<Resource<LoginTokenResponse>>()
    private var initiateResetPasswordResponse = MutableLiveData<Event<Resource<String>>>()
    private var addNewUserResponse = MutableLiveData<Resource<String>>()
    private var resetPasswordResponse = MutableLiveData<Event<Resource<String>>>()

    fun loginRequest() {
        viewModelScope.launch {
            repository.getLoginToken(
                loginTokenResponse,
                CoreConstants.GRANT_TYPE_PASSWORD,
                Constants.CLIENT_ID,
                Constants.CLIENT_SECRET,
                Constants.COUNTRY_CODE + signInMobileNumber.value!!,
                password.value!!
            )
        }
    }

    fun signUpRequest() {
        viewModelScope.launch {
            repository.getSignUpToken(
                signUpTokenResponse,
                CoreConstants.GRANT_TYPE_PASSWORD,
                Constants.CLIENT_ID,
                Constants.CLIENT_SECRET,
                Constants.COUNTRY_CODE + signUpMobileNumber.value!!,
                signUpCode.value!!
            )
            print("0")
        }
        print("1")
    }

    fun addNewUserRequest() {
        viewModelScope.launch {
            repository.addNewUser(
                addNewUserResponse,
                Constants.COUNTRY_CODE + signUpMobileNumber.value!!
            )
        }
    }

    fun initiateResetPasswordRequest() {
        viewModelScope.launch {
            repository.initiateResetPassword(
                initiateResetPasswordResponse,
                Constants.COUNTRY_CODE + resetMobileNumber.value!!
            )
        }
    }

    fun resetPasswordRequest() {
        viewModelScope.launch {
            repository.resetPassword(
                resetPasswordResponse,
                Constants.COUNTRY_CODE + resetMobileNumber.value!!,
                code.value!!,
                newPassword.value!!
            )
        }
    }

    fun userOauthSuccess(loginTokenResponse: LoginTokenResponse) {
        preferenceManager.setUserOauth(loginTokenResponse)
    }

    fun getLoginResponse(): LiveData<Resource<LoginTokenResponse>> {
        return loginTokenResponse
    }

    fun getSignUpResponse(): LiveData<Resource<LoginTokenResponse>> {
        return signUpTokenResponse
    }

    fun getInitiateResetPasswordResponse(): LiveData<Event<Resource<String>>> {
        return initiateResetPasswordResponse
    }

    fun getAddNewUserResponse(): LiveData<Resource<String>> {
        return addNewUserResponse
    }

    fun getResetPasswordResponse(): LiveData<Event<Resource<String>>> {
        return resetPasswordResponse
    }

    fun checkSignUpMobileNumberValidation(): Boolean {
        return !signUpMobileNumber.value.isNullOrEmpty() && signUpMobileNumber.value!!.length >= 6
    }

    fun checkSignInMobileNumberValidation(): Boolean {
        return !signInMobileNumber.value.isNullOrEmpty() && signInMobileNumber.value!!.length >= 6
    }

    fun checkResetMobileNumberValidation(): Boolean {
        return !resetMobileNumber.value.isNullOrEmpty() && resetMobileNumber.value!!.length >= 6
    }

    fun checkPasswordValidation(): Boolean {
        return !password.value.isNullOrEmpty() && password.value!!.length >= 6
    }

    fun checkNewPasswordValidation(): Boolean {
        return !newPassword.value.isNullOrEmpty() && newPassword.value!!.length >= 6
    }

    fun checkCodeValidation(): Boolean {
        return !code.value.isNullOrEmpty() && code.value!!.length >= 4
    }

    fun checkSignUpCodeValidation(): Boolean {
        return !signUpCode.value.isNullOrEmpty() && signUpCode.value!!.length >= 4
    }

}