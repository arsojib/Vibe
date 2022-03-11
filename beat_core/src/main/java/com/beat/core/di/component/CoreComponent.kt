package com.beat.core.di.component

import android.content.Context
import com.beat.core.data.rest.ApiService
import com.beat.core.data.storage.OfflineDownloadConnection
import com.beat.core.data.storage.PreferenceManager
import com.beat.core.di.module.*
import com.beat.core.di.scope.CoreScope
import com.beat.core.utils.videoPlayerInstance.VideoPlayerInstance
import dagger.Component

@CoreScope
@Component(modules = [ApplicationModule::class, NetworkModule::class, StorageModule::class])
interface CoreComponent {
    fun context(): Context
    fun videoPlayerInstance(): VideoPlayerInstance
    fun offlineDownloadConnection(): OfflineDownloadConnection
    fun preferenceManager(): PreferenceManager

    @WithOauth
    fun withOauthApiService(): ApiService

    @WithoutOauth
    fun withoutOauthApiService(): ApiService
}