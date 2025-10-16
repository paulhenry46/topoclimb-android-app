package com.example.topoclimb.data

import com.google.gson.annotations.SerializedName

data class Route(
    val id: Int,
    val name: String,
    val grade: String?,
    val type: String?, // e.g., "sport", "trad", "boulder"
    val description: String?,
    val height: Int?,
    val siteId: Int,
    val siteName: String?,
    val thumbnail: String?, // URL of the route thumbnail
    val color: String?, // Hex color code for the route
    @SerializedName("line_local_id")
    val lineLocalId: String?, // Local ID of the line
    @SerializedName("sector_local_id")
    val sectorLocalId: String?, // Local ID of the sector
    @SerializedName("line_count")
    val lineCount: Int? // Number of lines in the sector (to determine which ID to show)
)
