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
    val userId: Int,
    val userName: String,
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
    user = com.example.topoclimb.data.LogUser(
        id = userId,
        name = userName,
        profilePhotoUrl = userPpUrl
    )
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
    userId = user.id,
    userName = user.name,
    userPpUrl = user.profilePhotoUrl,
    backendId = backendId
)
