package com.example.topoclimb.repository

import android.content.Context
import com.example.topoclimb.AppConfig
import com.example.topoclimb.data.*
import com.example.topoclimb.network.MultiBackendRetrofitManager
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

/**
 * Repository that aggregates data from multiple federated backends
 * Each resource is wrapped in a Federated<T> object with backend metadata
 */
class FederatedTopoClimbRepository(context: Context) {
    
    private val backendConfigRepository = BackendConfigRepository(context)
    private val retrofitManager = MultiBackendRetrofitManager(AppConfig.ENABLE_LOGGING)
    
    /**
     * Get sites from all enabled backends
     */
    suspend fun getSites(): Result<List<Federated<Site>>> {
        return try {
            val enabledBackends = backendConfigRepository.getEnabledBackends()
            
            if (enabledBackends.isEmpty()) {
                return Result.failure(IllegalStateException("No enabled backends"))
            }
            
            val sites = coroutineScope {
                enabledBackends.map { backend ->
                    async {
                        try {
                            val api = retrofitManager.getApiService(backend)
                            val response = api.getSites()
                            response.data.map { site ->
                                Federated(
                                    data = site,
                                    backend = backend.toMetadata()
                                )
                            }
                        } catch (e: Exception) {
                            emptyList<Federated<Site>>()
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
     */
    suspend fun getSite(backendId: String, siteId: Int): Result<Federated<Site>> {
        return try {
            val backend = backendConfigRepository.getBackend(backendId)
                ?: return Result.failure(IllegalArgumentException("Backend not found"))
            
            val api = retrofitManager.getApiService(backend)
            val response = api.getSite(siteId)
            
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
     */
    suspend fun getAreasBySite(backendId: String, siteId: Int): Result<List<Federated<Area>>> {
        return try {
            val backend = backendConfigRepository.getBackend(backendId)
                ?: return Result.failure(IllegalArgumentException("Backend not found"))
            
            val api = retrofitManager.getApiService(backend)
            val response = api.getAreasBySite(siteId)
            
            Result.success(
                response.data.map { area ->
                    Federated(
                        data = area,
                        backend = backend.toMetadata()
                    )
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get contests by site from a specific backend
     */
    suspend fun getContestsBySite(backendId: String, siteId: Int): Result<List<Federated<Contest>>> {
        return try {
            val backend = backendConfigRepository.getBackend(backendId)
                ?: return Result.failure(IllegalArgumentException("Backend not found"))
            
            val api = retrofitManager.getApiService(backend)
            val response = api.getContestsBySite(siteId)
            
            Result.success(
                response.data.map { contest ->
                    Federated(
                        data = contest,
                        backend = backend.toMetadata()
                    )
                }
            )
        } catch (e: Exception) {
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
}
