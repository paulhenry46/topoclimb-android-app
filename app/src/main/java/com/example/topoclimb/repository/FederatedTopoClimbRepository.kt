package com.example.topoclimb.repository

import android.content.Context
import com.example.topoclimb.AppConfig
import com.example.topoclimb.data.*
import com.example.topoclimb.database.TopoClimbDatabase
import com.example.topoclimb.database.entities.toArea
import com.example.topoclimb.database.entities.toContest
import com.example.topoclimb.database.entities.toEntity
import com.example.topoclimb.database.entities.toRoute
import com.example.topoclimb.database.entities.toSite
import com.example.topoclimb.network.MultiBackendRetrofitManager
import com.example.topoclimb.utils.NetworkUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

/**
 * Repository that aggregates data from multiple federated backends
 * Each resource is wrapped in a Federated<T> object with backend metadata
 * 
 * Follows offline-first architecture:
 * - Returns cached data from Room immediately
 * - Fetches fresh data from API in background when online
 * - Updates Room cache with API response
 */
class FederatedTopoClimbRepository(private val context: Context) {
    
    private val backendConfigRepository = BackendConfigRepository(context)
    private val retrofitManager = MultiBackendRetrofitManager(AppConfig.ENABLE_LOGGING)
    private val database = TopoClimbDatabase.getDatabase(context)
    
    // Background scope for non-blocking cache refreshes
    private val backgroundScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    /**
     * Get sites from all enabled backends (offline-first)
     * Returns cached data first, then refreshes from network
     */
    suspend fun getSites(): Result<List<Federated<Site>>> {
        return try {
            val enabledBackends = backendConfigRepository.getEnabledBackends()
            
            if (enabledBackends.isEmpty()) {
                return Result.failure(IllegalStateException("No enabled backends"))
            }
            
            // Get cached sites from Room
            val cachedSites = enabledBackends.flatMap { backend ->
                database.siteDao().getAllSites(backend.id).map { entity ->
                    Federated(
                        data = entity.toSite(),
                        backend = backend.toMetadata()
                    )
                }
            }
            
            // If network available, refresh data in background (non-blocking)
            if (NetworkUtils.isNetworkAvailable(context)) {
                backgroundScope.launch {
                    refreshSitesFromNetwork(enabledBackends)
                }
            }
            
            // Return cached data (or empty list if no cache)
            Result.success(cachedSites)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Refresh sites from network and update cache
     */
    private suspend fun refreshSitesFromNetwork(backends: List<BackendConfig>) {
        try {
            coroutineScope {
                backends.map { backend ->
                    async {
                        try {
                            val api = retrofitManager.getApiService(backend)
                            val response = api.getSites()
                            val entities = response.data.map { it.toEntity(backend.id) }
                            database.siteDao().insertSites(entities)
                        } catch (e: Exception) {
                            // Silently fail - we already returned cached data
                        }
                    }
                }.awaitAll()
            }
        } catch (e: Exception) {
            // Ignore network errors when refreshing
        }
    }
    
    /**
     * Get a specific site from a specific backend (offline-first)
     */
    suspend fun getSite(backendId: String, siteId: Int): Result<Federated<Site>> {
        return try {
            val backend = backendConfigRepository.getBackend(backendId)
                ?: return Result.failure(IllegalArgumentException("Backend not found"))
            
            // Get cached site from Room
            val cachedSite = database.siteDao().getSite(siteId, backendId)
            
            // If network available, refresh data in background (non-blocking)
            if (NetworkUtils.isNetworkAvailable(context) && cachedSite != null) {
                backgroundScope.launch {
                    try {
                        val api = retrofitManager.getApiService(backend)
                        val response = api.getSite(siteId)
                        database.siteDao().insertSite(response.data.toEntity(backendId))
                    } catch (e: Exception) {
                        // Silently fail - we already returned cached data
                    }
                }
            }
            
            // Return cached data or fetch from network if no cache
            if (cachedSite != null) {
                Result.success(
                    Federated(
                        data = cachedSite.toSite(),
                        backend = backend.toMetadata()
                    )
                )
            } else if (NetworkUtils.isNetworkAvailable(context)) {
                // No cache and network available - fetch synchronously
                val api = retrofitManager.getApiService(backend)
                val response = api.getSite(siteId)
                database.siteDao().insertSite(response.data.toEntity(backendId))
                Result.success(
                    Federated(
                        data = response.data,
                        backend = backend.toMetadata()
                    )
                )
            } else {
                Result.failure(Exception("No cached data and network unavailable"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get routes from all enabled backends (offline-first)
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
            
            // Get cached routes from Room
            val cachedRoutes = enabledBackends.flatMap { backend ->
                val routes = if (siteId != null) {
                    database.routeDao().getRoutesBySite(siteId, backend.id)
                } else {
                    database.routeDao().getAllRoutes(backend.id)
                }
                routes.map { entity ->
                    Federated(
                        data = entity.toRoute(),
                        backend = backend.toMetadata()
                    )
                }
            }.let { routes ->
                // Apply filters if specified
                var filtered = routes
                if (grade != null) {
                    filtered = filtered.filter { it.data.grade?.toString() == grade }
                }
                if (type != null) {
                    filtered = filtered.filter { it.data.type == type }
                }
                filtered
            }
            
            // If network available, refresh data in background (non-blocking)
            if (NetworkUtils.isNetworkAvailable(context)) {
                backgroundScope.launch {
                    refreshRoutesFromNetwork(enabledBackends, siteId, grade, type)
                }
            }
            
            Result.success(cachedRoutes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Refresh routes from network and update cache
     */
    private suspend fun refreshRoutesFromNetwork(
        backends: List<BackendConfig>,
        siteId: Int? = null,
        grade: String? = null,
        type: String? = null
    ) {
        try {
            coroutineScope {
                backends.map { backend ->
                    async {
                        try {
                            val api = retrofitManager.getApiService(backend)
                            val response = api.getRoutes(siteId, grade, type)
                            val entities = response.data.map { it.toEntity(backend.id) }
                            database.routeDao().insertRoutes(entities)
                        } catch (e: Exception) {
                            // Silently fail - we already returned cached data
                        }
                    }
                }.awaitAll()
            }
        } catch (e: Exception) {
            // Ignore network errors when refreshing
        }
    }
    
    /**
     * Get a specific route from a specific backend (offline-first)
     */
    suspend fun getRoute(backendId: String, routeId: Int): Result<Federated<Route>> {
        return try {
            val backend = backendConfigRepository.getBackend(backendId)
                ?: return Result.failure(IllegalArgumentException("Backend not found"))
            
            // Get cached route from Room
            val cachedRoute = database.routeDao().getRoute(routeId, backendId)
            
            // If network available, refresh data in background (non-blocking)
            if (NetworkUtils.isNetworkAvailable(context) && cachedRoute != null) {
                backgroundScope.launch {
                    try {
                        val api = retrofitManager.getApiService(backend)
                        val response = api.getRoute(routeId)
                        database.routeDao().insertRoute(response.data.toEntity(backendId))
                    } catch (e: Exception) {
                        // Silently fail - we already returned cached data
                    }
                }
            }
            
            // Return cached data or fetch from network if no cache
            if (cachedRoute != null) {
                Result.success(
                    Federated(
                        data = cachedRoute.toRoute(),
                        backend = backend.toMetadata()
                    )
                )
            } else if (NetworkUtils.isNetworkAvailable(context)) {
                // No cache and network available - fetch synchronously
                val api = retrofitManager.getApiService(backend)
                val response = api.getRoute(routeId)
                database.routeDao().insertRoute(response.data.toEntity(backendId))
                Result.success(
                    Federated(
                        data = response.data,
                        backend = backend.toMetadata()
                    )
                )
            } else {
                Result.failure(Exception("No cached data and network unavailable"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get areas from all enabled backends (offline-first)
     */
    suspend fun getAreas(): Result<List<Federated<Area>>> {
        return try {
            val enabledBackends = backendConfigRepository.getEnabledBackends()
            
            if (enabledBackends.isEmpty()) {
                return Result.failure(IllegalStateException("No enabled backends"))
            }
            
            // Get cached areas from Room
            val cachedAreas = enabledBackends.flatMap { backend ->
                database.areaDao().getAllAreas(backend.id).map { entity ->
                    Federated(
                        data = entity.toArea(),
                        backend = backend.toMetadata()
                    )
                }
            }
            
            // If network available, refresh data in background
            if (NetworkUtils.isNetworkAvailable(context)) {
                backgroundScope.launch {
                    refreshAreasFromNetwork(enabledBackends)
                }
            }
            
            Result.success(cachedAreas)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Refresh areas from network and update cache
     */
    private suspend fun refreshAreasFromNetwork(backends: List<BackendConfig>) {
        try {
            coroutineScope {
                backends.map { backend ->
                    async {
                        try {
                            val api = retrofitManager.getApiService(backend)
                            val response = api.getAreas()
                            val entities = response.data.map { it.toEntity(backend.id) }
                            database.areaDao().insertAreas(entities)
                        } catch (e: Exception) {
                            // Silently fail - we already returned cached data
                        }
                    }
                }.awaitAll()
            }
        } catch (e: Exception) {
            // Ignore network errors when refreshing
        }
    }
    
    /**
     * Get a specific area from a specific backend (offline-first)
     */
    suspend fun getArea(backendId: String, areaId: Int): Result<Federated<Area>> {
        return try {
            val backend = backendConfigRepository.getBackend(backendId)
                ?: return Result.failure(IllegalArgumentException("Backend not found"))
            
            // Get cached area from Room
            val cachedArea = database.areaDao().getArea(areaId, backendId)
            
            // If network available, refresh data in background (non-blocking)
            if (NetworkUtils.isNetworkAvailable(context) && cachedArea != null) {
                backgroundScope.launch {
                    try {
                        val api = retrofitManager.getApiService(backend)
                        val response = api.getArea(areaId)
                        database.areaDao().insertArea(response.data.toEntity(backendId))
                    } catch (e: Exception) {
                        // Silently fail - we already returned cached data
                    }
                }
            }
            
            // Return cached data or fetch from network if no cache
            if (cachedArea != null) {
                Result.success(
                    Federated(
                        data = cachedArea.toArea(),
                        backend = backend.toMetadata()
                    )
                )
            } else if (NetworkUtils.isNetworkAvailable(context)) {
                // No cache and network available - fetch synchronously
                val api = retrofitManager.getApiService(backend)
                val response = api.getArea(areaId)
                database.areaDao().insertArea(response.data.toEntity(backendId))
                Result.success(
                    Federated(
                        data = response.data,
                        backend = backend.toMetadata()
                    )
                )
            } else {
                Result.failure(Exception("No cached data and network unavailable"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get routes by area from a specific backend (offline-first)
     */
    suspend fun getRoutesByArea(backendId: String, areaId: Int): Result<List<Federated<Route>>> {
        return try {
            val backend = backendConfigRepository.getBackend(backendId)
                ?: return Result.failure(IllegalArgumentException("Backend not found"))
            
            // For now, fetch from network as we don't have area-route mapping in Room
            // This is a simplification - in a full implementation, you'd add a junction table
            val api = retrofitManager.getApiService(backend)
            val response = api.getRoutesByArea(areaId)
            
            // Cache the routes
            val entities = response.data.map { it.toEntity(backendId) }
            database.routeDao().insertRoutes(entities)
            
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
     * Get areas by site from a specific backend (offline-first)
     */
    suspend fun getAreasBySite(backendId: String, siteId: Int): Result<List<Federated<Area>>> {
        return try {
            val backend = backendConfigRepository.getBackend(backendId)
                ?: return Result.failure(IllegalArgumentException("Backend not found"))
            
            // Get cached areas from Room
            val cachedAreas = database.areaDao().getAreasBySite(siteId, backendId)
            android.util.Log.d("OfflineFirst", "getAreasBySite - siteId: $siteId, backendId: $backendId, cached count: ${cachedAreas.size}")
            
            // If we have cached data, return it and refresh in background (non-blocking)
            if (cachedAreas.isNotEmpty()) {
                android.util.Log.d("OfflineFirst", "Returning ${cachedAreas.size} cached areas for site $siteId")
                // Launch background refresh if online
                if (NetworkUtils.isNetworkAvailable(context)) {
                    backgroundScope.launch {
                        try {
                            val api = retrofitManager.getApiService(backend)
                            val response = api.getAreasBySite(siteId)
                            // Use the correctSiteId parameter when caching because API may return siteId=0
                            val entities = response.data.map { it.toEntity(backendId, siteId) }
                            android.util.Log.d("OfflineFirst", "Background refresh: Caching ${entities.size} areas for site $siteId")
                            database.areaDao().insertAreas(entities)
                        } catch (e: Exception) {
                            // Silently fail - we already returned cached data
                            android.util.Log.e("OfflineFirst", "Background refresh failed for areas", e)
                        }
                    }
                }
                
                return Result.success(
                    cachedAreas.map { entity ->
                        Federated(
                            data = entity.toArea(),
                            backend = backend.toMetadata()
                        )
                    }
                )
            }
            
            // No cache - fetch from network if available
            if (NetworkUtils.isNetworkAvailable(context)) {
                android.util.Log.d("OfflineFirst", "No cache for site $siteId, fetching from network")
                try {
                    val api = retrofitManager.getApiService(backend)
                    val response = api.getAreasBySite(siteId)
                    // Use the correctSiteId parameter when caching because API may return siteId=0
                    val entities = response.data.map { it.toEntity(backendId, siteId) }
                    android.util.Log.d("OfflineFirst", "Fetched ${entities.size} areas from network, caching for site $siteId")
                    entities.forEach { entity ->
                        android.util.Log.d("OfflineFirst", "Caching area: id=${entity.id}, name=${entity.name}, siteId=${entity.siteId}, backendId=${entity.backendId}")
                    }
                    database.areaDao().insertAreas(entities)
                    android.util.Log.d("OfflineFirst", "Successfully cached ${entities.size} areas")
                    return Result.success(
                        response.data.map { area ->
                            Federated(
                                data = area,
                                backend = backend.toMetadata()
                            )
                        }
                    )
                } catch (e: Exception) {
                    android.util.Log.e("OfflineFirst", "Failed to fetch or cache areas for site $siteId", e)
                    return Result.failure(e)
                }
            }
            
            // No cache and offline - return empty list
            android.util.Log.w("OfflineFirst", "No cache and offline for site $siteId - returning empty")
            Result.success(emptyList())
        } catch (e: Exception) {
            android.util.Log.e("OfflineFirst", "Error in getAreasBySite for site $siteId", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get contests by site from a specific backend (offline-first)
     */
    suspend fun getContestsBySite(backendId: String, siteId: Int): Result<List<Federated<Contest>>> {
        return try {
            val backend = backendConfigRepository.getBackend(backendId)
                ?: return Result.failure(IllegalArgumentException("Backend not found"))
            
            // Get cached contests from Room
            val cachedContests = database.contestDao().getContestsBySite(siteId, backendId)
            android.util.Log.d("OfflineFirst", "getContestsBySite - siteId: $siteId, backendId: $backendId, cached count: ${cachedContests.size}")
            
            // If we have cached data, return it and refresh in background (non-blocking)
            if (cachedContests.isNotEmpty()) {
                android.util.Log.d("OfflineFirst", "Returning ${cachedContests.size} cached contests for site $siteId")
                // Launch background refresh if online
                if (NetworkUtils.isNetworkAvailable(context)) {
                    backgroundScope.launch {
                        try {
                            val api = retrofitManager.getApiService(backend)
                            val response = api.getContestsBySite(siteId)
                            // Use the correctSiteId parameter when caching because API may return incorrect siteId
                            val entities = response.data.map { it.toEntity(backendId, siteId) }
                            android.util.Log.d("OfflineFirst", "Background refresh: Caching ${entities.size} contests for site $siteId")
                            database.contestDao().insertContests(entities)
                        } catch (e: Exception) {
                            // Silently fail - we already returned cached data
                            android.util.Log.e("OfflineFirst", "Background refresh failed for contests", e)
                        }
                    }
                }
                
                return Result.success(
                    cachedContests.map { entity ->
                        Federated(
                            data = entity.toContest(),
                            backend = backend.toMetadata()
                        )
                    }
                )
            }
            
            // No cache - fetch from network if available
            if (NetworkUtils.isNetworkAvailable(context)) {
                android.util.Log.d("OfflineFirst", "No cache for site $siteId contests, fetching from network")
                try {
                    val api = retrofitManager.getApiService(backend)
                    val response = api.getContestsBySite(siteId)
                    // Use the correctSiteId parameter when caching because API may return incorrect siteId
                    val entities = response.data.map { it.toEntity(backendId, siteId) }
                    android.util.Log.d("OfflineFirst", "Fetched ${entities.size} contests from network, caching for site $siteId")
                    database.contestDao().insertContests(entities)
                    android.util.Log.d("OfflineFirst", "Successfully cached ${entities.size} contests")
                    return Result.success(
                        response.data.map { contest ->
                            Federated(
                                data = contest,
                                backend = backend.toMetadata()
                            )
                        }
                    )
                } catch (e: Exception) {
                    android.util.Log.e("OfflineFirst", "Failed to fetch or cache contests for site $siteId", e)
                    return Result.failure(e)
                }
            }
            
            // No cache and offline - return empty list
            android.util.Log.w("OfflineFirst", "No cache and offline for site $siteId contests - returning empty")
            Result.success(emptyList())
        } catch (e: Exception) {
            android.util.Log.e("OfflineFirst", "Error in getContestsBySite for site $siteId", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get sectors by area from a specific backend
     * Sectors are not cached in this implementation (can be added later)
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
     * Lines are not cached in this implementation (can be added later)
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
            
            // Cache the routes
            val entities = response.data.map { it.toEntity(backendId) }
            database.routeDao().insertRoutes(entities)
            
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
     * Logs are not cached as they change frequently
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
