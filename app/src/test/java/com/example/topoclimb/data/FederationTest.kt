package com.example.topoclimb.data

import org.junit.Test
import org.junit.Assert.*

class FederationTest {
    
    @Test
    fun backendConfig_isValid_returnsTrueForValidUrl() {
        val config = BackendConfig(
            name = "Test Backend",
            baseUrl = "https://api.example.com/"
        )
        
        assertTrue(config.isValid())
    }
    
    @Test
    fun backendConfig_isValid_returnsFalseForMissingSlash() {
        val config = BackendConfig(
            name = "Test Backend",
            baseUrl = "https://api.example.com"
        )
        
        assertFalse(config.isValid())
    }
    
    @Test
    fun backendConfig_isValid_returnsFalseForInvalidProtocol() {
        val config = BackendConfig(
            name = "Test Backend",
            baseUrl = "ftp://api.example.com/"
        )
        
        assertFalse(config.isValid())
    }
    
    @Test
    fun backendConfig_isValid_returnsFalseForBlankUrl() {
        val config = BackendConfig(
            name = "Test Backend",
            baseUrl = ""
        )
        
        assertFalse(config.isValid())
    }
    
    @Test
    fun backendConfig_toMetadata_createsCorrectMetadata() {
        val config = BackendConfig(
            id = "test-id",
            name = "Test Backend",
            baseUrl = "https://api.example.com/"
        )
        
        val metadata = config.toMetadata()
        
        assertEquals("test-id", metadata.backendId)
        assertEquals("Test Backend", metadata.backendName)
        assertEquals("https://api.example.com/", metadata.baseUrl)
    }
    
    @Test
    fun federated_getGlobalId_combinesBackendAndResourceId() {
        val backend = BackendMetadata(
            backendId = "backend-1",
            backendName = "Test Backend",
            baseUrl = "https://api.example.com/"
        )
        
        val site = Site(
            id = 42,
            name = "Test Site",
            description = "A test climbing site",
            latitude = 48.0,
            longitude = 2.0,
            imageUrl = null,
            slug = "test-site",
            address = null,
            profilePicture = null,
            banner = null,
            defaultCotation = null,
            createdAt = null,
            updatedAt = null
        )
        
        val federatedSite = Federated(
            data = site,
            backend = backend
        )
        
        val globalId = federatedSite.getGlobalId(site.id)
        assertEquals("backend-1:42", globalId)
    }
    
    @Test
    fun backendMetadata_containsCorrectInformation() {
        val metadata = BackendMetadata(
            backendId = "backend-123",
            backendName = "Production Backend",
            baseUrl = "https://prod.api.example.com/"
        )
        
        assertEquals("backend-123", metadata.backendId)
        assertEquals("Production Backend", metadata.backendName)
        assertEquals("https://prod.api.example.com/", metadata.baseUrl)
    }
    
    @Test
    fun federatedData_wrapsDataCorrectly() {
        val backend = BackendMetadata(
            backendId = "test",
            backendName = "Test",
            baseUrl = "https://test.com/"
        )
        
        val area = Area(
            id = 1,
            name = "Test Area",
            description = "Test description",
            latitude = 48.0,
            longitude = 2.0,
            siteId = 1,
            svgMap = null
        )
        
        val federated = Federated(
            data = area,
            backend = backend
        )
        
        assertEquals(area, federated.data)
        assertEquals(backend, federated.backend)
        assertEquals("test:1", federated.getGlobalId(area.id))
    }
}
