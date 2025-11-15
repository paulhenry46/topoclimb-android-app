package com.example.topoclimb.database.entities

import androidx.room.Entity
import com.example.topoclimb.data.Contest

@Entity(
    tableName = "contests",
    primaryKeys = ["id", "backendId"]
)
data class ContestEntity(
    val id: Int,
    val name: String,
    val description: String?,
    val siteId: Int?,
    val startDate: String?,
    val endDate: String?,
    val createdAt: String?,
    val updatedAt: String?,
    val backendId: String,
    val lastUpdated: Long = System.currentTimeMillis()
)

fun ContestEntity.toContest(): Contest = Contest(
    id = id,
    name = name,
    description = description,
    siteId = siteId,
    startDate = startDate,
    endDate = endDate,
    createdAt = createdAt,
    updatedAt = updatedAt
)

/**
 * Convert Contest to ContestEntity with explicit siteId override.
 * Used when the API returns incorrect siteId but we know the correct siteId from context.
 */
fun Contest.toEntity(backendId: String, correctSiteId: Int): ContestEntity = ContestEntity(
    id = id,
    name = name,
    description = description,
    siteId = correctSiteId,
    startDate = startDate,
    endDate = endDate,
    createdAt = createdAt,
    updatedAt = updatedAt,
    backendId = backendId
)
