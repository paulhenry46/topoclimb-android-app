package com.example.topoclimb.data

import com.google.gson.Gson
import org.junit.Test
import org.junit.Assert.*

class DataModelsTest {
    
    private val gson = Gson()
    
    @Test
    fun contest_deserializesCorrectly() {
        val json = """
            {
                "id": 1,
                "name": "Summer Boulder Festival",
                "description": "Annual competition",
                "site_id": 1,
                "start_date": "2025-07-01",
                "end_date": "2025-07-03",
                "created_at": "2025-01-01T00:00:00Z",
                "updated_at": "2025-01-01T00:00:00Z"
            }
        """.trimIndent()
        
        val contest = gson.fromJson(json, Contest::class.java)
        
        assertEquals(1, contest.id)
        assertEquals("Summer Boulder Festival", contest.name)
        assertEquals("Annual competition", contest.description)
        assertEquals(1, contest.siteId)
        assertEquals("2025-07-01", contest.startDate)
        assertEquals("2025-07-03", contest.endDate)
    }
    
    @Test
    fun areasResponse_deserializesCorrectly() {
        val json = """
            {
                "data": [
                    {
                        "id": 1,
                        "name": "Cuvier",
                        "description": "Classic sector",
                        "latitude": 48.4044,
                        "longitude": 2.6992,
                        "siteId": 1
                    },
                    {
                        "id": 2,
                        "name": "Bas Cuvier",
                        "description": "Beginner sector",
                        "latitude": 48.4055,
                        "longitude": 2.6988,
                        "siteId": 1
                    }
                ]
            }
        """.trimIndent()
        
        val response = gson.fromJson(json, AreasResponse::class.java)
        
        assertNotNull(response)
        assertEquals(2, response.data.size)
        assertEquals("Cuvier", response.data[0].name)
        assertEquals("Bas Cuvier", response.data[1].name)
    }
    
    @Test
    fun contestsResponse_deserializesCorrectly() {
        val json = """
            {
                "data": [
                    {
                        "id": 1,
                        "name": "Contest 1",
                        "description": "First contest",
                        "site_id": 1,
                        "start_date": "2025-07-01",
                        "end_date": "2025-07-03"
                    }
                ]
            }
        """.trimIndent()
        
        val response = gson.fromJson(json, ContestsResponse::class.java)
        
        assertNotNull(response)
        assertEquals(1, response.data.size)
        assertEquals("Contest 1", response.data[0].name)
    }
    
    @Test
    fun sitesResponse_deserializesCorrectly() {
        val json = """
            {
                "data": [
                    {
                        "id": 1,
                        "name": "Fontainebleau",
                        "description": "Famous bouldering area",
                        "latitude": 48.4044,
                        "longitude": 2.6992,
                        "imageUrl": "https://example.com/image.jpg",
                        "slug": "fontainebleau",
                        "address": "Forest of Fontainebleau",
                        "profile_picture": "https://example.com/logo.jpg",
                        "banner": "https://example.com/banner.jpg",
                        "default_cotation": true
                    }
                ]
            }
        """.trimIndent()
        
        val response = gson.fromJson(json, SitesResponse::class.java)
        
        assertNotNull(response)
        assertEquals(1, response.data.size)
        
        val site = response.data[0]
        assertEquals(1, site.id)
        assertEquals("Fontainebleau", site.name)
        assertEquals("https://example.com/logo.jpg", site.profilePicture)
        assertEquals("https://example.com/banner.jpg", site.banner)
    }
    
    @Test
    fun siteResponse_deserializesCorrectly() {
        val json = """
            {
                "data": {
                    "id": 1,
                    "name": "Fontainebleau",
                    "description": "Famous bouldering area",
                    "latitude": 48.4044,
                    "longitude": 2.6992,
                    "imageUrl": "https://example.com/image.jpg",
                    "slug": "fontainebleau",
                    "address": "Forest of Fontainebleau",
                    "profile_picture": "https://example.com/logo.jpg",
                    "banner": "https://example.com/banner.jpg",
                    "default_cotation": true
                }
            }
        """.trimIndent()
        
        val response = gson.fromJson(json, SiteResponse::class.java)
        
        assertNotNull(response)
        assertNotNull(response.data)
        
        val site = response.data
        assertEquals(1, site.id)
        assertEquals("Fontainebleau", site.name)
        assertEquals("https://example.com/logo.jpg", site.profilePicture)
        assertEquals("https://example.com/banner.jpg", site.banner)
    }
    
    @Test
    fun site_withGradingSystem_deserializesCorrectly() {
        val json = """
            {
                "id": 1,
                "name": "Fontainebleau",
                "description": "Famous bouldering area",
                "latitude": 48.4044,
                "longitude": 2.6992,
                "imageUrl": "https://example.com/image.jpg",
                "slug": "fontainebleau",
                "address": "Forest of Fontainebleau",
                "profile_picture": "https://example.com/logo.jpg",
                "banner": "https://example.com/banner.jpg",
                "default_cotation": true,
                "grading_system": {
                    "free": false,
                    "hint": "System is Fontainebleau scale",
                    "points": {
                        "3a": 300,
                        "3a+": 310,
                        "4a": 400,
                        "5a": 500,
                        "5a+": 510,
                        "6a": 600,
                        "6a+": 610,
                        "7a": 700
                    }
                }
            }
        """.trimIndent()
        
        val site = gson.fromJson(json, Site::class.java)
        
        assertNotNull(site)
        assertEquals(1, site.id)
        assertEquals("Fontainebleau", site.name)
        
        assertNotNull(site.gradingSystem)
        assertEquals(false, site.gradingSystem?.free)
        assertEquals("System is Fontainebleau scale", site.gradingSystem?.hint)
        
        val points = site.gradingSystem?.points
        assertNotNull(points)
        assertEquals(300, points?.get("3a"))
        assertEquals(310, points?.get("3a+"))
        assertEquals(500, points?.get("5a"))
        assertEquals(610, points?.get("6a+"))
        assertEquals(700, points?.get("7a"))
    }
    
    @Test
    fun routesResponse_deserializesCorrectly() {
        val json = """
            {
                "data": [
                    {
                        "id": 1,
                        "name": "La Marie-Rose",
                        "grade": "7c",
                        "type": "boulder",
                        "height": 5,
                        "siteId": 1
                    },
                    {
                        "id": 2,
                        "name": "Biographie",
                        "grade": "9a+",
                        "type": "sport",
                        "height": 45,
                        "siteId": 2
                    }
                ]
            }
        """.trimIndent()
        
        val response = gson.fromJson(json, RoutesResponse::class.java)
        
        assertNotNull(response)
        assertEquals(2, response.data.size)
        assertEquals("La Marie-Rose", response.data[0].name)
        assertEquals("7c", response.data[0].grade)
        assertEquals("Biographie", response.data[1].name)
        assertEquals("9a+", response.data[1].grade)
    }
    
    @Test
    fun route_withCreatedAt_deserializesCorrectly() {
        val json = """
            {
                "id": 1,
                "name": "New Route",
                "grade": "6a",
                "type": "sport",
                "height": 20,
                "siteId": 1,
                "created_at": "2025-10-08T12:18:41.000000Z"
            }
        """.trimIndent()
        
        val route = gson.fromJson(json, Route::class.java)
        
        assertNotNull(route)
        assertEquals(1, route.id)
        assertEquals("New Route", route.name)
        assertEquals("2025-10-08T12:18:41.000000Z", route.createdAt)
    }
}
