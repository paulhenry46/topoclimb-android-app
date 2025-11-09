package com.example.topoclimb.data

import com.google.gson.annotations.SerializedName
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.annotations.JsonAdapter
import java.lang.reflect.Type

/**
 * Custom deserializer for routes_by_grade field that handles both empty array [] and object {}
 */
class RoutesByGradeDeserializer : JsonDeserializer<Map<String, Int>> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): Map<String, Int> {
        return when {
            json.isJsonArray -> emptyMap() // Handle empty array case
            json.isJsonObject -> {
                val map = mutableMapOf<String, Int>()
                json.asJsonObject.entrySet().forEach { entry ->
                    map[entry.key] = entry.value.asInt
                }
                map
            }
            else -> emptyMap()
        }
    }
}

/**
 * User statistics data model
 */
data class UserStats(
    @SerializedName("trad_level")
    val tradLevel: String?, // e.g., "6a"
    @SerializedName("bouldering_level")
    val boulderingLevel: String?, // e.g., "3c"
    @SerializedName("total_climbed")
    val totalClimbed: Int, // Total number of routes climbed
    @SerializedName("routes_by_grade")
    @JsonAdapter(RoutesByGradeDeserializer::class)
    val routesByGrade: Map<String, Int> // e.g., {"6a": 2, "5c": 2, "4a": 1} or []
)

/**
 * Response from /user/stats endpoint
 */
data class UserStatsResponse(
    val data: UserStats
)

/**
 * Request body for updating user information
 */
data class UserUpdateRequest(
    val name: String? = null, // Optional: max 255 characters
    @SerializedName("birth_date")
    val birthDate: String? = null, // Optional: date format
    val gender: String? = null // Optional: "male", "female", or "other"
)

/**
 * Response from /user/update endpoint
 */
data class UserUpdateResponse(
    val data: User
)
