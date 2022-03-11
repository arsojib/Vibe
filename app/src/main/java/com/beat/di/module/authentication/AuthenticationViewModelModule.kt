package com.beat.di.module.authentication

import androidx.lifecycle.ViewModel
import com.beat.di.scope.ViewModelKey
import com.beat.view.authentication.AuthenticationViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class AuthenticationViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(AuthenticationViewModel::class)
    abstract fun bindAuthenticationViewModel(authenticationViewModel: AuthenticationViewModel): ViewModel

}