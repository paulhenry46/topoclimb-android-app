package com.example.topoclimb.repository

import com.example.topoclimb.data.AreasResponse
import com.example.topoclimb.data.Area
import com.example.topoclimb.data.ContestsResponse
import com.example.topoclimb.data.Line
import com.example.topoclimb.data.Route
import com.example.topoclimb.data.Sector
import com.example.topoclimb.data.SectorSchema
import com.example.topoclimb.data.Site
import com.example.topoclimb.data.SitesResponse
import com.example.topoclimb.network.RetrofitInstance

class TopoClimbRepository {
    
    private val api = RetrofitInstance.api
    
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
    
    suspend fun getSectorsByArea(areaId: Int): Result<List<Sector>> = 
        safeApiCallList { api.getSectorsByArea(areaId) }
    
    suspend fun getLinesBySector(sectorId: Int): Result<List<Line>> = 
        safeApiCallList { api.getLinesBySector(sectorId) }
    
    suspend fun getRoutesByLine(lineId: Int): Result<List<Route>> = 
        safeApiCallList { api.getRoutesByLine(lineId) }
    
    suspend fun getAreaSchemas(areaId: Int): Result<List<SectorSchema>> = 
        safeApiCallDirect { api.getAreaSchemas(areaId) }
}
