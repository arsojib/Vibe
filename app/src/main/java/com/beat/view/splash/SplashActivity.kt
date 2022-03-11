package com.beat.view.splash

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.beat.R
import com.beat.base.BaseDaggerActivity
import com.beat.core.data.model.LoginTokenResponse
import com.beat.core.data.model.Resource
import com.beat.util.viewModelFactory.ViewModelProviderFactory
import com.beat.view.main.MainActivity
import com.beat.view.authentication.AuthenticationActivity
import javax.inject.Inject

class SplashActivity : BaseDaggerActivity() {

    @Inject
    lateinit var providerFactory: ViewModelProviderFactory

    private lateinit var splashViewModel: SplashViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        splashViewModel = ViewModelProvider(this, providerFactory).get(SplashViewModel::class.java)

        Handler(Looper.getMainLooper()).postDelayed({
//            gotoAuthentication()
            initialComponent()
        }, 3000)
    }

    private fun initialComponent() {
        splashViewModel.getGuestTokenResponse()
            .observe(this, Observer<Resource<LoginTokenResponse>> {
                it?.let { resource ->
                    when (resource.status) {
                        Resource.Status.SUCCESS -> {
                            splashViewModel.guestOauthSuccess(resource.data!!)
                            startActivity(Intent(this, AuthenticationActivity::class.java))
//                        startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        }
                        Resource.Status.ERROR -> {
                            startActivity(Intent(this, AuthenticationActivity::class.java))
                            finish()
                            showToast(
                                resource.message!!,
                                Toast.LENGTH_LONG
                            )
                        }
                        Resource.Status.LOADING -> {

                        }
                    }
                }
            })

        splashViewModel.getRefreshTokenResponse()
            .observe(this, Observer<Resource<LoginTokenResponse>> {
                it?.let { resource ->
                    when (resource.status) {
                        Resource.Status.SUCCESS -> {
                            splashViewModel.guestOauthSuccess(resource.data!!)
                            moveToMainActivity();
                        }
                        Resource.Status.ERROR -> {
                            //Refresh token api return 400 for invalid token or bad request and 401 for unauthorized request
                            //Return to authentication process if one of this code matched
                            if (resource.code!! == 400 || resource.code!! == 401) {
                                startActivity(Intent(this, AuthenticationActivity::class.java))
                                finish()
                                showToast(
                                    resource.message!!,
                                    Toast.LENGTH_LONG
                                )
                            } else {
                                startActivity(Intent(this, MainActivity::class.java))
                                finish()
                            }
                        }
                        Resource.Status.LOADING -> {

                        }
                    }
                }
            })

        splashViewModel.checkUserLoggedIn()
    }

    private fun moveToMainActivity() {
        val appLinkAction: String? = intent?.action
        val appLinkData: Uri? = intent?.data
        val intent = Intent(this, MainActivity::class.java)
        if (appLinkAction != null && appLinkData != null) {
            intent.action = appLinkAction
            intent.data = appLinkData
        }
        startActivity(intent)
        finish()
    }

    //Temporary function to avoid generating guest token randomly
    private fun gotoAuthentication() {
//        splashViewModel.setWrongToken()
//        startActivity(Intent(this, AuthenticationActivity::class.java))
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

}
