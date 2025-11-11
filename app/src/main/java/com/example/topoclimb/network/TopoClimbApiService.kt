package com.example.topoclimb.network

import com.example.topoclimb.data.Area
import com.example.topoclimb.data.AreaResponse
import com.example.topoclimb.data.AreasResponse
import com.example.topoclimb.data.AuthResponse
import com.example.topoclimb.data.Contest
import com.example.topoclimb.data.ContestsResponse
import com.example.topoclimb.data.CreateLogRequest
import com.example.topoclimb.data.CreateLogResponse
import com.example.topoclimb.data.Line
import com.example.topoclimb.data.LinesResponse
import com.example.topoclimb.data.Log
import com.example.topoclimb.data.LogsResponse
import com.example.topoclimb.data.LoginRequest
import com.example.topoclimb.data.Route
import com.example.topoclimb.data.RouteResponse
import com.example.topoclimb.data.RoutesResponse
import com.example.topoclimb.data.Sector
import com.example.topoclimb.data.SectorSchema
import com.example.topoclimb.data.SectorsResponse
import com.example.topoclimb.data.Site
import com.example.topoclimb.data.SiteResponse
import com.example.topoclimb.data.SitesResponse
import com.example.topoclimb.data.UserLogsResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface TopoClimbApiService {
    
    @GET("meta")
    suspend fun getMeta(): com.example.topoclimb.data.InstanceMeta
    
    @POST("login")
    suspend fun login(@Body request: LoginRequest): AuthResponse
    
    @POST("register")
    suspend fun register(@Body request: com.example.topoclimb.data.RegisterRequest): AuthResponse
    
    @GET("sites")
    suspend fun getSites(): SitesResponse
    
    @GET("sites/{id}")
    suspend fun getSite(@Path("id") id: Int): SiteResponse
    
    @GET("routes")
    suspend fun getRoutes(
        @Query("siteId") siteId: Int? = null,
        @Query("grade") grade: String? = null,
        @Query("type") type: String? = null
    ): RoutesResponse
    
    @GET("routes/{id}")
    suspend fun getRoute(@Path("id") id: Int): RouteResponse
    
    @GET("areas")
    suspend fun getAreas(): AreasResponse
    
    @GET("areas/{id}")
    suspend fun getArea(@Path("id") id: Int): AreaResponse
    
    @GET("areas/{areaId}/routes")
    suspend fun getRoutesByArea(@Path("areaId") areaId: Int): RoutesResponse
    
    @GET("sites/{siteId}/areas")
    suspend fun getAreasBySite(@Path("siteId") siteId: Int): AreasResponse
    
    @GET("sites/{siteId}/contests")
    suspend fun getContestsBySite(@Path("siteId") siteId: Int): ContestsResponse
    
    @GET("sectors/{id}/lines")
    suspend fun getLinesBySector(@Path("id") sectorId: Int): LinesResponse
    
    @GET("lines/{id}/routes")
    suspend fun getRoutesByLine(@Path("id") lineId: Int): RoutesResponse
    
    @GET("areas/{areaId}/sectors")
    suspend fun getSectorsByArea(@Path("areaId") areaId: Int): SectorsResponse
    
    @GET("areas/{areaId}/schemas")
    suspend fun getAreaSchemas(@Path("areaId") areaId: Int): List<SectorSchema>
    
    @GET("routes/{route}/logs")
    suspend fun getRouteLogs(@Path("route") routeId: Int): LogsResponse
    
    @POST("routes/{route}/logs/create")
    suspend fun createRouteLog(
        @Path("route") routeId: Int,
        @Body request: CreateLogRequest,
        @Header("Authorization") authToken: String
    ): CreateLogResponse
    
    @GET("user/logs")
    suspend fun getUserLogs(@Header("Authorization") authToken: String): UserLogsResponse
    
    @POST("user/update")
    suspend fun updateUser(
        @Body request: com.example.topoclimb.data.UserUpdateRequest,
        @Header("Authorization") authToken: String
    ): com.example.topoclimb.data.UserUpdateResponse
    
    @GET("user/stats")
    suspend fun getUserStats(@Header("Authorization") authToken: String): com.example.topoclimb.data.UserStats
    
    @GET("user/qrcode")
    suspend fun getUserQRCode(@Header("Authorization") authToken: String): com.example.topoclimb.data.QRCodeResponse
}

