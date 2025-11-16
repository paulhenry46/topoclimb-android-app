package com.example.topoclimb.database.entities

import androidx.room.Entity
import com.example.topoclimb.data.Log

/**
 * Entity for caching route logs
 */
@Entity(
    tableName = "logs",
    primaryKeys = ["id", "backendId"]
)
data class LogEntity(
    val id: Int,
    val routeId: Int,
    val comments: String?,
    val type: String,
    val way: String,
    val grade: Int,
    val createdAt: String,
    val isVerified: Boolean,
    val userName: String,
    val userPpUrl: String,
    val backendId: String,
    val lastUpdated: Long = System.currentTimeMillis()
)

fun LogEntity.toLog(): Log = Log(
    id = id,
    routeId = routeId,
    comments = comments,
    type = type,
    way = way,
    grade = grade,
    createdAt = createdAt,
    isVerified = isVerified,
    userName = userName,
    userPpUrl = userPpUrl
)

fun Log.toEntity(backendId: String): LogEntity = LogEntity(
    id = id,
    routeId = routeId,
    comments = comments,
    type = type,
    way = way,
    grade = grade,
    createdAt = createdAt,
    isVerified = isVerified,
    userName = userName,
    userPpUrl = userPpUrl,
    backendId = backendId
)
