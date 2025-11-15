package com.example.topoclimb.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.topoclimb.data.Route
import com.example.topoclimb.database.converters.StringListConverter

@Entity(tableName = "routes")
@TypeConverters(StringListConverter::class)
data class RouteEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val grade: Int?,
    val type: String?,
    val description: String?,
    val height: Int?,
    val siteId: Int,
    val siteName: String?,
    val thumbnail: String?,
    val color: String?,
    val createdAt: String?,
    val picture: String?,
    val circle: String?,
    val openers: List<String>?,
    val filteredPicture: String?,
    val tags: List<String>?,
    val numberLogs: Int?,
    val numberComments: Int?,
    val removingAt: String?,
    val backendId: String,
    val lastUpdated: Long = System.currentTimeMillis()
)

fun RouteEntity.toRoute(): Route = Route(
    id = id,
    name = name,
    grade = grade,
    type = type,
    description = description,
    height = height,
    siteId = siteId,
    siteName = siteName,
    thumbnail = thumbnail,
    color = color,
    createdAt = createdAt,
    picture = picture,
    circle = circle,
    openers = openers,
    filteredPicture = filteredPicture,
    tags = tags,
    numberLogs = numberLogs,
    numberComments = numberComments,
    removingAt = removingAt
)

fun Route.toEntity(backendId: String): RouteEntity = RouteEntity(
    id = id,
    name = name,
    grade = grade,
    type = type,
    description = description,
    height = height,
    siteId = siteId,
    siteName = siteName,
    thumbnail = thumbnail,
    color = color,
    createdAt = createdAt,
    picture = picture,
    circle = circle,
    openers = openers,
    filteredPicture = filteredPicture,
    tags = tags,
    numberLogs = numberLogs,
    numberComments = numberComments,
    removingAt = removingAt,
    backendId = backendId
)
