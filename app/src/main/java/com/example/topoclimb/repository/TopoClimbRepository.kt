package com.example.topoclimb.repository

import android.content.Context
import com.example.topoclimb.data.AreasResponse
import com.example.topoclimb.data.Area
import com.example.topoclimb.data.ContestsResponse
import com.example.topoclimb.data.Line
import com.example.topoclimb.data.Route
import com.example.topoclimb.data.Sector
import com.example.topoclimb.data.SectorSchema
import com.example.topoclimb.data.Site
import com.example.topoclimb.data.SitesResponse
import com.example.topoclimb.database.TopoClimbDatabase
import com.example.topoclimb.database.entities.toEntity
import com.example.topoclimb.database.entities.toLine
import com.example.topoclimb.database.entities.toRoute
import com.example.topoclimb.database.entities.toSector
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
    
    suspend fun getSite(id: Int): Result<Site> = 
        safeApiCall { api.getSite(id) }
    
    suspend fun getRoutes(
        siteId: Int? = null,
        grade: String? = null,
        type: String? = null
    ): Result<List<Route>> = 
        safeApiCallList { api.getRoutes(siteId, grade, type) }
    
    suspend fun getRoute(id: Int): Result<Route> = 
        safeApiCall { api.getRoute(id) }
    
    suspend fun getAreas(): Result<List<Area>> = 
        safeApiCallList { api.getAreas() }
    
    suspend fun getArea(id: Int): Result<Area> = 
        safeApiCall { api.getArea(id) }
    
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
                android.util.Log.d("OfflineFirst", "No cache for area $areaId, fetching from network")
                val response = api.getSectorsByArea(areaId)
                android.util.Log.d("OfflineFirst", "API returned ${response.data.size} sectors for area $areaId")
                
                // Convert to entities with null checks and logging
                val entities = response.data.mapNotNull { sector ->
                    try {
                        sector.toEntity(defaultBackendId)
                    } catch (e: Exception) {
                        android.util.Log.e("OfflineFirst", "Failed to convert sector ${sector.id} to entity: ${e.message}. Sector data: id=${sector.id}, name='${sector.name}', areaId=${sector.areaId}")
                        null  // Skip sectors that can't be converted
                    }
                }
                
                if (entities.isNotEmpty()) {
                    database.sectorDao().insertSectors(entities)
                    android.util.Log.d("OfflineFirst", "Fetched and cached ${entities.size} sectors for area $areaId")
                }
                
                return Result.success(response.data)
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
                                    sector.toEntity(defaultBackendId)
                                } catch (e: Exception) {
                                    android.util.Log.e("OfflineFirst", "Failed to convert sector ${sector.id} in background refresh: ${e.message}")
                                    null
                                }
                            }
                            if (entities.isNotEmpty()) {
                                database.sectorDao().insertSectors(entities)
                                android.util.Log.d("OfflineFirst", "Background refresh: Updated ${entities.size} sectors for area $areaId (forceRefresh=$forceRefresh)")
                            }
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
            android.util.Log.e("OfflineFirst", "Error in getSectorsByArea", e)
            Result.failure(e)
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
                android.util.Log.d("OfflineFirst", "No cache for sector $sectorId, fetching from network")
                val response = api.getLinesBySector(sectorId)
                android.util.Log.d("OfflineFirst", "API returned ${response.data.size} lines for sector $sectorId")
                
                // Convert to entities with null checks and logging
                val entities = response.data.mapNotNull { line ->
                    try {
                        line.toEntity(defaultBackendId)
                    } catch (e: Exception) {
                        android.util.Log.e("OfflineFirst", "Failed to convert line ${line.id} to entity: ${e.message}. Line data: id=${line.id}, name='${line.name}', sectorId=${line.sectorId}")
                        null  // Skip lines that can't be converted
                    }
                }
                
                if (entities.isNotEmpty()) {
                    database.lineDao().insertLines(entities)
                    android.util.Log.d("OfflineFirst", "Fetched and cached ${entities.size} lines for sector $sectorId")
                }
                
                return Result.success(response.data)
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
                                    line.toEntity(defaultBackendId)
                                } catch (e: Exception) {
                                    android.util.Log.e("OfflineFirst", "Failed to convert line ${line.id} in background refresh: ${e.message}")
                                    null
                                }
                            }
                            if (entities.isNotEmpty()) {
                                database.lineDao().insertLines(entities)
                                android.util.Log.d("OfflineFirst", "Background refresh: Updated ${entities.size} lines for sector $sectorId (forceRefresh=$forceRefresh)")
                            }
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
            android.util.Log.e("OfflineFirst", "Error in getLinesBySector", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get routes for a line with offline-first caching
     * Returns cached data first, then refreshes from network if cache is stale or forced
     * Note: This caches routes by lineId, separate from the general routes cache
     */
    suspend fun getRoutesByLine(lineId: Int, forceRefresh: Boolean = false): Result<List<Route>> {
        // If no database, fall back to direct API call
        if (database == null || context == null) {
            return safeApiCallList { api.getRoutesByLine(lineId) }
        }
        
        return try {
            // Get cached routes for this line
            val cachedRoutes = database.routeDao().getRoutesByLine(lineId, defaultBackendId)
            android.util.Log.d("OfflineFirst", "getRoutesByLine: lineId=$lineId, cached count=${cachedRoutes.size}")
            
            // If no cache and online, fetch synchronously first
            if (cachedRoutes.isEmpty() && NetworkUtils.isNetworkAvailable(context)) {
                android.util.Log.d("OfflineFirst", "No cache for line $lineId, fetching from network")
                val response = api.getRoutesByLine(lineId)
                val entities = response.data.map { it.toEntity(defaultBackendId, lineId) }
                database.routeDao().insertRoutes(entities)
                android.util.Log.d("OfflineFirst", "Fetched and cached ${entities.size} routes for line $lineId")
                return Result.success(response.data)
            }
            
            // If no cache and offline, return empty
            if (cachedRoutes.isEmpty()) {
                android.util.Log.w("OfflineFirst", "No cache and offline for line $lineId - returning empty")
                return Result.success(emptyList())
            }
            
            // Return cached data immediately
            val result = cachedRoutes.map { it.toRoute() }
            android.util.Log.d("OfflineFirst", "Returning ${result.size} cached routes for line $lineId")
            
            // Refresh in background if cache is stale or forced
            if (NetworkUtils.isNetworkAvailable(context)) {
                val shouldRefresh = forceRefresh || CacheUtils.isAnyCacheStale(cachedRoutes) { it.lastUpdated }
                if (shouldRefresh) {
                    backgroundScope.launch {
                        try {
                            val response = api.getRoutesByLine(lineId)
                            val entities = response.data.map { it.toEntity(defaultBackendId, lineId) }
                            database.routeDao().insertRoutes(entities)
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
            android.util.Log.e("OfflineFirst", "Error in getRoutesByLine", e)
            Result.failure(e)
        }
    }
    
    suspend fun getAreaSchemas(areaId: Int): Result<List<SectorSchema>> = 
        safeApiCallDirect { api.getAreaSchemas(areaId) }
}
