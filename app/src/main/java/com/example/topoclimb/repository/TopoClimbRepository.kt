package com.example.topoclimb.repository

import com.example.topoclimb.data.Area
import com.example.topoclimb.data.AreasResponse
import com.example.topoclimb.data.Contest
import com.example.topoclimb.data.ContestsResponse
import com.example.topoclimb.data.Route
import com.example.topoclimb.data.RoutesResponse
import com.example.topoclimb.data.Site
import com.example.topoclimb.data.SitesResponse
import com.example.topoclimb.network.RetrofitInstance

class TopoClimbRepository {
    
    private val api = RetrofitInstance.api
    
    suspend fun getSites(): Result<SitesResponse> {
        return try {
            Result.success(api.getSites())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getSite(id: Int): Result<Site> {
        return try {
            val response = api.getSite(id)
            Result.success(response.data)
        } catch (e: Exception) {
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
            Result.success(api.getRoute(id))
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
    
    suspend fun getArea(id: Int): Result<Area> {
        return try {
            val response = api.getArea(id)
            Result.success(response.data)
        } catch (e: Exception) {
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
}
