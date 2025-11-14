package com.example.topoclimb.repository

import android.content.Context
import com.example.topoclimb.AppConfig
import com.example.topoclimb.cache.CacheManager
import com.example.topoclimb.cache.CachePreferences
import com.example.topoclimb.data.*
import com.example.topoclimb.network.MultiBackendRetrofitManager
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

/**
 * Repository that aggregates data from multiple federated backends
 * Each resource is wrapped in a Federated<T> object with backend metadata
 * 
 * Implements cache-first strategy with network fallback
 */
class FederatedTopoClimbRepository(private val context: Context) {
    
    private val backendConfigRepository = BackendConfigRepository(context)
    private val retrofitManager = MultiBackendRetrofitManager(AppConfig.ENABLE_LOGGING)
    private val cacheManager = CacheManager(context)
    private val cachePreferences = CachePreferences(context)
    
    /**
     * Get sites from all enabled backends
     * @param forceRefresh: if true, bypass cache and fetch from network
     */
    suspend fun getSites(forceRefresh: Boolean = false): Result<List<Federated<Site>>> {
        return try {
            val enabledBackends = backendConfigRepository.getEnabledBackends()
            
            if (enabledBackends.isEmpty()) {
                return Result.failure(IllegalStateException("No enabled backends"))
            }
            
            val sites = coroutineScope {
                enabledBackends.map { backend ->
                    async {
                        try {
                            // Cache-first strategy (unless force refresh or cache disabled)
                            if (!forceRefresh && cachePreferences.isCacheEnabled) {
                                val cached = cacheManager.getCachedSites(backend.id)
                                if (cached != null) {
                                    return@async cached.map { site ->
                                        Federated(
                                            data = site,
                                            backend = backend.toMetadata()
                                        )
                                    }
                                }
                            }
                            
                            // Network fallback
                            val api = retrofitManager.getApiService(backend)
                            val response = api.getSites()
                            
                            // Cache the result if cache is enabled
                            if (cachePreferences.isCacheEnabled) {
                                cacheManager.cacheSites(response.data, backend.id)
                            }
                            
                            response.data.map { site ->
                                Federated(
                                    data = site,
                                    backend = backend.toMetadata()
                                )
                            }
                        } catch (e: Exception) {
                            // If network fails, try to return cached data even if expired
                            if (cachePreferences.isCacheEnabled) {
                                val cached = cacheManager.getCachedSites(backend.id)
                                cached?.map { site ->
                                    Federated(
                                        data = site,
                                        backend = backend.toMetadata()
                                    )
                                } ?: emptyList()
                            } else {
                                emptyList<Federated<Site>>()
                            }
                        }
                    }
                }.awaitAll().flatten()
            }
            
            Result.success(sites)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get a specific site from a specific backend
     * @param forceRefresh: if true, bypass cache and fetch from network
     */
    suspend fun getSite(backendId: String, siteId: Int, forceRefresh: Boolean = false): Result<Federated<Site>> {
        return try {
            val backend = backendConfigRepository.getBackend(backendId)
                ?: return Result.failure(IllegalArgumentException("Backend not found"))
            
            // Cache-first strategy
            if (!forceRefresh && cachePreferences.isCacheEnabled) {
                val cached = cacheManager.getCachedSite(siteId, backendId)
                if (cached != null) {
                    return Result.success(
                        Federated(
                            data = cached,
                            backend = backend.toMetadata()
                        )
                    )
                }
            }
            
            // Network fallback
            val api = retrofitManager.getApiService(backend)
            val response = api.getSite(siteId)
            
            // Cache the result if cache is enabled
            if (cachePreferences.isCacheEnabled) {
                cacheManager.cacheSite(response.data, backendId)
            }
            
            Result.success(
                Federated(
                    data = response.data,
                    backend = backend.toMetadata()
                )
            )
        } catch (e: Exception) {
            // If network fails, try to return cached data even if expired
            if (cachePreferences.isCacheEnabled) {
                val cached = cacheManager.getCachedSite(siteId, backendId)
                if (cached != null) {
                    return Result.success(
                        Federated(
                            data = cached,
                            backend = backend.toMetadata()
                        )
                    )
                }
            }
            Result.failure(e)
        }
    }
    
    /**
     * Get routes from all enabled backends
     */
    suspend fun getRoutes(
        siteId: Int? = null,
        grade: String? = null,
        type: String? = null
    ): Result<List<Federated<Route>>> {
        return try {
            val enabledBackends = backendConfigRepository.getEnabledBackends()
            
            if (enabledBackends.isEmpty()) {
                return Result.failure(IllegalStateException("No enabled backends"))
            }
            
            val routes = coroutineScope {
                enabledBackends.map { backend ->
                    async {
                        try {
                            val api = retrofitManager.getApiService(backend)
                            val response = api.getRoutes(siteId, grade, type)
                            response.data.map { route ->
                                Federated(
                                    data = route,
                                    backend = backend.toMetadata()
                                )
                            }
                        } catch (e: Exception) {
                            emptyList<Federated<Route>>()
                        }
                    }
                }.awaitAll().flatten()
            }
            
            Result.success(routes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get a specific route from a specific backend
     */
    suspend fun getRoute(backendId: String, routeId: Int): Result<Federated<Route>> {
        return try {
            val backend = backendConfigRepository.getBackend(backendId)
                ?: return Result.failure(IllegalArgumentException("Backend not found"))
            
            val api = retrofitManager.getApiService(backend)
            val response = api.getRoute(routeId)
            
            Result.success(
                Federated(
                    data = response.data,
                    backend = backend.toMetadata()
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get areas from all enabled backends
     */
    suspend fun getAreas(): Result<List<Federated<Area>>> {
        return try {
            val enabledBackends = backendConfigRepository.getEnabledBackends()
            
            if (enabledBackends.isEmpty()) {
                return Result.failure(IllegalStateException("No enabled backends"))
            }
            
            val areas = coroutineScope {
                enabledBackends.map { backend ->
                    async {
                        try {
                            val api = retrofitManager.getApiService(backend)
                            val response = api.getAreas()
                            response.data.map { area ->
                                Federated(
                                    data = area,
                                    backend = backend.toMetadata()
                                )
                            }
                        } catch (e: Exception) {
                            emptyList<Federated<Area>>()
                        }
                    }
                }.awaitAll().flatten()
            }
            
            Result.success(areas)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get a specific area from a specific backend
     */
    suspend fun getArea(backendId: String, areaId: Int): Result<Federated<Area>> {
        return try {
            val backend = backendConfigRepository.getBackend(backendId)
                ?: return Result.failure(IllegalArgumentException("Backend not found"))
            
            val api = retrofitManager.getApiService(backend)
            val response = api.getArea(areaId)
            
            Result.success(
                Federated(
                    data = response.data,
                    backend = backend.toMetadata()
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get routes by area from a specific backend
     */
    suspend fun getRoutesByArea(backendId: String, areaId: Int): Result<List<Federated<Route>>> {
        return try {
            val backend = backendConfigRepository.getBackend(backendId)
                ?: return Result.failure(IllegalArgumentException("Backend not found"))
            
            val api = retrofitManager.getApiService(backend)
            val response = api.getRoutesByArea(areaId)
            
            Result.success(
                response.data.map { route ->
                    Federated(
                        data = route,
                        backend = backend.toMetadata()
                    )
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get areas by site from a specific backend
     * @param forceRefresh: if true, bypass cache and fetch from network
     */
    suspend fun getAreasBySite(backendId: String, siteId: Int, forceRefresh: Boolean = false): Result<List<Federated<Area>>> {
        return try {
            val backend = backendConfigRepository.getBackend(backendId)
                ?: return Result.failure(IllegalArgumentException("Backend not found"))
            
            // Cache-first strategy
            if (!forceRefresh && cachePreferences.isCacheEnabled) {
                val cached = cacheManager.getCachedAreasBySite(siteId, backendId)
                if (cached != null) {
                    return Result.success(
                        cached.map { area ->
                            Federated(
                                data = area,
                                backend = backend.toMetadata()
                            )
                        }
                    )
                }
            }
            
            // Network fallback
            val api = retrofitManager.getApiService(backend)
            val response = api.getAreasBySite(siteId)
            
            // Cache the result if cache is enabled
            if (cachePreferences.isCacheEnabled) {
                cacheManager.cacheAreas(response.data, backendId)
            }
            
            Result.success(
                response.data.map { area ->
                    Federated(
                        data = area,
                        backend = backend.toMetadata()
                    )
                }
            )
        } catch (e: Exception) {
            // If network fails, try to return cached data even if expired
            if (cachePreferences.isCacheEnabled) {
                val cached = cacheManager.getCachedAreasBySite(siteId, backendId)
                if (cached != null) {
                    return Result.success(
                        cached.map { area ->
                            Federated(
                                data = area,
                                backend = backend.toMetadata()
                            )
                        }
                    )
                }
            }
            Result.failure(e)
        }
    }
    
    /**
     * Get contests by site from a specific backend
     * @param forceRefresh: if true, bypass cache and fetch from network
     */
    suspend fun getContestsBySite(backendId: String, siteId: Int, forceRefresh: Boolean = false): Result<List<Federated<Contest>>> {
        return try {
            val backend = backendConfigRepository.getBackend(backendId)
                ?: return Result.failure(IllegalArgumentException("Backend not found"))
            
            // Cache-first strategy
            if (!forceRefresh && cachePreferences.isCacheEnabled) {
                val cached = cacheManager.getCachedContestsBySite(siteId, backendId)
                if (cached != null) {
                    return Result.success(
                        cached.map { contest ->
                            Federated(
                                data = contest,
                                backend = backend.toMetadata()
                            )
                        }
                    )
                }
            }
            
            // Network fallback
            val api = retrofitManager.getApiService(backend)
            val response = api.getContestsBySite(siteId)
            
            // Cache the result if cache is enabled
            if (cachePreferences.isCacheEnabled) {
                cacheManager.cacheContests(response.data, backendId)
            }
            
            Result.success(
                response.data.map { contest ->
                    Federated(
                        data = contest,
                        backend = backend.toMetadata()
                    )
                }
            )
        } catch (e: Exception) {
            // If network fails, try to return cached data even if expired
            if (cachePreferences.isCacheEnabled) {
                val cached = cacheManager.getCachedContestsBySite(siteId, backendId)
                if (cached != null) {
                    return Result.success(
                        cached.map { contest ->
                            Federated(
                                data = contest,
                                backend = backend.toMetadata()
                            )
                        }
                    )
                }
            }
            Result.failure(e)
        }
    }
    
    /**
     * Get sectors by area from a specific backend
     */
    suspend fun getSectorsByArea(backendId: String, areaId: Int): Result<List<Federated<Sector>>> {
        return try {
            val backend = backendConfigRepository.getBackend(backendId)
                ?: return Result.failure(IllegalArgumentException("Backend not found"))
            
            val api = retrofitManager.getApiService(backend)
            val response = api.getSectorsByArea(areaId)
            
            Result.success(
                response.data.map { sector ->
                    Federated(
                        data = sector,
                        backend = backend.toMetadata()
                    )
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get lines by sector from a specific backend
     */
    suspend fun getLinesBySector(backendId: String, sectorId: Int): Result<List<Federated<Line>>> {
        return try {
            val backend = backendConfigRepository.getBackend(backendId)
                ?: return Result.failure(IllegalArgumentException("Backend not found"))
            
            val api = retrofitManager.getApiService(backend)
            val response = api.getLinesBySector(sectorId)
            
            Result.success(
                response.data.map { line ->
                    Federated(
                        data = line,
                        backend = backend.toMetadata()
                    )
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get routes by line from a specific backend
     */
    suspend fun getRoutesByLine(backendId: String, lineId: Int): Result<List<Federated<Route>>> {
        return try {
            val backend = backendConfigRepository.getBackend(backendId)
                ?: return Result.failure(IllegalArgumentException("Backend not found"))
            
            val api = retrofitManager.getApiService(backend)
            val response = api.getRoutesByLine(lineId)
            
            Result.success(
                response.data.map { route ->
                    Federated(
                        data = route,
                        backend = backend.toMetadata()
                    )
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get route logs from a specific backend
     */
    suspend fun getRouteLogs(backendId: String, routeId: Int): Result<List<Federated<Log>>> {
        return try {
            val backend = backendConfigRepository.getBackend(backendId)
                ?: return Result.failure(IllegalArgumentException("Backend not found"))
            
            val api = retrofitManager.getApiService(backend)
            val response = api.getRouteLogs(routeId)
            
            Result.success(
                response.data.map { log ->
                    Federated(
                        data = log,
                        backend = backend.toMetadata()
                    )
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get the backend config repository for UI interactions
     */
    fun getBackendConfigRepository(): BackendConfigRepository {
        return backendConfigRepository
    }
    
    /**
     * Get cache manager for direct cache operations
     */
    fun getCacheManager(): CacheManager {
        return cacheManager
    }
    
    /**
     * Get cache preferences
     */
    fun getCachePreferences(): CachePreferences {
        return cachePreferences
    }
    
    /**
     * Clear all cached data
     */
    suspend fun clearAllCache() {
        cacheManager.clearAllCache()
    }
}
