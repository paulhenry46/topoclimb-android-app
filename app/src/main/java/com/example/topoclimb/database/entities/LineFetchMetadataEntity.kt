package com.example.topoclimb.database.entities

import androidx.room.Entity

/**
 * Metadata entity to track which lines have been fetched
 * This allows us to distinguish between "not fetched yet" and "fetched but has 0 routes"
 */
@Entity(
    tableName = "line_fetch_metadata",
    primaryKeys = ["lineId", "backendId"]
)
data class LineFetchMetadataEntity(
    val lineId: Int,
    val backendId: String,
    val lastFetched: Long = System.currentTimeMillis()
)
