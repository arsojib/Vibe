package com.beat.di.module.authentication

import com.beat.view.authentication.LoginFragment
import com.beat.view.authentication.SignUpFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class AuthenticationFragmentBuildersModule {

    @ContributesAndroidInjector
    abstract fun contributeLoginFragment(): LoginFragment

    @ContributesAndroidInjector
    abstract fun contributeSignUpFragment(): SignUpFragment

}