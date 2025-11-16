package com.example.topoclimb.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity for caching SVG map content
 * SVG maps are cached with a longer TTL (1 week) since they don't change frequently
 */
@Entity(tableName = "svg_map_cache")
data class SvgMapCacheEntity(
    @PrimaryKey
    val url: String,
    val content: String,
    val lastUpdated: Long = System.currentTimeMillis()
)
