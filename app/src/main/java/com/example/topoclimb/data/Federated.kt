package com.example.topoclimb.data

import androidx.compose.runtime.Stable

/**
 * Backend metadata that identifies the source backend for a federated resource
 */
@Stable
data class BackendMetadata(
    val backendId: String,
    val backendName: String,
    val baseUrl: String
)

/**
 * Wrapper class for resources fetched from federated backends
 * Wraps any resource type T with backend metadata
 */
@Stable
data class Federated<T>(
    val data: T,
    val backend: BackendMetadata
) {
    /**
     * Unique identifier combining backend ID and resource ID
     * Format: "backendId:resourceId"
     */
    fun getGlobalId(resourceId: Any): String {
        return "${backend.backendId}:$resourceId"
    }
}
