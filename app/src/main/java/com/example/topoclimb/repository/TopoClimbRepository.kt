package com.example.topoclimb.repository

import com.example.topoclimb.data.Area
import com.example.topoclimb.data.Route
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
            Result.success(api.getSite(id))
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
            Result.success(api.getRoutes(siteId, grade, type))
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
            Result.success(api.getAreas())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getArea(id: Int): Result<Area> {
        return try {
            Result.success(api.getArea(id))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
