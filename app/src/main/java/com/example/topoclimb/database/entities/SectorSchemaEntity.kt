package com.example.topoclimb.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.topoclimb.data.SectorSchema

/**
 * Entity for caching sector schema metadata
 * Stores the schema information (id, name, paths URL, bg URL) for offline access
 */
@Entity(
    tableName = "sector_schemas",
    primaryKeys = ["id", "backendId"]
)
data class SectorSchemaEntity(
    val id: Int,
    val name: String,
    val paths: String?,
    val bg: String?,
    val areaId: Int,
    val backendId: String,
    val lastUpdated: Long = System.currentTimeMillis()
)

// Extension functions for converting between SectorSchema and SectorSchemaEntity
fun SectorSchema.toEntity(backendId: String, areaId: Int): SectorSchemaEntity {
    return SectorSchemaEntity(
        id = this.id,
        name = this.name,
        paths = this.paths,
        bg = this.bg,
        areaId = areaId,
        backendId = backendId,
        lastUpdated = System.currentTimeMillis()
    )
}

fun SectorSchemaEntity.toSectorSchema(): SectorSchema {
    return SectorSchema(
        id = this.id,
        name = this.name,
        paths = this.paths,
        bg = this.bg
    )
}
