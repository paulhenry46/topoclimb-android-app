package com.example.topoclimb.network

import com.example.topoclimb.data.BackendConfig
import org.junit.Test
import org.junit.Assert.*
import org.junit.Before

class MultiBackendRetrofitManagerTest {
    
    private lateinit var manager: MultiBackendRetrofitManager
    
    @Before
    fun setup() {
        manager = MultiBackendRetrofitManager(loggingEnabled = false)
    }
    
    @Test
    fun getApiService_createsNewInstanceForNewBackend() {
        val backend = BackendConfig(
            id = "test-1",
            name = "Test Backend",
            baseUrl = "https://api.example.com/"
        )
        
        val service1 = manager.getApiService(backend)
        val service2 = manager.getApiService(backend)
        
        // Should return the same cached instance
        assertSame(service1, service2)
    }
    
    @Test
    fun getApiService_recreatesInstanceWhenUrlChanges() {
        val backendId = "test-1"
        val backend1 = BackendConfig(
            id = backendId,
            name = "Test Backend",
            baseUrl = "https://api.example.com/"
        )
        
        val service1 = manager.getApiService(backend1)
        
        // Update backend with new URL
        val backend2 = backend1.copy(
            baseUrl = "https://api2.example.com/"
        )
        
        val service2 = manager.getApiService(backend2)
        
        // Should return a different instance because URL changed
        assertNotSame(service1, service2)
    }
    
    @Test
    fun getApiService_returnsSameInstanceWhenOnlyNameChanges() {
        val backendId = "test-1"
        val backend1 = BackendConfig(
            id = backendId,
            name = "Test Backend",
            baseUrl = "https://api.example.com/"
        )
        
        val service1 = manager.getApiService(backend1)
        
        // Update backend with same URL but different name
        val backend2 = backend1.copy(
            name = "Updated Name"
        )
        
        val service2 = manager.getApiService(backend2)
        
        // Should return the same instance because URL didn't change
        assertSame(service1, service2)
    }
    
    @Test
    fun clearCache_removesAllCachedInstances() {
        val backend1 = BackendConfig(
            id = "test-1",
            name = "Backend 1",
            baseUrl = "https://api1.example.com/"
        )
        val backend2 = BackendConfig(
            id = "test-2",
            name = "Backend 2",
            baseUrl = "https://api2.example.com/"
        )
        
        val service1 = manager.getApiService(backend1)
        val service2 = manager.getApiService(backend2)
        
        manager.clearCache()
        
        // After clearing cache, should create new instances
        val newService1 = manager.getApiService(backend1)
        val newService2 = manager.getApiService(backend2)
        
        assertNotSame(service1, newService1)
        assertNotSame(service2, newService2)
    }
    
    @Test
    fun removeBackend_removesSpecificBackendCache() {
        val backend1 = BackendConfig(
            id = "test-1",
            name = "Backend 1",
            baseUrl = "https://api1.example.com/"
        )
        val backend2 = BackendConfig(
            id = "test-2",
            name = "Backend 2",
            baseUrl = "https://api2.example.com/"
        )
        
        val service1 = manager.getApiService(backend1)
        val service2 = manager.getApiService(backend2)
        
        manager.removeBackend("test-1")
        
        // Backend 1 should get a new instance
        val newService1 = manager.getApiService(backend1)
        assertNotSame(service1, newService1)
        
        // Backend 2 should still have the same instance
        val sameService2 = manager.getApiService(backend2)
        assertSame(service2, sameService2)
    }
}
