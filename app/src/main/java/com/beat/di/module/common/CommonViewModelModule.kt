package com.beat.di.module.common

import androidx.lifecycle.ViewModel
import com.beat.di.scope.ViewModelKey
import com.beat.view.common.setting.SettingViewModel
import com.beat.view.common.subscribtion.SubscriptionViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class CommonViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(SubscriptionViewModel::class)
    abstract fun bindSubscriptionViewModel(subscriptionViewModel: SubscriptionViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SettingViewModel::class)
    abstract fun bindSettingViewModel(settingViewModel: SettingViewModel): ViewModel

}