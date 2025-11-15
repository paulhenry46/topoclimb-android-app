package com.example.topoclimb.repository

import com.example.topoclimb.data.ApiResponse
import com.example.topoclimb.data.ApiListResponse

/**
 * Extension function to wrap API calls with error handling and unwrap ApiResponse
 */
suspend fun <T> safeApiCall(apiCall: suspend () -> ApiResponse<T>): Result<T> {
    return try {
        val response = apiCall()
        Result.success(response.data)
    } catch (e: Exception) {
        Result.failure(e)
    }
}

/**
 * Extension function to wrap API calls with error handling and unwrap ApiListResponse
 */
suspend fun <T> safeApiCallList(apiCall: suspend () -> ApiListResponse<T>): Result<List<T>> {
    return try {
        val response = apiCall()
        Result.success(response.data)
    } catch (e: Exception) {
        Result.failure(e)
    }
}

/**
 * Extension function to wrap API calls with error handling (for direct responses)
 */
suspend fun <T> safeApiCallDirect(apiCall: suspend () -> T): Result<T> {
    return try {
        Result.success(apiCall())
    } catch (e: Exception) {
        Result.failure(e)
    }
}
