package com.example.topoclimb.cache.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.topoclimb.data.SectorSchema

@Entity(tableName = "sector_schemas")
data class SectorSchemaEntity(
    @PrimaryKey val id: Int,
    val backendId: String,
    val name: String,
    val paths: String?,
    val bg: String?,
    val areaId: Int, // For querying by area
    val cachedAt: Long
) {
    fun toSectorSchema(): SectorSchema {
        return SectorSchema(
            id = id,
            name = name,
            paths = paths,
            bg = bg
        )
    }

    companion object {
        fun fromSectorSchema(schema: SectorSchema, backendId: String, areaId: Int): SectorSchemaEntity {
            return SectorSchemaEntity(
                id = schema.id,
                backendId = backendId,
                name = schema.name,
                paths = schema.paths,
                bg = schema.bg,
                areaId = areaId,
                cachedAt = System.currentTimeMillis()
            )
        }
    }
}
