package com.example.topoclimb.network

import com.example.topoclimb.data.Area
import com.example.topoclimb.data.AreasResponse
import com.example.topoclimb.data.Contest
import com.example.topoclimb.data.ContestsResponse
import com.example.topoclimb.data.Route
import com.example.topoclimb.data.Site
import com.example.topoclimb.data.SitesResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TopoClimbApiService {
    
    @GET("sites")
    suspend fun getSites(): SitesResponse
    
    @GET("sites/{id}")
    suspend fun getSite(@Path("id") id: Int): Site
    
    @GET("routes")
    suspend fun getRoutes(
        @Query("siteId") siteId: Int? = null,
        @Query("grade") grade: String? = null,
        @Query("type") type: String? = null
    ): List<Route>
    
    @GET("routes/{id}")
    suspend fun getRoute(@Path("id") id: Int): Route
    
    @GET("areas")
    suspend fun getAreas(): List<Area>
    
    @GET("areas/{id}")
    suspend fun getArea(@Path("id") id: Int): Area
    
    @GET("sites/{siteId}/areas")
    suspend fun getAreasBySite(@Path("siteId") siteId: Int): AreasResponse
    
    @GET("sites/{siteId}/contests")
    suspend fun getContestsBySite(@Path("siteId") siteId: Int): ContestsResponse
}
