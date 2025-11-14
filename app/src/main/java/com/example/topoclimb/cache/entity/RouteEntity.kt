package com.example.topoclimb.cache.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.topoclimb.cache.database.StringListConverter
import com.example.topoclimb.data.Route

@Entity(tableName = "routes")
@TypeConverters(StringListConverter::class)
data class RouteEntity(
    @PrimaryKey val id: Int,
    val backendId: String,
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
    val tags: List<String>?,
    val numberLogs: Int?,
    val numberComments: Int?,
    val removingAt: String?,
    val cachedAt: Long
) {
    fun toRoute(): Route {
        return Route(
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
            filteredPicture = null, // Not cached per requirements
            tags = tags,
            numberLogs = numberLogs,
            numberComments = numberComments,
            removingAt = removingAt
        )
    }

    companion object {
        fun fromRoute(route: Route, backendId: String): RouteEntity {
            return RouteEntity(
                id = route.id,
                backendId = backendId,
                name = route.name,
                grade = route.grade,
                type = route.type,
                description = route.description,
                height = route.height,
                siteId = route.siteId,
                siteName = route.siteName,
                thumbnail = route.thumbnail,
                color = route.color,
                createdAt = route.createdAt,
                picture = route.picture,
                circle = route.circle,
                openers = route.openers,
                tags = route.tags,
                numberLogs = route.numberLogs,
                numberComments = route.numberComments,
                removingAt = route.removingAt,
                cachedAt = System.currentTimeMillis()
            )
        }
    }
}
