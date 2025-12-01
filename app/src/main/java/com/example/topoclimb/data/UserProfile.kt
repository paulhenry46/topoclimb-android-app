package com.example.topoclimb.data

import com.google.gson.annotations.SerializedName

/**
 * User profile data model for /api/v1/users/{id} endpoint
 * This is different from the User data model because it doesn't include email
 * and other sensitive information.
 */
data class UserProfile(
    val id: Int,
    val name: String,
    @SerializedName("profile_photo_url")
    val profilePhotoUrl: String?,
    val stats: UserStats?
)

/**
 * User route log data model for /api/v1/users/{id}/routes endpoint
 * Represents a log entry for a route climbed by a user
 */
data class UserRouteLog(
    val id: Int,
    @SerializedName("route_id")
    val routeId: Int,
    val comment: String?,
    val type: String?, // e.g., "flash", "work", "view"
    val way: String?, // e.g., "bouldering", "lead", "top-rope"
    val grade: Int, // Grade value as numeric (e.g., 400)
    @SerializedName("created_at")
    val createdAt: String?, // Format: 2025-11-22T20:23:58.000000Z
    @SerializedName("is_verified")
    val isVerified: Boolean
)

/**
 * Response from /api/v1/users/{id} endpoint
 */
typealias UserProfileResponse = ApiResponse<UserProfile>

/**
 * Response from /api/v1/users/{id}/routes endpoint
 */
typealias UserRouteLogsResponse = ApiListResponse<UserRouteLog>
