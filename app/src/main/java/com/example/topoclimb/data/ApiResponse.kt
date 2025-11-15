package com.example.topoclimb.data

import com.google.gson.annotations.SerializedName

/**
 * Generic API response wrapper for single items
 */
data class ApiResponse<T>(
    @SerializedName("data")
    val data: T
)

/**
 * Generic API response wrapper for lists
 */
data class ApiListResponse<T>(
    @SerializedName("data")
    val data: List<T>
)

// Type aliases for backward compatibility
typealias RouteResponse = ApiResponse<Route>
typealias RoutesResponse = ApiListResponse<Route>
typealias SiteResponse = ApiResponse<Site>
typealias SitesResponse = ApiListResponse<Site>
typealias AreaResponse = ApiResponse<Area>
typealias AreasResponse = ApiListResponse<Area>
typealias ContestsResponse = ApiListResponse<Contest>
typealias LineResponse = ApiResponse<Line>
typealias LinesResponse = ApiListResponse<Line>
typealias SectorResponse = ApiResponse<Sector>
typealias SectorsResponse = ApiListResponse<Sector>
typealias LogsResponse = ApiListResponse<Log>
