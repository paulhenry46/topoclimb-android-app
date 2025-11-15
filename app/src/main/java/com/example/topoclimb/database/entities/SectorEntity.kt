package com.example.topoclimb.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.topoclimb.data.Sector

@Entity(
    tableName = "sectors",
    primaryKeys = ["id", "backendId"]
)
data class SectorEntity(
    val id: Int,
    val name: String,
    val description: String?,
    val areaId: Int,
    val localId: String?,
    val backendId: String,
    val lastUpdated: Long = System.currentTimeMillis()
)

// Extension functions for converting between Sector and SectorEntity
fun Sector.toEntity(backendId: String): SectorEntity {
    return SectorEntity(
        id = this.id,
        name = this.name,
        description = this.description,
        areaId = this.areaId,
        localId = this.localId,
        backendId = backendId,
        lastUpdated = System.currentTimeMillis()
    )
}

fun SectorEntity.toSector(): Sector {
    return Sector(
        id = this.id,
        name = this.name,
        description = this.description,
        areaId = this.areaId,
        localId = this.localId
    )
}
