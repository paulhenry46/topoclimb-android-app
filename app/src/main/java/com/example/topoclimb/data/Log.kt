package com.example.topoclimb.data

import com.google.gson.annotations.SerializedName

/**
 * User information in log entries
 */
data class LogUser(
    val id: Int,
    val name: String,
    @SerializedName("profile_photo_url")
    val profilePhotoUrl: String?
)

data class Log(
    val id: Int,
    @SerializedName("route_id")
    val routeId: Int,
    @SerializedName("comment")
    val comments: String?,
    val type: String?, // e.g., "flash", "redpoint", "onsight" - can be null
    val way: String?, // e.g., "bouldering", "sport", "trad" - can be null
    val grade: Int, // Grade value as numeric (e.g., 600) - format depends on the climbing system
    @SerializedName("created_at")
    val createdAt: String?, // Format: 2025-10-03T10:18:24.000000Z - can be null
    @SerializedName("is_verified")
    val isVerified: Boolean,
    val user: LogUser
) {
    // Convenience properties for backward compatibility
    val userName: String get() = user.name
    val userPpUrl: String? get() = user.profilePhotoUrl
}

/**
 * Request body for creating a new log
 */
data class CreateLogRequest(
    val grade: Int, // Difficulty grade value (300-950)
    val type: String, // Type of ascent: "work", "flash", "view"
    val way: String, // Climbing style: "top-rope", "lead", "bouldering"
    val comment: String? = null, // Optional comments (max 1000 chars)
    @SerializedName("video_url")
    val videoUrl: String? = null // Optional video URL (max 255 chars)
)

/**
 * Response from creating a log
 */
data class CreateLogResponse(
    val data: Log
)

/**
 * Response from user/logs endpoint containing route IDs the user has logged
 */
data class UserLogsResponse(
    val data: List<Int> // List of route IDs that the user has logged
)
