package com.example.topoclimb.data

import com.google.gson.annotations.SerializedName

/**
 * User statistics data model
 * Note: routes_by_grade can be either an empty array [] or an object {"6a":2,"5c":2}
 */
data class UserStats(
    @SerializedName("trad_level")
    val tradLevel: String,
    @SerializedName("bouldering_level")
    val boulderingLevel: String,
    @SerializedName("total_climbed")
    val totalClimbed: Int,
    @SerializedName("routes_by_grade")
    val routesByGrade: Any? = null // Can be array or map, using Any for now
)
