package com.example.topoclimb.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.topoclimb.data.Line

@Entity(
    tableName = "lines",
    primaryKeys = ["id", "backendId"]
)
data class LineEntity(
    val id: Int,
    val name: String?,  // Made nullable to handle API responses
    val description: String?,
    val sectorId: Int,
    val localId: String?,
    val backendId: String,
    val lastUpdated: Long = System.currentTimeMillis()
)

// Extension functions for converting between Line and LineEntity
fun Line.toEntity(backendId: String, correctSectorId: Int? = null): LineEntity {
    return LineEntity(
        id = this.id,
        name = this.name,
        description = this.description,
        sectorId = correctSectorId ?: this.sectorId,  // Override with correct sectorId if provided
        localId = this.localId,
        backendId = backendId,
        lastUpdated = System.currentTimeMillis()
    )
}

fun LineEntity.toLine(): Line {
    return Line(
        id = this.id,
        name = this.name ?: "",  // Provide default empty string if null
        description = this.description,
        sectorId = this.sectorId,
        localId = this.localId
    )
}
