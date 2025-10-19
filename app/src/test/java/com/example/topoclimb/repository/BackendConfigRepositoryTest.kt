package com.example.topoclimb.repository

import com.example.topoclimb.data.BackendConfig
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for URL format validation in BackendConfig
 */
class BackendConfigRepositoryTest {
    
    @Test
    fun backendConfig_isValid_withValidUrl_returnsTrue() {
        val backend = BackendConfig(
            id = "test-1",
            name = "Backend 1",
            baseUrl = "https://api.example.com/"
        )
        
        assertTrue(backend.isValid())
    }
    
    @Test
    fun backendConfig_isValid_withInvalidUrl_returnsFalse() {
        val backend = BackendConfig(
            id = "test-1",
            name = "Backend 1",
            baseUrl = "not-a-url"
        )
        
        assertFalse(backend.isValid())
    }
    
    @Test
    fun backendConfig_isValid_withUrlNotEndingWithSlash_returnsFalse() {
        val backend = BackendConfig(
            id = "test-1",
            name = "Backend 1",
            baseUrl = "https://api.example.com"
        )
        
        assertFalse(backend.isValid())
    }
    
    @Test
    fun backendConfig_isValid_withHttpUrl_returnsTrue() {
        val backend = BackendConfig(
            id = "test-1",
            name = "Backend 1",
            baseUrl = "http://api.example.com/"
        )
        
        assertTrue(backend.isValid())
    }
}
