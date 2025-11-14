package com.example.topoclimb.cache.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.topoclimb.data.Area

@Entity(tableName = "areas")
data class AreaEntity(
    @PrimaryKey val id: Int,
    val backendId: String,
    val name: String,
    val description: String?,
    val latitude: Double?,
    val longitude: Double?,
    val siteId: Int,
    val svgMap: String?,
    val type: String?,
    val cachedAt: Long
) {
    fun toArea(): Area {
        return Area(
            id = id,
            name = name,
            description = description,
            latitude = latitude,
            longitude = longitude,
            siteId = siteId,
            svgMap = svgMap,
            type = type
        )
    }

    companion object {
        fun fromArea(area: Area, backendId: String): AreaEntity {
            return AreaEntity(
                id = area.id,
                backendId = backendId,
                name = area.name,
                description = area.description,
                latitude = area.latitude,
                longitude = area.longitude,
                siteId = area.siteId,
                svgMap = area.svgMap,
                type = area.type,
                cachedAt = System.currentTimeMillis()
            )
        }
    }
}
