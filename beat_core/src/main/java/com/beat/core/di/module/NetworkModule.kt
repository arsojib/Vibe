package com.beat.core.di.module

import com.beat.core.data.rest.ApiService
import com.beat.core.data.storage.PreferenceManager
import com.beat.core.di.scope.CoreScope
import com.beat.core.utils.network.ApiServiceHolder
import com.beat.core.utils.network.SupportInterceptor
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class WithOauth

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class WithoutOauth

@Module
class NetworkModule constructor(
    private val baseUrl: String,
    private val clientId: String,
    private val clientSecret: String
) {

    @CoreScope
    @WithOauth
    @Provides
    fun provideWithOauthApiService(@WithOauth retrofit: Retrofit): ApiService {
        val apiService = retrofit.create(ApiService::class.java)
        return apiService
    }

    @CoreScope
    @WithoutOauth
    @Provides
    fun provideWithoutOauthApiService(
        @WithoutOauth retrofit: Retrofit,
        apiServiceHolder: ApiServiceHolder
    ): ApiService {
        val apiService = retrofit.create(ApiService::class.java)
        apiServiceHolder.setApiService(apiService)
        return apiService
    }

    @CoreScope
    @Provides
    fun provideApiServiceHolder(): ApiServiceHolder {
        return ApiServiceHolder()
    }

    @CoreScope
    @WithOauth
    @Provides
    fun provideWithOauthRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl(baseUrl)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @CoreScope
    @WithoutOauth
    @Provides
    fun provideWithoutOauthRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @CoreScope
    @Provides
    fun provideUnsafeOkHttpClient(supportInterceptor: SupportInterceptor): OkHttpClient {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.HEADERS
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        val builder = OkHttpClient.Builder()
        builder.addInterceptor(interceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .authenticator(supportInterceptor)
            .addInterceptor(supportInterceptor)
        return builder.build()
    }

    @CoreScope
    @Provides
    fun provideSupportInterceptor(
        apiServiceHolder: ApiServiceHolder,
        preferenceManager: PreferenceManager
    ): SupportInterceptor {
        return SupportInterceptor(apiServiceHolder, preferenceManager, clientId, clientSecret)
    }

}