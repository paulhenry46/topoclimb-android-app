package com.example.topoclimb.data

import com.google.gson.annotations.SerializedName

/**
 * Represents schema information for a sector
 * Contains the background image and SVG paths overlay for route visualization
 */
data class SectorSchema(
    val id: Int,           // Sector ID
    val name: String,      // Sector name
    val paths: String?,    // URL to SVG file with route paths
    val bg: String?        // URL to background image
)
