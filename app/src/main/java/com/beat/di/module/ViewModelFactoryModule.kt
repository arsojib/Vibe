package com.beat.di.module

import androidx.lifecycle.ViewModelProvider
import com.beat.util.viewModelFactory.ViewModelProviderFactory
import dagger.Binds
import dagger.Module

@Module
abstract class ViewModelFactoryModule {

    @Binds
    abstract fun bindViewModelFactory(factory: ViewModelProviderFactory?): ViewModelProvider.Factory?

}