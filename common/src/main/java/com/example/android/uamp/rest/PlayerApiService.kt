package com.example.android.uamp.rest

import com.example.android.uamp.rest.model.StreamResponse
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

interface PlayerApiService {

    //Formats = {*/high, */normal, */low, h264/*}
    @GET("streams/online/{track_id}")
    suspend fun getOnlineStream(
        @Path(value = "track_id", encoded = true) trackId: String,
        @Header("Authorization") authorization: String,
        @Header("X-Stream-Formats") streamFormats: String
    ): Response<StreamResponse>

}

fun getRetrofit(baseUrl: String) : Retrofit {
    return Retrofit.Builder().baseUrl(baseUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
}