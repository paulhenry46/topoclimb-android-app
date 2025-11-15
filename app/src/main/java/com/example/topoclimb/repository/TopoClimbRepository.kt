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
            
            // Return cached data immediately
            val result = cachedSectors.map { it.toSector() }
            
            // Refresh in background if needed
            if (NetworkUtils.isNetworkAvailable(context) && cachedSectors.isNotEmpty()) {
                val shouldRefresh = forceRefresh || CacheUtils.isAnyCacheStale(cachedSectors) { it.lastUpdated }
                if (shouldRefresh) {
                    backgroundScope.launch {
                        try {
                            val response = api.getSectorsByArea(areaId)
                            val entities = response.data.map { it.toEntity(defaultBackendId) }
                            database.sectorDao().insertSectors(entities)
                            android.util.Log.d("OfflineFirst", "Background refresh: Updated ${entities.size} sectors for area $areaId")
                        } catch (e: Exception) {
                            android.util.Log.e("OfflineFirst", "Background refresh failed for sectors", e)
                        }
                    }
                } else {
                    android.util.Log.d("OfflineFirst", "Skipping refresh for sectors - cache is fresh")
                }
            }
            
            // If no cache and online, fetch synchronously
            if (result.isEmpty() && NetworkUtils.isNetworkAvailable(context)) {
                val response = api.getSectorsByArea(areaId)
                val entities = response.data.map { it.toEntity(defaultBackendId) }
                database.sectorDao().insertSectors(entities)
                android.util.Log.d("OfflineFirst", "Fetched and cached ${entities.size} sectors for area $areaId")
                return Result.success(response.data)
            }
            
            Result.success(result)
        } catch (e: Exception) {
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
            
            // Return cached data immediately
            val result = cachedLines.map { it.toLine() }
            
            // Refresh in background if needed
            if (NetworkUtils.isNetworkAvailable(context) && cachedLines.isNotEmpty()) {
                val shouldRefresh = forceRefresh || CacheUtils.isAnyCacheStale(cachedLines) { it.lastUpdated }
                if (shouldRefresh) {
                    backgroundScope.launch {
                        try {
                            val response = api.getLinesBySector(sectorId)
                            val entities = response.data.map { it.toEntity(defaultBackendId) }
                            database.lineDao().insertLines(entities)
                            android.util.Log.d("OfflineFirst", "Background refresh: Updated ${entities.size} lines for sector $sectorId")
                        } catch (e: Exception) {
                            android.util.Log.e("OfflineFirst", "Background refresh failed for lines", e)
                        }
                    }
                } else {
                    android.util.Log.d("OfflineFirst", "Skipping refresh for lines - cache is fresh")
                }
            }
            
            // If no cache and online, fetch synchronously
            if (result.isEmpty() && NetworkUtils.isNetworkAvailable(context)) {
                val response = api.getLinesBySector(sectorId)
                val entities = response.data.map { it.toEntity(defaultBackendId) }
                database.lineDao().insertLines(entities)
                android.util.Log.d("OfflineFirst", "Fetched and cached ${entities.size} lines for sector $sectorId")
                return Result.success(response.data)
            }
            
            Result.success(result)
        } catch (e: Exception) {
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
            
            // Return cached data immediately
            val result = cachedRoutes.map { it.toRoute() }
            
            // Refresh in background if needed
            if (NetworkUtils.isNetworkAvailable(context) && cachedRoutes.isNotEmpty()) {
                val shouldRefresh = forceRefresh || CacheUtils.isAnyCacheStale(cachedRoutes) { it.lastUpdated }
                if (shouldRefresh) {
                    backgroundScope.launch {
                        try {
                            val response = api.getRoutesByLine(lineId)
                            val entities = response.data.map { it.toEntity(defaultBackendId, lineId) }
                            database.routeDao().insertRoutes(entities)
                            android.util.Log.d("OfflineFirst", "Background refresh: Updated ${entities.size} routes for line $lineId")
                        } catch (e: Exception) {
                            android.util.Log.e("OfflineFirst", "Background refresh failed for routes", e)
                        }
                    }
                } else {
                    android.util.Log.d("OfflineFirst", "Skipping refresh for routes - cache is fresh")
                }
            }
            
            // If no cache and online, fetch synchronously
            if (result.isEmpty() && NetworkUtils.isNetworkAvailable(context)) {
                val response = api.getRoutesByLine(lineId)
                val entities = response.data.map { it.toEntity(defaultBackendId, lineId) }
                database.routeDao().insertRoutes(entities)
                android.util.Log.d("OfflineFirst", "Fetched and cached ${entities.size} routes for line $lineId")
                return Result.success(response.data)
            }
            
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getAreaSchemas(areaId: Int): Result<List<SectorSchema>> = 
        safeApiCallDirect { api.getAreaSchemas(areaId) }
}
