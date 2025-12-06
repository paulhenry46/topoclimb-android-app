package com.example.topoclimb.data

import androidx.compose.runtime.Stable
import com.google.gson.annotations.SerializedName

@Stable
data class Route(
    val id: Int,
    val name: String,
    val grade: Int?, // Grade as integer (300-950)
    val type: String?, // e.g., "sport", "trad", "boulder"
    val description: String?,
    val height: Int?,
    @SerializedName("site_id")
    val siteId: Int,
    @SerializedName("site_name")
    val siteName: String?,
    val thumbnail: String?, // URL of the route thumbnail
    val color: String?, // Hex color code for the route
    @SerializedName("created_at")
    val createdAt: String?, // Format: 2025-10-08T12:18:41.000000Z
    val picture: String?, // URL of the full route picture
    val circle: String?, // URL of the SVG circle overlay
    val openers: List<String>?, // Names of route openers
    @SerializedName("filtered_picture")
    val filteredPicture: String?, // URL of the filtered/focused route picture
    val tags: List<String>?, // Tags for the route
    @SerializedName("number_logs")
    val numberLogs: Int? = 0, // Number of logs for this route
    @SerializedName("number_comments")
    val numberComments: Int? = 0, // Number of comments on this route
    @SerializedName("removing_at")
    val removingAt: String? = null // Date when route will be removed (format: 2025-09-06 00:00:00, 2025-09-06, or null)
)

/**
 * Route with additional display metadata (line/sector info)
 * Used when displaying routes that have been filtered by sector/line
 */
@Stable
data class RouteWithMetadata(
    val route: Route,
    val lineLocalId: String? = null,
    val sectorLocalId: String? = null,
    val lineCount: Int? = null
) {
    // Delegate all Route properties for easy access
    val id: Int get() = route.id
    val name: String get() = route.name
    val grade: Int? get() = route.grade
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
    val openers: List<String>? get() = route.openers
    val filteredPicture: String? get() = route.filteredPicture
    val tags: List<String>? get() = route.tags
    val numberLogs: Int? get() = route.numberLogs
    val numberComments: Int? get() = route.numberComments
    val removingAt: String? get() = route.removingAt
}
