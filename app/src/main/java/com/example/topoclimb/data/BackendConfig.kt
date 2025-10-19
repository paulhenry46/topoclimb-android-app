package com.example.topoclimb.data

import com.google.gson.annotations.SerializedName
import java.util.UUID

/**
 * Configuration for a backend API endpoint
 */
data class BackendConfig(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val baseUrl: String,
    val enabled: Boolean = true,
    @SerializedName("created_at")
    val createdAt: Long = System.currentTimeMillis(),
    @SerializedName("updated_at")
    val updatedAt: Long = System.currentTimeMillis()
) {
    /**
     * Validates that the base URL is properly formatted
     */
    fun isValid(): Boolean {
        return baseUrl.isNotBlank() && 
               (baseUrl.startsWith("http://") || baseUrl.startsWith("https://")) &&
               baseUrl.endsWith("/")
    }
    
    /**
     * Creates backend metadata from this config
     */
    fun toMetadata(): BackendMetadata {
        return BackendMetadata(
            backendId = id,
            backendName = name,
            baseUrl = baseUrl
        )
    }
}
