package com.example.topoclimb.database.entities

import androidx.room.Entity
import com.example.topoclimb.data.Area

@Entity(
    tableName = "areas",
    primaryKeys = ["id", "backendId"]
)
data class AreaEntity(
    val id: Int,
    val name: String,
    val description: String?,
    val latitude: Double?,
    val longitude: Double?,
    val siteId: Int,
    val svgMap: String?,
    val type: String?,
    val backendId: String,
    val lastUpdated: Long = System.currentTimeMillis()
)

fun AreaEntity.toArea(): Area = Area(
    id = id,
    name = name,
    description = description,
    latitude = latitude,
    longitude = longitude,
    siteId = siteId,
    svgMap = svgMap,
    type = type
)

fun Area.toEntity(backendId: String): AreaEntity = AreaEntity(
    id = id,
    name = name,
    description = description,
    latitude = latitude,
    longitude = longitude,
    siteId = siteId,
    svgMap = svgMap,
    type = type,
    backendId = backendId
)
