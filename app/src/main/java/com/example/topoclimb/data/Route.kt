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
    @SerializedName("created_at")
    val createdAt: String?, // Format: 2025-10-08T12:18:41.000000Z
    val picture: String?, // URL of the full route picture
    val circle: String?, // URL of the SVG circle overlay
    val openers: String? // Names of route openers
)

/**
 * Route with additional display metadata (line/sector info)
 * Used when displaying routes that have been filtered by sector/line
 */
data class RouteWithMetadata(
    val route: Route,
    val lineLocalId: String? = null,
    val sectorLocalId: String? = null,
    val lineCount: Int? = null
) {
    // Delegate all Route properties for easy access
    val id: Int get() = route.id
    val name: String get() = route.name
    val grade: String? get() = route.grade
    val type: String? get() = route.type
    val description: String? get() = route.description
    val height: Int? get() = route.height
    val siteId: Int get() = route.siteId
    val siteName: String? get() = route.siteName
    val thumbnail: String? get() = route.thumbnail
    val color: String? get() = route.color
    val createdAt: String? get() = route.createdAt
    val picture: String? get() = route.picture
    val circle: String? get() = route.circle
    val openers: String? get() = route.openers
}
