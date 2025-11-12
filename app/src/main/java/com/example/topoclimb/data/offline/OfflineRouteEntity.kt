package com.example.topoclimb.data.offline

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.topoclimb.data.Route

@Entity(tableName = "offline_routes")
data class OfflineRouteEntity(
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
    val openers: String?, // Stored as JSON string
    val filteredPicture: String?,
    val tags: String?, // Stored as JSON string
    val numberLogs: Int?,
    val numberComments: Int?,
    val removingAt: String?,
    val lastSyncTimestamp: Long = System.currentTimeMillis()
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
            openers = openers?.split(",")?.filter { it.isNotEmpty() },
            filteredPicture = filteredPicture,
            tags = tags?.split(",")?.filter { it.isNotEmpty() },
            numberLogs = numberLogs,
            numberComments = numberComments,
            removingAt = removingAt
        )
    }
    
    companion object {
        fun fromRoute(route: Route): OfflineRouteEntity {
            return OfflineRouteEntity(
                id = route.id,
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
                openers = route.openers?.joinToString(","),
                filteredPicture = route.filteredPicture,
                tags = route.tags?.joinToString(","),
                numberLogs = route.numberLogs,
                numberComments = route.numberComments,
                removingAt = route.removingAt
            )
        }
    }
}
