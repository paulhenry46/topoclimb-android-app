package com.example.topoclimb.repository

import android.content.Context
import com.example.topoclimb.cache.CacheManager
import com.example.topoclimb.cache.CachePreferences
import com.example.topoclimb.data.Area
import com.example.topoclimb.data.AreasResponse
import com.example.topoclimb.data.Contest
import com.example.topoclimb.data.ContestsResponse
import com.example.topoclimb.data.Line
import com.example.topoclimb.data.Route
import com.example.topoclimb.data.RoutesResponse
import com.example.topoclimb.data.Sector
import com.example.topoclimb.data.SectorSchema
import com.example.topoclimb.data.Site
import com.example.topoclimb.data.SitesResponse
import com.example.topoclimb.network.RetrofitInstance

/**
 * Repository with cache support for non-federated usage
 */
class TopoClimbRepository(private val context: Context? = null, private val backendId: String = "default") {
    
    private val api = RetrofitInstance.api
    private val cacheManager: CacheManager? = context?.let { CacheManager(it) }
    private val cachePreferences: CachePreferences? = context?.let { CachePreferences(it) }
    
    suspend fun getSites(forceRefresh: Boolean = false): Result<SitesResponse> {
        return try {
            // Cache-first strategy
            if (!forceRefresh && cachePreferences?.isCacheEnabled == true) {
                val cached = cacheManager?.getCachedSites(backendId)
                if (cached != null) {
                    return Result.success(SitesResponse(cached))
                }
            }
            
            // Network fallback
            val response = api.getSites()
            
            // Cache the result if cache is enabled
            if (cachePreferences?.isCacheEnabled == true) {
                cacheManager?.cacheSites(response.data, backendId)
            }
            
            Result.success(response)
        } catch (e: Exception) {
            // If network fails, try to return cached data even if expired
            if (cachePreferences?.isCacheEnabled == true) {
                val cached = cacheManager?.getCachedSitesIgnoreExpiration(backendId)
                if (cached != null) {
                    return Result.success(SitesResponse(cached))
                }
            }
            Result.failure(e)
        }
    }
    
    suspend fun getSite(id: Int, forceRefresh: Boolean = false): Result<Site> {
        return try {
            // Cache-first strategy
            if (!forceRefresh && cachePreferences?.isCacheEnabled == true) {
                val cached = cacheManager?.getCachedSite(id, backendId)
                if (cached != null) {
                    return Result.success(cached)
                }
            }
            
            // Network fallback
            val response = api.getSite(id)
            
            // Cache the result if cache is enabled
            if (cachePreferences?.isCacheEnabled == true) {
                cacheManager?.cacheSite(response.data, backendId)
            }
            
            Result.success(response.data)
        } catch (e: Exception) {
            // If network fails, try to return cached data even if expired
            if (cachePreferences?.isCacheEnabled == true) {
                val cached = cacheManager?.getCachedSiteIgnoreExpiration(id, backendId)
                if (cached != null) {
                    return Result.success(cached)
                }
            }
            Result.failure(e)
        }
    }
    
    suspend fun getRoutes(
        siteId: Int? = null,
        grade: String? = null,
        type: String? = null
    ): Result<List<Route>> {
        return try {
            val response = api.getRoutes(siteId, grade, type)
            Result.success(response.data)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getRoute(id: Int): Result<Route> {
        return try {
            val response = api.getRoute(id)
            Result.success(response.data)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getAreas(): Result<List<Area>> {
        return try {
            val response = api.getAreas()
            Result.success(response.data)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getArea(id: Int, forceRefresh: Boolean = false): Result<Area> {
        return try {
            // Cache-first strategy
            if (!forceRefresh && cachePreferences?.isCacheEnabled == true) {
                val cached = cacheManager?.getCachedArea(id, backendId)
                if (cached != null) {
                    return Result.success(cached)
                }
            }
            
            // Network fallback
            val response = api.getArea(id)
            
            // Don't cache single areas here since they're cached via getAreasBySite
            // Caching here could overwrite correctly cached areas with incomplete data
            // (e.g., if API doesn't populate siteId for single area requests)
            
            Result.success(response.data)
        } catch (e: Exception) {
            // If network fails, try to return cached data even if expired
            if (cachePreferences?.isCacheEnabled == true) {
                val cached = cacheManager?.getCachedAreaIgnoreExpiration(id, backendId)
                if (cached != null) {
                    return Result.success(cached)
                }
            }
            Result.failure(e)
        }
    }
    
    suspend fun getRoutesByArea(areaId: Int): Result<List<Route>> {
        return try {
            val response = api.getRoutesByArea(areaId)
            Result.success(response.data)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getAreasBySite(siteId: Int): Result<AreasResponse> {
        return try {
            Result.success(api.getAreasBySite(siteId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getContestsBySite(siteId: Int): Result<ContestsResponse> {
        return try {
            Result.success(api.getContestsBySite(siteId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getSectorsByArea(areaId: Int, forceRefresh: Boolean = false): Result<List<Sector>> {
        return try {
            // Cache-first strategy
            if (!forceRefresh && cachePreferences?.isCacheEnabled == true) {
                val cached = cacheManager?.getCachedSectorsByArea(areaId, backendId)
                if (cached != null) {
                    return Result.success(cached)
                }
            }
            
            // Network call
            val response = api.getSectorsByArea(areaId)
            
            // Cache the result if cache is enabled
            if (cachePreferences?.isCacheEnabled == true) {
                cacheManager?.cacheSectorsByArea(response.data, areaId, backendId)
            }
            
            Result.success(response.data)
        } catch (e: Exception) {
            // If network fails, try to return cached data even if expired
            if (cachePreferences?.isCacheEnabled == true) {
                val cached = cacheManager?.getCachedSectorsByAreaIgnoreExpiration(areaId, backendId)
                if (cached != null) {
                    return Result.success(cached)
                }
            }
            Result.failure(e)
        }
    }
    
    suspend fun getLinesBySector(sectorId: Int, forceRefresh: Boolean = false): Result<List<Line>> {
        return try {
            val response = api.getLinesBySector(sectorId)
            Result.success(response.data)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getRoutesByLine(lineId: Int, forceRefresh: Boolean = false): Result<List<Route>> {
        return try {
            // Simple approach: just fetch from API
            // Caching by lineId was causing issues, so keep it simple
            val response = api.getRoutesByLine(lineId)
            Result.success(response.data)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getAreaSchemas(areaId: Int, forceRefresh: Boolean = false): Result<List<SectorSchema>> {
        return try {
            // Cache-first strategy
            if (!forceRefresh && cachePreferences?.isCacheEnabled == true) {
                val cached = cacheManager?.getCachedSchemasByArea(areaId, backendId)
                if (cached != null) {
                    return Result.success(cached)
                }
            }
            
            // Network fallback
            val response = api.getAreaSchemas(areaId)
            
            // Cache the result if cache is enabled
            if (cachePreferences?.isCacheEnabled == true) {
                cacheManager?.cacheSchemas(response, areaId, backendId)
            }
            
            Result.success(response)
        } catch (e: Exception) {
            // If network fails, try to return cached data even if expired
            if (cachePreferences?.isCacheEnabled == true) {
                val cached = cacheManager?.getCachedSchemasByAreaIgnoreExpiration(areaId, backendId)
                if (cached != null) {
                    return Result.success(cached)
                }
            }
            Result.failure(e)
        }
    }
}
