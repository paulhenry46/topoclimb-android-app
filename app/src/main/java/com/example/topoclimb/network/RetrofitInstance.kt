package com.example.topoclimb.network

import android.content.Context
import com.example.topoclimb.AppConfig
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit

object RetrofitInstance {
    
    private var cache: Cache? = null
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (AppConfig.ENABLE_LOGGING) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }
    
    fun initializeCache(context: Context) {
        val cacheSize = 10 * 1024 * 1024L // 10 MB
        val cacheDir = File(context.cacheDir, "http_cache")
        cache = Cache(cacheDir, cacheSize)
    }
    
    val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .cache(cache)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }
    
    val api: TopoClimbApiService by lazy {
        Retrofit.Builder()
            .baseUrl(AppConfig.API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TopoClimbApiService::class.java)
    }
}
