package com.example.topoclimb.network

import com.example.topoclimb.data.BackendConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Manages multiple Retrofit instances for different backend URLs
 */
class MultiBackendRetrofitManager(
    private val loggingEnabled: Boolean = true
) {
    
    private val apiInstances = mutableMapOf<String, TopoClimbApiService>()
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (loggingEnabled) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }
    
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()
    
    /**
     * Get or create an API service for a specific backend
     */
    fun getApiService(backend: BackendConfig): TopoClimbApiService {
        return apiInstances.getOrPut(backend.id) {
            createApiService(backend.baseUrl)
        }
    }
    
    /**
     * Create a new Retrofit API service for a given base URL
     */
    private fun createApiService(baseUrl: String): TopoClimbApiService {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TopoClimbApiService::class.java)
    }
    
    /**
     * Clear cached API instances (useful when backends are updated)
     */
    fun clearCache() {
        apiInstances.clear()
    }
    
    /**
     * Remove a specific backend's API instance
     */
    fun removeBackend(backendId: String) {
        apiInstances.remove(backendId)
    }
}
