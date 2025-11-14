package com.example.topoclimb.cache.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.topoclimb.cache.database.StringMapConverter
import com.example.topoclimb.data.GradingSystem
import com.example.topoclimb.data.Site

@Entity(tableName = "sites")
@TypeConverters(StringMapConverter::class)
data class SiteEntity(
    @PrimaryKey val id: Int,
    val backendId: String, // To support federation
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
    val gradingSystemFree: Boolean?,
    val gradingSystemHint: String?,
    val gradingSystemPoints: Map<String, Int>?,
    val createdAt: String?,
    val updatedAt: String?,
    val email: String?,
    val phone: String?,
    val website: String?,
    val coordinates: String?,
    val cachedAt: Long // Timestamp when cached
) {
    fun toSite(): Site {
        return Site(
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
            gradingSystem = if (gradingSystemFree != null || gradingSystemHint != null || gradingSystemPoints != null) {
                GradingSystem(gradingSystemFree, gradingSystemHint, gradingSystemPoints)
            } else null,
            createdAt = createdAt,
            updatedAt = updatedAt,
            email = email,
            phone = phone,
            website = website,
            coordinates = coordinates
        )
    }

    companion object {
        fun fromSite(site: Site, backendId: String): SiteEntity {
            return SiteEntity(
                id = site.id,
                backendId = backendId,
                name = site.name,
                description = site.description,
                latitude = site.latitude,
                longitude = site.longitude,
                imageUrl = site.imageUrl,
                slug = site.slug,
                address = site.address,
                profilePicture = site.profilePicture,
                banner = site.banner,
                defaultCotation = site.defaultCotation,
                gradingSystemFree = site.gradingSystem?.free,
                gradingSystemHint = site.gradingSystem?.hint,
                gradingSystemPoints = site.gradingSystem?.points,
                createdAt = site.createdAt,
                updatedAt = site.updatedAt,
                email = site.email,
                phone = site.phone,
                website = site.website,
                coordinates = site.coordinates,
                cachedAt = System.currentTimeMillis()
            )
        }
    }
}
