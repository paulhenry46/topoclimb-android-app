package com.example.topoclimb.database.entities

import androidx.room.Entity

/**
 * Metadata entity to track which routes have had their logs fetched
 * This allows us to distinguish between "not fetched yet" and "fetched but has 0 logs"
 */
@Entity(
    tableName = "route_logs_fetch_metadata",
    primaryKeys = ["routeId", "backendId"]
)
data class RouteLogsFetchMetadataEntity(
    val routeId: Int,
    val backendId: String,
    val lastFetched: Long = System.currentTimeMillis()
)
