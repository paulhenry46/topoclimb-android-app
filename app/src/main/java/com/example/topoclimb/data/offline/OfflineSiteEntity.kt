package com.example.topoclimb.data.offline

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.topoclimb.data.Site

@Entity(tableName = "offline_sites")
data class OfflineSiteEntity(
    @PrimaryKey val id: Int,
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
    val gradingSystemId: Int?,
    val gradingSystemName: String?,
    val createdAt: String?,
    val updatedAt: String?,
    val email: String?,
    val phone: String?,
    val website: String?,
    val coordinates: String?,
    val backendId: String,
    val backendName: String,
    val lastSyncTimestamp: Long = System.currentTimeMillis()
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
            gradingSystem = if (gradingSystemId != null && gradingSystemName != null) {
                com.example.topoclimb.data.GradingSystem(
                    free = null,
                    hint = gradingSystemName,
                    points = null
                )
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
        fun fromSite(site: Site, backendId: String, backendName: String): OfflineSiteEntity {
            return OfflineSiteEntity(
                id = site.id,
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
                gradingSystemId = site.gradingSystem?.free?.let { if (it) 1 else 0 },
                gradingSystemName = site.gradingSystem?.hint,
                createdAt = site.createdAt,
                updatedAt = site.updatedAt,
                email = site.email,
                phone = site.phone,
                website = site.website,
                coordinates = site.coordinates,
                backendId = backendId,
                backendName = backendName
            )
        }
    }
}
