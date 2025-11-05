package com.example.topoclimb.data
import com.google.gson.annotations.SerializedName

object AreaType {
    const val TRAD = "trad"
    const val BOULDERING = "bouldering"
}

data class Area(
    val id: Int,
    val name: String,
    val description: String?,
    val latitude: Double?,
    val longitude: Double?,
    val siteId: Int,
    @SerializedName("svg_schema") val svgMap: String?,
    val type: String? // "bouldering" or "trad"
)
