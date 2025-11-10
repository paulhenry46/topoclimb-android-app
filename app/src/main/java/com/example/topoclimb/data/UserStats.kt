package com.example.topoclimb.data

import com.google.gson.annotations.SerializedName

/**
 * User statistics data model
 */
data class UserStats(
    @SerializedName("trad_level")
    val tradLevel: String,
    @SerializedName("bouldering_level")
    val boulderingLevel: String,
    @SerializedName("total_climbed")
    val totalClimbed: Int,
    @SerializedName("routes_by_grade")
    val routesByGrade: Map<String, Int>? = null
)

/**
 * Response from /user/stats endpoint
 */
data class UserStatsResponse(
    val data: UserStats
)
