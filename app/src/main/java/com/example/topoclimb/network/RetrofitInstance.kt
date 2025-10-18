package com.example.topoclimb.network

import com.example.topoclimb.AppConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (AppConfig.ENABLE_LOGGING) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }
    
    val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()
    
    val api: TopoClimbApiService by lazy {
        Retrofit.Builder()
            .baseUrl(AppConfig.API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TopoClimbApiService::class.java)
    }
}
