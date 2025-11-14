package com.example.topoclimb.cache.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.topoclimb.data.Line

@Entity(tableName = "lines")
data class LineEntity(
    @PrimaryKey val id: Int,
    val backendId: String,
    val name: String,
    val description: String?,
    val sectorId: Int,
    val localId: String?,
    val cachedAt: Long
) {
    fun toLine(): Line {
        return Line(
            id = id,
            name = name,
            description = description,
            sectorId = sectorId,
            localId = localId
        )
    }

    companion object {
        fun fromLine(line: Line, backendId: String): LineEntity {
            return LineEntity(
                id = line.id,
                backendId = backendId,
                name = line.name,
                description = line.description,
                sectorId = line.sectorId,
                localId = line.localId,
                cachedAt = System.currentTimeMillis()
            )
        }
    }
}
