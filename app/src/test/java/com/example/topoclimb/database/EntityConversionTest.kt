package com.example.topoclimb.database

import com.example.topoclimb.data.Area
import com.example.topoclimb.data.Route
import com.example.topoclimb.data.Site
import com.example.topoclimb.database.entities.toArea
import com.example.topoclimb.database.entities.toEntity
import com.example.topoclimb.database.entities.toRoute
import com.example.topoclimb.database.entities.toSite
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for Room entity conversions
 */
class EntityConversionTest {
    
    @Test
    fun siteEntity_toSite_convertsCorrectly() {
        val site = Site(
            id = 1,
            name = "Test Site",
            description = "A test climbing site",
            latitude = 45.5,
            longitude = -122.6,
            imageUrl = "https://example.com/image.jpg",
            slug = "test-site",
            address = "123 Test St",
            profilePicture = null,
            banner = null,
            defaultCotation = true,
            gradingSystem = null,
            createdAt = "2024-01-01",
            updatedAt = "2024-01-02",
            email = "test@example.com",
            phone = "555-1234",
            website = "https://example.com",
            coordinates = "45.5,-122.6"
        )
        
        val entity = site.toEntity("backend-1")
        val convertedBack = entity.toSite()
        
        assertEquals(site.id, convertedBack.id)
        assertEquals(site.name, convertedBack.name)
        assertEquals(site.description, convertedBack.description)
        assertEquals(site.latitude, convertedBack.latitude)
        assertEquals(site.longitude, convertedBack.longitude)
        assertEquals(site.slug, convertedBack.slug)
        assertEquals("backend-1", entity.backendId)
    }
    
    @Test
    fun areaEntity_toArea_convertsCorrectly() {
        val area = Area(
            id = 1,
            name = "Test Area",
            description = "A test area",
            latitude = 45.5,
            longitude = -122.6,
            siteId = 1,
            svgMap = null,
            type = "bouldering"
        )
        
        val entity = area.toEntity("backend-1")
        val convertedBack = entity.toArea()
        
        assertEquals(area.id, convertedBack.id)
        assertEquals(area.name, convertedBack.name)
        assertEquals(area.description, convertedBack.description)
        assertEquals(area.siteId, convertedBack.siteId)
        assertEquals(area.type, convertedBack.type)
        assertEquals("backend-1", entity.backendId)
    }
    
    @Test
    fun routeEntity_toRoute_convertsCorrectly() {
        val route = Route(
            id = 1,
            name = "Test Route",
            grade = 500,
            type = "sport",
            description = "A test route",
            height = 20,
            siteId = 1,
            siteName = "Test Site",
            thumbnail = "https://example.com/thumb.jpg",
            color = "#FF0000",
            createdAt = "2024-01-01",
            picture = "https://example.com/pic.jpg",
            circle = null,
            openers = listOf("John Doe"),
            filteredPicture = null,
            tags = listOf("beginner", "fun"),
            numberLogs = 5,
            numberComments = 3,
            removingAt = null
        )
        
        val entity = route.toEntity("backend-1")
        val convertedBack = entity.toRoute()
        
        assertEquals(route.id, convertedBack.id)
        assertEquals(route.name, convertedBack.name)
        assertEquals(route.grade, convertedBack.grade)
        assertEquals(route.type, convertedBack.type)
        assertEquals(route.siteId, convertedBack.siteId)
        assertEquals(route.openers, convertedBack.openers)
        assertEquals(route.tags, convertedBack.tags)
        assertEquals("backend-1", entity.backendId)
    }
    
    @Test
    fun routeEntity_withNullLists_convertsCorrectly() {
        val route = Route(
            id = 1,
            name = "Test Route",
            grade = 500,
            type = "sport",
            description = null,
            height = null,
            siteId = 1,
            siteName = null,
            thumbnail = null,
            color = null,
            createdAt = null,
            picture = null,
            circle = null,
            openers = null,
            filteredPicture = null,
            tags = null,
            numberLogs = null,
            numberComments = null,
            removingAt = null
        )
        
        val entity = route.toEntity("backend-1")
        val convertedBack = entity.toRoute()
        
        assertEquals(route.id, convertedBack.id)
        assertEquals(route.name, convertedBack.name)
        assertNull(convertedBack.openers)
        assertNull(convertedBack.tags)
    }
}
