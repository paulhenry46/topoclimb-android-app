package com.example.topoclimb.data

import androidx.compose.runtime.Stable
import com.google.gson.annotations.SerializedName

@Stable
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

@Stable
data class ContestStep(
    val id: Int,
    val name: String,
    @SerializedName("start_time") val startTime: String,
    @SerializedName("end_time") val endTime: String,
    val routes: List<Int>
)

@Stable
data class ContestStepsResponse(
    val steps: List<ContestStep>
)

@Stable
data class ContestRankEntry(
    @SerializedName("user_id") val userId: Int,
    @SerializedName("user_name") val userName: String,
    @SerializedName("routes_count") val routesCount: Int,
    @SerializedName("total_points") val totalPoints: Int,
    val rank: Int
)

@Stable
data class ContestRankResponse(
    val rank: List<ContestRankEntry>
)

@Stable
data class ContestCategory(
    val id: Int,
    val name: String,
    @SerializedName("contest_id") val contestId: Int,
    val criteria: String?,
    @SerializedName("auto_assign") val autoAssign: Boolean,
    @SerializedName("min_age") val minAge: Int?,
    @SerializedName("max_age") val maxAge: Int?,
    val gender: String?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("updated_at") val updatedAt: String?
)

@Stable
data class ContestCategoriesResponse(
    val data: List<ContestCategory>
)

/**
 * Response from /v1/current_events endpoint
 * Returns contests that are currently happening across all sites
 */
@Stable
data class CurrentEventsResponse(
    val data: List<Contest>
)
