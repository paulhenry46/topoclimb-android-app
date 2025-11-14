package com.example.topoclimb.cache.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.topoclimb.data.Sector

@Entity(tableName = "sectors")
data class SectorEntity(
    @PrimaryKey val id: Int,
    val backendId: String,
    val name: String,
    val description: String?,
    val areaId: Int,
    val localId: String?,
    val cachedAt: Long
) {
    fun toSector(): Sector {
        return Sector(
            id = id,
            name = name,
            description = description,
            areaId = areaId,
            localId = localId
        )
    }

    companion object {
        fun fromSector(sector: Sector, backendId: String): SectorEntity {
            return SectorEntity(
                id = sector.id,
                backendId = backendId,
                name = sector.name,
                description = sector.description,
                areaId = sector.areaId,
                localId = sector.localId,
                cachedAt = System.currentTimeMillis()
            )
        }
    }
}
