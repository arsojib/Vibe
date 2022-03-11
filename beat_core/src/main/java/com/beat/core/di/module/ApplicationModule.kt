package com.beat.core.di.module

import android.content.Context
import com.beat.core.di.scope.CoreScope
import dagger.Module
import dagger.Provides

@Module
class ApplicationModule(private val context: Context) {

    @CoreScope
    @Provides
    fun provideContext(): Context {
        return context
    }

}