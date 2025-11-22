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
    val type: String?, // Can be null
    val way: String?, // Can be null
    val grade: Int,
    val createdAt: String?, // Can be null
    val isVerified: Boolean,
    val userName: String?, // Can be null if user is deleted
    val userPpUrl: String?, // Can be null if user has no profile picture
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
