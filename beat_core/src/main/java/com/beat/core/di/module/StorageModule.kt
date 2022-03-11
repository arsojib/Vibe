package com.beat.core.di.module

import android.content.Context
import android.content.SharedPreferences
import com.beat.core.data.storage.DatabaseClient
import com.beat.core.data.storage.LocalStorage
import com.beat.core.data.storage.PreferenceStorage
import com.beat.core.di.scope.CoreScope
import com.beat.core.utils.CoreConstants
import dagger.Module
import dagger.Provides

@Module
class StorageModule {

    @CoreScope
    @Provides
    fun getLocalStorage(sharedPreferences: SharedPreferences): LocalStorage {
        return PreferenceStorage(sharedPreferences)
    }

    @CoreScope
    @Provides
    fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(
            CoreConstants.SHARED_PREFERENCES_NAME,
            Context.MODE_PRIVATE
        )
    }

    @CoreScope
    @Provides
    fun getAppDatabase(context: Context): DatabaseClient {
        return DatabaseClient(context)
    }

}