package com.example.topoclimb.data
import com.google.gson.annotations.SerializedName

data class Area(
    val id: Int,
    val name: String,
    val description: String?,
    val latitude: Double?,
    val longitude: Double?,
    val siteId: Int,
    @SerializedName("svg_graphic") val svgMap: String?
)
