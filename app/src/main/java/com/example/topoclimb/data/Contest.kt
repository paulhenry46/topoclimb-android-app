package com.example.topoclimb.data

import com.google.gson.annotations.SerializedName

data class Contest(
    val id: Int,
    val name: String,
    val description: String?,
    @SerializedName("site_id") val siteId: Int?,
    @SerializedName("start_date") val startDate: String?,
    @SerializedName("end_date") val endDate: String?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("updated_at") val updatedAt: String?
)

data class ContestStep(
    val id: Int,
    val name: String,
    @SerializedName("start_time") val startTime: String,
    @SerializedName("end_time") val endTime: String,
    val routes: List<Int>
)

data class ContestStepsResponse(
    val steps: List<ContestStep>
)

data class ContestRankEntry(
    @SerializedName("user_id") val userId: Int,
    @SerializedName("user_name") val userName: String,
    @SerializedName("routes_count") val routesCount: Int,
    @SerializedName("total_points") val totalPoints: Int,
    val rank: Int
)

data class ContestRankResponse(
    val rank: List<ContestRankEntry>
)
