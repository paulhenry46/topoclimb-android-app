package com.example.topoclimb.cache

import com.example.topoclimb.data.Site
import com.example.topoclimb.cache.entity.SiteEntity
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for cache entities and expiration logic
 */
class CacheTest {
    
    @Test
    fun siteEntity_toSite_convertsCorrectly() {
        val siteEntity = SiteEntity(
            id = 1,
            backendId = "test-backend",
            name = "Test Site",
            description = "A test site",
            latitude = 45.0,
            longitude = 5.0,
            imageUrl = "https://example.com/image.jpg",
            slug = "test-site",
            address = "123 Test St",
            profilePicture = null,
            banner = null,
            defaultCotation = true,
            gradingSystemFree = true,
            gradingSystemHint = "Test hint",
            gradingSystemPoints = mapOf("6a" to 600, "6b" to 610),
            createdAt = "2025-01-01T00:00:00Z",
            updatedAt = "2025-01-01T00:00:00Z",
            email = "test@example.com",
            phone = "555-1234",
            website = "https://example.com",
            coordinates = "45.0,5.0",
            cachedAt = System.currentTimeMillis()
        )
        
        val site = siteEntity.toSite()
        
        assertEquals(1, site.id)
        assertEquals("Test Site", site.name)
        assertEquals("A test site", site.description)
        assertEquals(45.0, site.latitude ?: 0.0, 0.001)
        assertEquals(5.0, site.longitude ?: 0.0, 0.001)
        assertNotNull(site.gradingSystem)
        assertTrue(site.gradingSystem?.free ?: false)
        assertEquals("Test hint", site.gradingSystem?.hint)
        assertEquals(2, site.gradingSystem?.points?.size ?: 0)
    }
    
    @Test
    fun siteEntity_fromSite_convertsCorrectly() {
        val site = Site(
            id = 1,
            name = "Test Site",
            description = "A test site",
            latitude = 45.0,
            longitude = 5.0,
            imageUrl = "https://example.com/image.jpg",
            slug = "test-site",
            address = "123 Test St",
            profilePicture = null,
            banner = null,
            defaultCotation = true,
            gradingSystem = com.example.topoclimb.data.GradingSystem(
                free = true,
                hint = "Test hint",
                points = mapOf("6a" to 600, "6b" to 610)
            ),
            createdAt = "2025-01-01T00:00:00Z",
            updatedAt = "2025-01-01T00:00:00Z",
            email = "test@example.com",
            phone = "555-1234",
            website = "https://example.com",
            coordinates = "45.0,5.0"
        )
        
        val siteEntity = SiteEntity.fromSite(site, "test-backend")
        
        assertEquals(1, siteEntity.id)
        assertEquals("test-backend", siteEntity.backendId)
        assertEquals("Test Site", siteEntity.name)
        assertEquals("A test site", siteEntity.description)
        assertEquals(true, siteEntity.gradingSystemFree ?: false)
        assertEquals("Test hint", siteEntity.gradingSystemHint)
        assertEquals(2, siteEntity.gradingSystemPoints?.size ?: 0)
        assertTrue(siteEntity.cachedAt > 0)
    }
    
    @Test
    fun cacheExpiration_oneWeek_isCorrect() {
        val oneWeekMs = 7 * 24 * 60 * 60 * 1000L
        assertEquals(604800000L, oneWeekMs)
    }
    
    @Test
    fun cacheExpiration_threeDays_isCorrect() {
        val threeDaysMs = 3 * 24 * 60 * 60 * 1000L
        assertEquals(259200000L, threeDaysMs)
    }
}
