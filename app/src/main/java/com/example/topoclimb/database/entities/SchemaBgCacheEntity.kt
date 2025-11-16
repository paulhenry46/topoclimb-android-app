package com.example.topoclimb.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity for caching schema background images
 * Background images are cached with a 2-week TTL since they don't change frequently
 */
@Entity(tableName = "schema_bg_cache")
data class SchemaBgCacheEntity(
    @PrimaryKey
    val url: String,
    val content: String,  // Base64-encoded image data with data URI prefix
    val lastUpdated: Long = System.currentTimeMillis()
)
