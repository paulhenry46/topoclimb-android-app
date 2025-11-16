package com.example.topoclimb.repository

import android.content.Context
import com.example.topoclimb.data.AreasResponse
import com.example.topoclimb.data.Area
import com.example.topoclimb.data.ContestsResponse
import com.example.topoclimb.data.Line
import com.example.topoclimb.data.Log
import com.example.topoclimb.data.Route
import com.example.topoclimb.data.Sector
import com.example.topoclimb.data.SectorSchema
import com.example.topoclimb.data.Site
import com.example.topoclimb.data.SitesResponse
import com.example.topoclimb.database.TopoClimbDatabase
import com.example.topoclimb.database.entities.LineFetchMetadataEntity
import com.example.topoclimb.database.entities.RouteLogsFetchMetadataEntity
import com.example.topoclimb.database.entities.toArea
import com.example.topoclimb.database.entities.toEntity
import com.example.topoclimb.database.entities.toLine
import com.example.topoclimb.database.entities.toLog
import com.example.topoclimb.database.entities.toRoute
import com.example.topoclimb.database.entities.toSector
import com.example.topoclimb.database.entities.toSite
import com.example.topoclimb.network.RetrofitInstance
import com.example.topoclimb.utils.CacheUtils
import com.example.topoclimb.utils.NetworkUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Repository with offline-first caching for sectors, lines, and routes
 * Note: This repository works with a single backend (non-federated)
 */
class TopoClimbRepository(private val context: Context? = null) {
    
    private val api = RetrofitInstance.api
    private val database = context?.let { TopoClimbDatabase.getDatabase(it) }
    
    // Background scope for non-blocking cache refreshes
    private val backgroundScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    // Backend ID for non-federated repository (using default)
    private val defaultBackendId = "default"
    
    suspend fun getSites(): Result<SitesResponse> = 
        safeApiCallDirect { api.getSites() }
    
    /**
     * Get a site with offline-first caching
     * Returns cached data first if available, then refreshes from network in background
     */
    suspend fun getSite(id: Int): Result<Site> {
        // If no database, fall back to direct API call
        if (database == null || context == null) {
            return safeApiCall { api.getSite(id) }
        }
        
        return try {
            // Get cached site
            val cachedSite = database.siteDao().getSite(id, defaultBackendId)
            android.util.Log.d("OfflineFirst", "getSite: id=$id, cached=${cachedSite != null}")
            
            // If no cache and online, fetch synchronously first
            if (cachedSite == null && NetworkUtils.isNetworkAvailable(context)) {
                try {
                    android.util.Log.d("OfflineFirst", "No cache for site $id, fetching from network")
                    val response = api.getSite(id)
                    val entity = response.data.toEntity(defaultBackendId)
                    database.siteDao().insertSite(entity)
                    android.util.Log.d("OfflineFirst", "Fetched and cached site $id")
                    return Result.success(response.data)
                } catch (e: Exception) {
                    // Network error - return failure as we have no cached data
                    android.util.Log.w("OfflineFirst", "Network error fetching site $id: ${e.message}")
                    return Result.failure(e)
                }
            }
            
            // If no cache and offline, return failure
            if (cachedSite == null) {
                android.util.Log.w("OfflineFirst", "No cache and offline for site $id")
                return Result.failure(Exception("No cached data available for site $id"))
            }
            
            // Return cached data immediately
            val result = cachedSite.toSite()
            android.util.Log.d("OfflineFirst", "Returning cached site $id")
            
            // Refresh in background if cache is stale
            if (NetworkUtils.isNetworkAvailable(context)) {
                val shouldRefresh = CacheUtils.isCacheStale(cachedSite.lastUpdated)
                if (shouldRefresh) {
                    backgroundScope.launch {
                        try {
                            val response = api.getSite(id)
                            val entity = response.data.toEntity(defaultBackendId)
                            database.siteDao().insertSite(entity)
                            android.util.Log.d("OfflineFirst", "Background refresh: Updated site $id")
                        } catch (e: Exception) {
                            android.util.Log.e("OfflineFirst", "Background refresh failed for site", e)
                        }
                    }
                } else {
                    android.util.Log.d("OfflineFirst", "Skipping refresh for site - cache is fresh")
                }
            }
            
            Result.success(result)
        } catch (e: Exception) {
            android.util.Log.e("OfflineFirst", "Error in getSite for site $id: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun getRoutes(
        siteId: Int? = null,
        grade: String? = null,
        type: String? = null
    ): Result<List<Route>> = 
        safeApiCallList { api.getRoutes(siteId, grade, type) }
    
    /**
     * Get a single route with offline-first caching
     * Returns cached data first if available, then refreshes from network in background
     */
    suspend fun getRoute(id: Int): Result<Route> {
        // If no database, fall back to direct API call
        if (database == null || context == null) {
            return safeApiCall { api.getRoute(id) }
        }
        
        return try {
            // Get cached route
            val cachedRoute = database.routeDao().getRoute(id, defaultBackendId)
            android.util.Log.d("OfflineFirst", "getRoute: id=$id, cached=${cachedRoute != null}")
            
            // If no cache and online, fetch synchronously first
            if (cachedRoute == null && NetworkUtils.isNetworkAvailable(context)) {
                try {
                    android.util.Log.d("OfflineFirst", "No cache for route $id, fetching from network")
                    val response = api.getRoute(id)
                    val entity = response.data.toEntity(defaultBackendId)
                    database.routeDao().insertRoute(entity)
                    android.util.Log.d("OfflineFirst", "Fetched and cached route $id")
                    return Result.success(response.data)
                } catch (e: Exception) {
                    // Network error - return failure as we have no cached data
                    android.util.Log.w("OfflineFirst", "Network error fetching route $id: ${e.message}")
                    return Result.failure(e)
                }
            }
            
            // If no cache and offline, return failure
            if (cachedRoute == null) {
                android.util.Log.w("OfflineFirst", "No cache and offline for route $id")
                return Result.failure(Exception("No cached data available for route $id"))
            }
            
            // Return cached data immediately
            val result = cachedRoute.toRoute()
            android.util.Log.d("OfflineFirst", "Returning cached route $id")
            
            // Refresh in background if cache is stale
            if (NetworkUtils.isNetworkAvailable(context)) {
                val shouldRefresh = CacheUtils.isCacheStale(cachedRoute.lastUpdated)
                if (shouldRefresh) {
                    backgroundScope.launch {
                        try {
                            val response = api.getRoute(id)
                            val entity = response.data.toEntity(defaultBackendId)
                            database.routeDao().insertRoute(entity)
                            android.util.Log.d("OfflineFirst", "Background refresh: Updated route $id")
                        } catch (e: Exception) {
                            android.util.Log.e("OfflineFirst", "Background refresh failed for route", e)
                        }
                    }
                } else {
                    android.util.Log.d("OfflineFirst", "Skipping refresh for route - cache is fresh")
                }
            }
            
            Result.success(result)
        } catch (e: Exception) {
            android.util.Log.e("OfflineFirst", "Error in getRoute for route $id: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun getAreas(): Result<List<Area>> = 
        safeApiCallList { api.getAreas() }
    
    /**
     * Get an area with offline-first caching
     * Returns cached data first if available, then refreshes from network in background
     */
    suspend fun getArea(id: Int): Result<Area> {
        // If no database, fall back to direct API call
        if (database == null || context == null) {
            return safeApiCall { api.getArea(id) }
        }
        
        return try {
            // Get cached area
            val cachedArea = database.areaDao().getArea(id, defaultBackendId)
            android.util.Log.d("OfflineFirst", "getArea: id=$id, cached=${cachedArea != null}")
            
            // If no cache and online, fetch synchronously first
            if (cachedArea == null && NetworkUtils.isNetworkAvailable(context)) {
                try {
                    android.util.Log.d("OfflineFirst", "No cache for area $id, fetching from network")
                    val response = api.getArea(id)
                    val entity = response.data.toEntity(defaultBackendId)
                    database.areaDao().insertArea(entity)
                    android.util.Log.d("OfflineFirst", "Fetched and cached area $id")
                    return Result.success(response.data)
                } catch (e: Exception) {
                    // Network error - return failure as we have no cached data
                    android.util.Log.w("OfflineFirst", "Network error fetching area $id: ${e.message}")
                    return Result.failure(e)
                }
            }
            
            // If no cache and offline, return failure
            if (cachedArea == null) {
                android.util.Log.w("OfflineFirst", "No cache and offline for area $id")
                return Result.failure(Exception("No cached data available for area $id"))
            }
            
            // Return cached data immediately
            val result = cachedArea.toArea()
            android.util.Log.d("OfflineFirst", "Returning cached area $id")
            
            // Refresh in background if cache is stale
            if (NetworkUtils.isNetworkAvailable(context)) {
                val shouldRefresh = CacheUtils.isCacheStale(cachedArea.lastUpdated)
                if (shouldRefresh) {
                    backgroundScope.launch {
                        try {
                            val response = api.getArea(id)
                            val entity = response.data.toEntity(defaultBackendId)
                            database.areaDao().insertArea(entity)
                            android.util.Log.d("OfflineFirst", "Background refresh: Updated area $id")
                        } catch (e: Exception) {
                            android.util.Log.e("OfflineFirst", "Background refresh failed for area", e)
                        }
                    }
                } else {
                    android.util.Log.d("OfflineFirst", "Skipping refresh for area - cache is fresh")
                }
            }
            
            Result.success(result)
        } catch (e: Exception) {
            android.util.Log.e("OfflineFirst", "Error in getArea for area $id: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun getRoutesByArea(areaId: Int): Result<List<Route>> = 
        safeApiCallList { api.getRoutesByArea(areaId) }
    
    suspend fun getAreasBySite(siteId: Int): Result<AreasResponse> = 
        safeApiCallDirect { api.getAreasBySite(siteId) }
    
    suspend fun getContestsBySite(siteId: Int): Result<ContestsResponse> = 
        safeApiCallDirect { api.getContestsBySite(siteId) }
    
    /**
     * Get sectors for an area with offline-first caching
     * Returns cached data first, then refreshes from network if cache is stale or forced
     */
    suspend fun getSectorsByArea(areaId: Int, forceRefresh: Boolean = false): Result<List<Sector>> {
        // If no database, fall back to direct API call
        if (database == null || context == null) {
            return safeApiCallList { api.getSectorsByArea(areaId) }
        }
        
        return try {
            // Get cached sectors
            val cachedSectors = database.sectorDao().getSectorsByArea(areaId, defaultBackendId)
            android.util.Log.d("OfflineFirst", "getSectorsByArea: areaId=$areaId, cached count=${cachedSectors.size}")
            
            // If no cache and online, fetch synchronously first
            if (cachedSectors.isEmpty() && NetworkUtils.isNetworkAvailable(context)) {
                try {
                    android.util.Log.d("OfflineFirst", "No cache for area $areaId, fetching from network")
                    val response = api.getSectorsByArea(areaId)
                    android.util.Log.d("OfflineFirst", "API returned ${response.data.size} sectors for area $areaId")
                    
                    // Convert to entities with null checks and logging, override areaId
                    val entities = response.data.mapNotNull { sector ->
                        try {
                            sector.toEntity(defaultBackendId, areaId)  // Pass correct areaId
                        } catch (e: Exception) {
                            android.util.Log.e("OfflineFirst", "Failed to convert sector ${sector.id} to entity: ${e.message}. Sector data: id=${sector.id}, name='${sector.name}', areaId=${sector.areaId}")
                            null  // Skip sectors that can't be converted
                        }
                    }
                    
                    // Cache even if empty - this marks that we fetched and got nothing
                    database.sectorDao().insertSectors(entities)
                    android.util.Log.d("OfflineFirst", "Fetched and cached ${entities.size} sectors for area $areaId")
                    // Log a sample to verify correct areaId
                    entities.firstOrNull()?.let { 
                        android.util.Log.d("OfflineFirst", "Sample cached sector: id=${it.id}, name='${it.name}', areaId=${it.areaId} (should be $areaId)")
                    }
                    
                    return Result.success(response.data)
                } catch (e: Exception) {
                    // Network error - return empty gracefully (no cache available)
                    android.util.Log.w("OfflineFirst", "Network error fetching sectors for area $areaId, returning empty: ${e.message}")
                    return Result.success(emptyList())
                }
            }
            
            // If no cache and offline, return empty
            if (cachedSectors.isEmpty()) {
                android.util.Log.w("OfflineFirst", "No cache and offline for area $areaId - returning empty")
                return Result.success(emptyList())
            }
            
            // Return cached data immediately
            val result = cachedSectors.map { it.toSector() }
            android.util.Log.d("OfflineFirst", "Returning ${result.size} cached sectors for area $areaId")
            
            // Refresh in background if cache is stale or forced
            if (NetworkUtils.isNetworkAvailable(context)) {
                val shouldRefresh = forceRefresh || CacheUtils.isAnyCacheStale(cachedSectors) { it.lastUpdated }
                if (shouldRefresh) {
                    backgroundScope.launch {
                        try {
                            val response = api.getSectorsByArea(areaId)
                            val entities = response.data.mapNotNull { sector ->
                                try {
                                    sector.toEntity(defaultBackendId, areaId)  // Pass correct areaId
                                } catch (e: Exception) {
                                    android.util.Log.e("OfflineFirst", "Failed to convert sector ${sector.id} in background refresh: ${e.message}")
                                    null
                                }
                            }
                            // Cache even if empty
                            database.sectorDao().insertSectors(entities)
                            android.util.Log.d("OfflineFirst", "Background refresh: Updated ${entities.size} sectors for area $areaId (forceRefresh=$forceRefresh)")
                        } catch (e: Exception) {
                            android.util.Log.e("OfflineFirst", "Background refresh failed for sectors", e)
                        }
                    }
                } else {
                    android.util.Log.d("OfflineFirst", "Skipping refresh for sectors - cache is fresh")
                }
            }
            
            Result.success(result)
        } catch (e: Exception) {
            // Unexpected error - log but return empty instead of failure
            android.util.Log.e("OfflineFirst", "Error in getSectorsByArea for area $areaId, returning empty: ${e.message}", e)
            Result.success(emptyList())
        }
    }
    
    /**
     * Get lines for a sector with offline-first caching
     * Returns cached data first, then refreshes from network if cache is stale or forced
     */
    suspend fun getLinesBySector(sectorId: Int, forceRefresh: Boolean = false): Result<List<Line>> {
        // If no database, fall back to direct API call
        if (database == null || context == null) {
            return safeApiCallList { api.getLinesBySector(sectorId) }
        }
        
        return try {
            // Get cached lines
            val cachedLines = database.lineDao().getLinesBySector(sectorId, defaultBackendId)
            android.util.Log.d("OfflineFirst", "getLinesBySector: sectorId=$sectorId, cached count=${cachedLines.size}")
            
            // If no cache and online, fetch synchronously first
            if (cachedLines.isEmpty() && NetworkUtils.isNetworkAvailable(context)) {
                try {
                    android.util.Log.d("OfflineFirst", "No cache for sector $sectorId, fetching from network")
                    val response = api.getLinesBySector(sectorId)
                    android.util.Log.d("OfflineFirst", "API returned ${response.data.size} lines for sector $sectorId")
                    
                    // Convert to entities with null checks and logging, override sectorId
                    val entities = response.data.mapNotNull { line ->
                        try {
                            line.toEntity(defaultBackendId, sectorId)  // Pass correct sectorId
                        } catch (e: Exception) {
                            android.util.Log.e("OfflineFirst", "Failed to convert line ${line.id} to entity: ${e.message}. Line data: id=${line.id}, name='${line.name}', sectorId=${line.sectorId}")
                            null  // Skip lines that can't be converted
                        }
                    }
                    
                    // Cache even if empty - this marks that we fetched and got nothing
                    database.lineDao().insertLines(entities)
                    android.util.Log.d("OfflineFirst", "Fetched and cached ${entities.size} lines for sector $sectorId")
                    // Log a sample to verify correct sectorId
                    entities.firstOrNull()?.let {
                        android.util.Log.d("OfflineFirst", "Sample cached line: id=${it.id}, name='${it.name}', sectorId=${it.sectorId} (should be $sectorId)")
                    }
                    
                    return Result.success(response.data)
                } catch (e: Exception) {
                    // Network error - return empty gracefully (no cache available)
                    android.util.Log.w("OfflineFirst", "Network error fetching lines for sector $sectorId, returning empty: ${e.message}")
                    return Result.success(emptyList())
                }
            }
            
            // If no cache and offline, return empty
            if (cachedLines.isEmpty()) {
                android.util.Log.w("OfflineFirst", "No cache and offline for sector $sectorId - returning empty")
                return Result.success(emptyList())
            }
            
            // Return cached data immediately
            val result = cachedLines.map { it.toLine() }
            android.util.Log.d("OfflineFirst", "Returning ${result.size} cached lines for sector $sectorId")
            
            // Refresh in background if cache is stale or forced
            if (NetworkUtils.isNetworkAvailable(context)) {
                val shouldRefresh = forceRefresh || CacheUtils.isAnyCacheStale(cachedLines) { it.lastUpdated }
                if (shouldRefresh) {
                    backgroundScope.launch {
                        try {
                            val response = api.getLinesBySector(sectorId)
                            val entities = response.data.mapNotNull { line ->
                                try {
                                    line.toEntity(defaultBackendId, sectorId)  // Pass correct sectorId
                                } catch (e: Exception) {
                                    android.util.Log.e("OfflineFirst", "Failed to convert line ${line.id} in background refresh: ${e.message}")
                                    null
                                }
                            }
                            // Cache even if empty
                            database.lineDao().insertLines(entities)
                            android.util.Log.d("OfflineFirst", "Background refresh: Updated ${entities.size} lines for sector $sectorId (forceRefresh=$forceRefresh)")
                        } catch (e: Exception) {
                            android.util.Log.e("OfflineFirst", "Background refresh failed for lines", e)
                        }
                    }
                } else {
                    android.util.Log.d("OfflineFirst", "Skipping refresh for lines - cache is fresh")
                }
            }
            
            Result.success(result)
        } catch (e: Exception) {
            // Unexpected error - log but return empty instead of failure
            android.util.Log.e("OfflineFirst", "Error in getLinesBySector for sector $sectorId, returning empty: ${e.message}", e)
            Result.success(emptyList())
        }
    }
    
    /**
     * Get routes for a line with offline-first caching
     * Returns cached data first, then refreshes from network if cache is stale or forced
     * Note: This caches routes by lineId, separate from the general routes cache
     * Uses metadata to track whether a line has been fetched (even if it has 0 routes)
     */
    suspend fun getRoutesByLine(lineId: Int, forceRefresh: Boolean = false): Result<List<Route>> {
        // If no database, fall back to direct API call
        if (database == null || context == null) {
            return safeApiCallList { api.getRoutesByLine(lineId) }
        }
        
        return try {
            // Get cached routes for this line
            val cachedRoutes = database.routeDao().getRoutesByLine(lineId, defaultBackendId)
            // Check if we've fetched this line before (even if it has 0 routes)
            val fetchMetadata = database.lineFetchMetadataDao().getLineFetchMetadata(lineId, defaultBackendId)
            
            android.util.Log.d("OfflineFirst", "getRoutesByLine: lineId=$lineId, cached count=${cachedRoutes.size}, hasFetchMetadata=${fetchMetadata != null}")
            
            // If no metadata (never fetched) and online, fetch synchronously first
            if (fetchMetadata == null && NetworkUtils.isNetworkAvailable(context)) {
                try {
                    android.util.Log.d("OfflineFirst", "No cache metadata for line $lineId, fetching from network")
                    val response = api.getRoutesByLine(lineId)
                    android.util.Log.d("OfflineFirst", "API returned ${response.data.size} routes for line $lineId")
                    
                    // Convert to entities, even if empty - this marks that we fetched and got nothing
                    val entities = response.data.map { it.toEntity(defaultBackendId, lineId) }
                    
                    // Cache the routes (even if empty)
                    database.routeDao().insertRoutes(entities)
                    // Mark that we've fetched this line
                    database.lineFetchMetadataDao().insertLineFetchMetadata(
                        LineFetchMetadataEntity(lineId = lineId, backendId = defaultBackendId)
                    )
                    android.util.Log.d("OfflineFirst", "Fetched and cached ${entities.size} routes for line $lineId with metadata")
                    
                    return Result.success(response.data)
                } catch (e: Exception) {
                    // Network error - return empty gracefully (no cache available)
                    android.util.Log.w("OfflineFirst", "Network error fetching routes for line $lineId, returning empty: ${e.message}")
                    return Result.success(emptyList())
                }
            }
            
            // If no metadata and offline, return empty
            if (fetchMetadata == null) {
                android.util.Log.w("OfflineFirst", "No cache metadata and offline for line $lineId - returning empty")
                return Result.success(emptyList())
            }
            
            // We have metadata, so we've fetched before - return cached data (even if empty)
            val result = cachedRoutes.map { it.toRoute() }
            android.util.Log.d("OfflineFirst", "Returning ${result.size} cached routes for line $lineId (fetched previously)")
            
            // Refresh in background if cache is stale or forced
            if (NetworkUtils.isNetworkAvailable(context)) {
                val shouldRefresh = forceRefresh || CacheUtils.isCacheStale(fetchMetadata.lastFetched)
                if (shouldRefresh) {
                    backgroundScope.launch {
                        try {
                            val response = api.getRoutesByLine(lineId)
                            val entities = response.data.map { it.toEntity(defaultBackendId, lineId) }
                            database.routeDao().insertRoutes(entities)
                            // Update metadata timestamp
                            database.lineFetchMetadataDao().insertLineFetchMetadata(
                                LineFetchMetadataEntity(lineId = lineId, backendId = defaultBackendId)
                            )
                            android.util.Log.d("OfflineFirst", "Background refresh: Updated ${entities.size} routes for line $lineId (forceRefresh=$forceRefresh)")
                        } catch (e: Exception) {
                            android.util.Log.e("OfflineFirst", "Background refresh failed for routes", e)
                        }
                    }
                } else {
                    android.util.Log.d("OfflineFirst", "Skipping refresh for routes - cache is fresh")
                }
            }
            
            Result.success(result)
        } catch (e: Exception) {
            // Unexpected error - log but return empty instead of failure
            android.util.Log.e("OfflineFirst", "Error in getRoutesByLine for line $lineId, returning empty: ${e.message}", e)
            Result.success(emptyList())
        }
    }
    
    suspend fun getAreaSchemas(areaId: Int): Result<List<SectorSchema>> = 
        safeApiCallDirect { api.getAreaSchemas(areaId) }
    
    /**
     * Get logs for a route with offline-first caching (1 day TTL)
     * Returns cached data first, then refreshes from network if cache is stale or forced
     * Uses metadata to track whether a route's logs have been fetched (even if it has 0 logs)
     */
    suspend fun getRouteLogs(routeId: Int, forceRefresh: Boolean = false): Result<List<Log>> {
        // If no database, fall back to direct API call
        if (database == null || context == null) {
            return safeApiCallList { api.getRouteLogs(routeId) }
        }
        
        return try {
            // Get cached logs for this route
            val cachedLogs = database.logDao().getLogsByRoute(routeId, defaultBackendId)
            // Check if we've fetched this route's logs before (even if it has 0 logs)
            val fetchMetadata = database.routeLogsFetchMetadataDao().getRouteLogsFetchMetadata(routeId, defaultBackendId)
            
            android.util.Log.d("OfflineFirst", "getRouteLogs: routeId=$routeId, cached count=${cachedLogs.size}, hasFetchMetadata=${fetchMetadata != null}")
            
            // If no metadata (never fetched) and online, fetch synchronously first
            if (fetchMetadata == null && NetworkUtils.isNetworkAvailable(context)) {
                try {
                    android.util.Log.d("OfflineFirst", "No cache metadata for route logs $routeId, fetching from network")
                    val response = api.getRouteLogs(routeId)
                    android.util.Log.d("OfflineFirst", "API returned ${response.data.size} logs for route $routeId")
                    
                    // Convert to entities, even if empty
                    val entities = response.data.map { it.toEntity(defaultBackendId) }
                    
                    // Cache the logs (even if empty)
                    database.logDao().insertLogs(entities)
                    // Mark that we've fetched this route's logs
                    database.routeLogsFetchMetadataDao().insertRouteLogsFetchMetadata(
                        RouteLogsFetchMetadataEntity(routeId = routeId, backendId = defaultBackendId)
                    )
                    android.util.Log.d("OfflineFirst", "Fetched and cached ${entities.size} logs for route $routeId with metadata")
                    
                    return Result.success(response.data)
                } catch (e: Exception) {
                    // Network error - return empty gracefully (no cache available)
                    android.util.Log.w("OfflineFirst", "Network error fetching logs for route $routeId, returning empty: ${e.message}")
                    return Result.success(emptyList())
                }
            }
            
            // If no metadata and offline, return empty
            if (fetchMetadata == null) {
                android.util.Log.w("OfflineFirst", "No cache metadata and offline for route logs $routeId - returning empty")
                return Result.success(emptyList())
            }
            
            // We have metadata, so we've fetched before - return cached data (even if empty)
            val result = cachedLogs.map { it.toLog() }
            android.util.Log.d("OfflineFirst", "Returning ${result.size} cached logs for route $routeId (fetched previously)")
            
            // Refresh in background if cache is stale or forced
            if (NetworkUtils.isNetworkAvailable(context)) {
                val shouldRefresh = forceRefresh || CacheUtils.isCacheStale(fetchMetadata.lastFetched)
                if (shouldRefresh) {
                    backgroundScope.launch {
                        try {
                            val response = api.getRouteLogs(routeId)
                            val entities = response.data.map { it.toEntity(defaultBackendId) }
                            database.logDao().insertLogs(entities)
                            // Update metadata timestamp
                            database.routeLogsFetchMetadataDao().insertRouteLogsFetchMetadata(
                                RouteLogsFetchMetadataEntity(routeId = routeId, backendId = defaultBackendId)
                            )
                            android.util.Log.d("OfflineFirst", "Background refresh: Updated ${entities.size} logs for route $routeId (forceRefresh=$forceRefresh)")
                        } catch (e: Exception) {
                            android.util.Log.e("OfflineFirst", "Background refresh failed for logs", e)
                        }
                    }
                } else {
                    android.util.Log.d("OfflineFirst", "Skipping refresh for logs - cache is fresh")
                }
            }
            
            Result.success(result)
        } catch (e: Exception) {
            // Unexpected error - log but return empty instead of failure
            android.util.Log.e("OfflineFirst", "Error in getRouteLogs for route $routeId, returning empty: ${e.message}", e)
            Result.success(emptyList())
        }
    }
}
