package com.beat.base

import com.beat.core.di.component.CoreComponent
import com.beat.core.di.component.DaggerCoreComponent
import com.beat.core.di.module.ApplicationModule
import com.beat.core.di.module.NetworkModule
import com.beat.di.component.DaggerApplicationComponent
import com.beat.util.Constants
import com.onesignal.OneSignal
import dagger.android.AndroidInjector
import dagger.android.support.DaggerApplication

class BaseApplication : DaggerApplication() {

    private val ONESIGNAL_APP_ID = "877f4e66-c57a-4698-94a0-dd2d96ace404"

    override fun onCreate() {
        super.onCreate()

        // Enable verbose OneSignal logging to debug issues if needed.
        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE)

        // OneSignal Initialization
        OneSignal.initWithContext(this)
        OneSignal.setAppId(ONESIGNAL_APP_ID)
    }

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        val coreComponent: CoreComponent =
            DaggerCoreComponent.builder()
                .networkModule(
                    NetworkModule(
                        Constants.API_URL,
                        Constants.CLIENT_ID,
                        Constants.CLIENT_SECRET
                    )
                )
                .applicationModule(ApplicationModule(this))
                .build()

        return DaggerApplicationComponent.builder().coreComponent(coreComponent)
            .build()
    }

}

