package com.beat.view.authentication

import android.os.Bundle
import com.beat.R
import com.beat.base.BaseDaggerActivity

class AuthenticationActivity : BaseDaggerActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authentication)

        moveToFragment(LoginFragment.newInstance(), LoginFragment.toString())
    }
}
