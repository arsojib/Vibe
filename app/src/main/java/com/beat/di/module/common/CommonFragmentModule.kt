package com.beat.di.module.common

import com.beat.view.common.setting.SettingFragment
import com.beat.view.common.subscribtion.SubscriptionFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class CommonFragmentModule {

    @ContributesAndroidInjector
    abstract fun contributeSubscriptionFragment(): SubscriptionFragment

    @ContributesAndroidInjector
    abstract fun contributeSettingFragment(): SettingFragment

}