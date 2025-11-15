package com.example.topoclimb.database.entities

import androidx.room.Entity
import androidx.room.TypeConverters
import com.example.topoclimb.data.GradingSystem
import com.example.topoclimb.data.Site
import com.example.topoclimb.database.converters.GradingSystemConverter

@Entity(
    tableName = "sites",
    primaryKeys = ["id", "backendId"]
)
@TypeConverters(GradingSystemConverter::class)
data class SiteEntity(
    val id: Int,
    val name: String,
    val description: String?,
    val latitude: Double?,
    val longitude: Double?,
    val imageUrl: String?,
    val slug: String,
    val address: String?,
    val profilePicture: String?,
    val banner: String?,
    val defaultCotation: Boolean?,
    val gradingSystem: GradingSystem?,
    val createdAt: String?,
    val updatedAt: String?,
    val email: String?,
    val phone: String?,
    val website: String?,
    val coordinates: String?,
    val backendId: String,
    val lastUpdated: Long = System.currentTimeMillis()
)

fun SiteEntity.toSite(): Site = Site(
    id = id,
    name = name,
    description = description,
    latitude = latitude,
    longitude = longitude,
    imageUrl = imageUrl,
    slug = slug,
    address = address,
    profilePicture = profilePicture,
    banner = banner,
    defaultCotation = defaultCotation,
    gradingSystem = gradingSystem,
    createdAt = createdAt,
    updatedAt = updatedAt,
    email = email,
    phone = phone,
    website = website,
    coordinates = coordinates
)

fun Site.toEntity(backendId: String): SiteEntity = SiteEntity(
    id = id,
    name = name,
    description = description,
    latitude = latitude,
    longitude = longitude,
    imageUrl = imageUrl,
    slug = slug,
    address = address,
    profilePicture = profilePicture,
    banner = banner,
    defaultCotation = defaultCotation,
    gradingSystem = gradingSystem,
    createdAt = createdAt,
    updatedAt = updatedAt,
    email = email,
    phone = phone,
    website = website,
    coordinates = coordinates,
    backendId = backendId
)
