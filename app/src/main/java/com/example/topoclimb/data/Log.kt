package com.example.topoclimb.data

import com.google.gson.annotations.SerializedName

data class Log(
    val id: Int,
    @SerializedName("route_id")
    val routeId: Int,
    val comments: String?,
    val type: String, // e.g., "flash", "redpoint", "onsight"
    val way: String, // e.g., "bouldering", "sport", "trad"
    val grade: Int, // Grade value as numeric (e.g., 600) - format depends on the climbing system
    @SerializedName("created_at")
    val createdAt: String, // Format: 2025-10-03T10:18:24.000000Z
    @SerializedName("is_verified")
    val isVerified: Boolean,
    @SerializedName("user_name")
    val userName: String,
    @SerializedName("user_pp_url")
    val userPpUrl: String
)

data class LogsResponse(
    val data: List<Log>
)
