package com.example.topoclimb.data.offline

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.topoclimb.data.Area

@Entity(tableName = "offline_areas")
data class OfflineAreaEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val description: String?,
    val latitude: Double?,
    val longitude: Double?,
    val siteId: Int,
    val svgMap: String?,
    val type: String?,
    val lastSyncTimestamp: Long = System.currentTimeMillis()
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
        fun fromArea(area: Area): OfflineAreaEntity {
            return OfflineAreaEntity(
                id = area.id,
                name = area.name,
                description = area.description,
                latitude = area.latitude,
                longitude = area.longitude,
                siteId = area.siteId,
                svgMap = area.svgMap,
                type = area.type
            )
        }
    }
}
