package com.beat.di.component

import com.beat.core.di.component.CoreComponent
import com.beat.di.module.ActivityModule
import com.beat.di.module.ViewModelFactoryModule
import com.beat.di.scope.VibeScope
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import dagger.android.support.DaggerApplication

@VibeScope
@Component(
    modules = [AndroidSupportInjectionModule::class, ActivityModule::class],
    dependencies = [CoreComponent::class]
)
interface ApplicationComponent : AndroidInjector<DaggerApplication> {

}