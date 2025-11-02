package com.example.topoclimb.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.topoclimb.data.Area
import com.example.topoclimb.data.GradingSystem
import com.example.topoclimb.data.Route
import com.example.topoclimb.data.RouteWithMetadata
import com.example.topoclimb.data.Sector
import com.example.topoclimb.repository.TopoClimbRepository
import com.example.topoclimb.utils.GradeUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

data class AreaDetailUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val area: Area? = null,
    val routes: List<Route> = emptyList(),
    val routesWithMetadata: List<com.example.topoclimb.data.RouteWithMetadata> = emptyList(),
    val sectors: List<Sector> = emptyList(),
    val selectedSectorId: Int? = null,
    val error: String? = null,
    val svgMapContent: String? = null,
    val backendId: String? = null,
    val siteId: Int? = null,
    val siteName: String? = null,
    val areaId: Int? = null,
    val gradingSystem: GradingSystem? = null,
    // Filter state
    val searchQuery: String = "",
    val minGrade: String? = null,
    val maxGrade: String? = null,
    val showNewRoutesOnly: Boolean = false,
    val climbedFilter: ClimbedFilter = ClimbedFilter.ALL,
    // Grouping state
    val groupingOption: GroupingOption = GroupingOption.NONE
)

enum class ClimbedFilter {
    ALL,        // Show all routes
    CLIMBED,    // Show only climbed routes
    NOT_CLIMBED // Show only not climbed routes
}

enum class GroupingOption {
    NONE,       // No grouping
    BY_GRADE,   // Group by grade
    BY_SECTOR   // Group by sector
}

class AreaDetailViewModel : ViewModel() {
    private val repository = TopoClimbRepository()
    private val httpClient = OkHttpClient()
    
    private val _uiState = MutableStateFlow(AreaDetailUiState())
    val uiState: StateFlow<AreaDetailUiState> = _uiState.asStateFlow()
    
    // Store all routes before filtering
    private var allRoutesCache: List<Route> = emptyList()
    private var allRoutesWithMetadataCache: List<RouteWithMetadata> = emptyList()
    
    // Get logged routes from shared state
    private val loggedRouteIds: StateFlow<Set<Int>> = RouteDetailViewModel.sharedLoggedRouteIds
    
    fun loadAreaDetails(backendId: String, siteId: Int, areaId: Int) {
        viewModelScope.launch {
            _uiState.value = AreaDetailUiState(isLoading = true, backendId = backendId, siteId = siteId, areaId = areaId)
            
            // Now passing siteId to avoid fetching it from area relationship
            val result = fetchAreaData(siteId, areaId)
            
            if (result.isFailure) {
                _uiState.value = AreaDetailUiState(
                    isLoading = false,
                    isRefreshing = false,
                    error = result.exceptionOrNull()?.message ?: "Failed to load area details",
                    backendId = backendId,
                    siteId = siteId,
                    areaId = areaId
                )
                return@launch
            }
            
            val (area, gradingSystem, siteName, sectors, routes, routesWithMetadata, svgContent) = result.getOrNull()!!
            
            // Cache all routes for filtering
            allRoutesCache = routes
            allRoutesWithMetadataCache = routesWithMetadata
            
            _uiState.value = AreaDetailUiState(
                isLoading = false,
                isRefreshing = false,
                area = area,
                routes = routes,
                routesWithMetadata = routesWithMetadata,
                sectors = sectors,
                error = null,
                svgMapContent = svgContent,
                backendId = backendId,
                siteId = siteId,
                siteName = siteName,
                areaId = areaId,
                gradingSystem = gradingSystem
            )
        }
    }
    
    fun refreshAreaDetails() {
        val backendId = _uiState.value.backendId ?: return
        val siteId = _uiState.value.siteId ?: return
        val areaId = _uiState.value.areaId ?: return
        val currentState = _uiState.value
        
        viewModelScope.launch {
            _uiState.value = currentState.copy(isRefreshing = true, error = null)
            
            val result = fetchAreaData(siteId, areaId)
            
            if (result.isFailure) {
                _uiState.value = currentState.copy(
                    isRefreshing = false,
                    error = result.exceptionOrNull()?.message ?: "Failed to refresh area details"
                )
                return@launch
            }
            
            val (area, gradingSystem, siteName, sectors, routes, routesWithMetadata, svgContent) = result.getOrNull()!!
            
            // Cache all routes for filtering
            allRoutesCache = routes
            allRoutesWithMetadataCache = routesWithMetadata
            
            _uiState.value = currentState.copy(
                isRefreshing = false,
                area = area,
                routes = routes,
                routesWithMetadata = routesWithMetadata,
                sectors = sectors,
                error = null,
                svgMapContent = svgContent,
                siteName = siteName,
                gradingSystem = gradingSystem
            )
        }
    }
    
    /**
     * Helper method to fetch area data including site grading system
     * Returns a Result containing all area-related data
     * 
     * With the nested navigation architecture, siteId is passed directly from navigation,
     * which allows us to fetch site data without traversing the area→site relationship.
     * This reduces the number of API calls and improves performance.
     */
    private suspend fun fetchAreaData(siteId: Int, areaId: Int): Result<AreaData> {
        return try {
            // Load area details
            val areaResult = repository.getArea(areaId)
            if (areaResult.isFailure) {
                return Result.failure(areaResult.exceptionOrNull() ?: Exception("Failed to load area"))
            }
            
            val area = areaResult.getOrNull()
            
            // Load site using the directly provided siteId (optimization from nested navigation)
            var gradingSystem: GradingSystem? = null
            var siteName: String? = null
            val siteResult = repository.getSite(siteId)
            gradingSystem = siteResult.getOrNull()?.gradingSystem
            siteName = siteResult.getOrNull()?.name
            
            // Load sectors for the area
            val sectorsResult = repository.getSectorsByArea(areaId)
            val sectors = sectorsResult.getOrNull() ?: emptyList()
            
            // Load routes for the area
            val routesResult = repository.getRoutesByArea(areaId)
            val routes = routesResult.getOrNull() ?: emptyList()
            
            // Fetch SVG map content from URL if available
            val svgContent = area?.svgMap?.let { mapUrl ->
                try {
                    withContext(Dispatchers.IO) {
                        val request = Request.Builder()
                            .url(mapUrl)
                            .build()
                        
                        httpClient.newCall(request).execute().use { response ->
                            if (response.isSuccessful) {
                                response.body?.string()
                            } else {
                                null
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            }
            
            // Convert routes to RouteWithMetadata, ensuring siteId and siteName are set
            val routesWithMetadata = routes.map { route ->
                // If the route doesn't have siteId or siteName from API, set them from context
                val updatedRoute = if (route.siteId == 0 || route.siteName.isNullOrBlank()) {
                    route.copy(
                        siteId = if (route.siteId == 0) siteId else route.siteId,
                        siteName = route.siteName?.takeIf { it.isNotBlank() } ?: siteName
                    )
                } else {
                    route
                }
                RouteWithMetadata(updatedRoute)
            }
            
            Result.success(AreaData(area, gradingSystem, siteName, sectors, routes, routesWithMetadata, svgContent))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Data class to hold all area-related data
     */
    private data class AreaData(
        val area: Area?,
        val gradingSystem: GradingSystem?,
        val siteName: String?,
        val sectors: List<Sector>,
        val routes: List<Route>,
        val routesWithMetadata: List<RouteWithMetadata>,
        val svgContent: String?
    )
    
    /**
     * Ensures a route has valid siteId and siteName by using context values if missing
     */
    private fun ensureRouteSiteInfo(route: Route, contextSiteId: Int, contextSiteName: String?): Route {
        return if (route.siteId == 0 || route.siteName.isNullOrBlank()) {
            route.copy(
                siteId = if (route.siteId == 0) contextSiteId else route.siteId,
                siteName = route.siteName?.takeIf { it.isNotBlank() } ?: contextSiteName
            )
        } else {
            route
        }
    }
    
    fun filterRoutesBySector(sectorId: Int?) {
        viewModelScope.launch {
            if (sectorId == null) {
                // Deselected - reload all routes for the area
                val currentState = _uiState.value
                currentState.area?.let { area ->
                    val routesResult = repository.getRoutesByArea(area.id)
                    val routes = routesResult.getOrNull() ?: emptyList()
                    
                    // Get site info from current state
                    val contextSiteId = currentState.siteId ?: 0
                    val contextSiteName = currentState.siteName
                    
                    val routesWithMetadata = routes.map { route ->
                        val updatedRoute = ensureRouteSiteInfo(route, contextSiteId, contextSiteName)
                        RouteWithMetadata(updatedRoute)
                    }
                    // Update cache
                    allRoutesCache = routes
                    allRoutesWithMetadataCache = routesWithMetadata
                    
                    _uiState.value = currentState.copy(
                        selectedSectorId = null
                    )
                    // Apply filters to the new data
                    applyFilters()
                }
            } else {
                // Selected - fetch lines for this sector, then routes for each line
                val currentState = _uiState.value
                _uiState.value = currentState.copy(selectedSectorId = sectorId)
                
                // Get sector info for localId
                val sector = currentState.sectors.find { it.id == sectorId }
                
                // Get site info from current state
                val contextSiteId = currentState.siteId ?: 0
                val contextSiteName = currentState.siteName
                
                val linesResult = repository.getLinesBySector(sectorId)
                if (linesResult.isSuccess) {
                    val lines = linesResult.getOrNull() ?: emptyList()
                    val allRoutesWithMetadata = mutableListOf<RouteWithMetadata>()
                    
                    // Fetch routes for each line and enrich with metadata
                    for (line in lines) {
                        val routesResult = repository.getRoutesByLine(line.id)
                        if (routesResult.isSuccess) {
                            val routes = routesResult.getOrNull() ?: emptyList()
                            routes.forEach { route ->
                                val updatedRoute = ensureRouteSiteInfo(route, contextSiteId, contextSiteName)
                                allRoutesWithMetadata.add(
                                    RouteWithMetadata(
                                        route = updatedRoute,
                                        lineLocalId = line.localId,
                                        sectorLocalId = sector?.localId,
                                        lineCount = lines.size
                                    )
                                )
                            }
                        }
                    }
                    
                    val allRoutes = allRoutesWithMetadata.map { it.route }
                    
                    // Update cache
                    allRoutesCache = allRoutes
                    allRoutesWithMetadataCache = allRoutesWithMetadata
                    
                    _uiState.value = currentState.copy(
                        selectedSectorId = sectorId
                    )
                    // Apply filters to the new data
                    applyFilters()
                } else {
                    // Error fetching lines, keep current state but mark sector as selected
                    allRoutesCache = emptyList()
                    allRoutesWithMetadataCache = emptyList()
                    _uiState.value = currentState.copy(
                        selectedSectorId = sectorId,
                        routes = emptyList(),
                        routesWithMetadata = emptyList()
                    )
                }
            }
        }
    }
    
    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        applyFilters()
    }
    
    fun updateMinGrade(grade: String?) {
        _uiState.value = _uiState.value.copy(minGrade = grade)
        applyFilters()
    }
    
    fun updateMaxGrade(grade: String?) {
        _uiState.value = _uiState.value.copy(maxGrade = grade)
        applyFilters()
    }
    
    fun toggleNewRoutesFilter(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(showNewRoutesOnly = enabled)
        applyFilters()
    }
    
    fun setClimbedFilter(filter: ClimbedFilter) {
        _uiState.value = _uiState.value.copy(climbedFilter = filter)
        applyFilters()
    }
    
    fun setGroupingOption(option: GroupingOption) {
        _uiState.value = _uiState.value.copy(groupingOption = option)
        applyFilters()
    }
    
    fun clearFilters() {
        _uiState.value = _uiState.value.copy(
            searchQuery = "",
            minGrade = null,
            maxGrade = null,
            showNewRoutesOnly = false,
            climbedFilter = ClimbedFilter.ALL,
            groupingOption = GroupingOption.NONE
        )
        applyFilters()
    }
    
    private fun applyFilters() {
        val currentState = _uiState.value
        var filteredRoutes = allRoutesCache
        var filteredRoutesWithMetadata = allRoutesWithMetadataCache
        
        // Apply search filter
        if (currentState.searchQuery.isNotEmpty()) {
            filteredRoutes = filteredRoutes.filter { route ->
                route.name.contains(currentState.searchQuery, ignoreCase = true)
            }
            filteredRoutesWithMetadata = filteredRoutesWithMetadata.filter { routeWithMetadata ->
                routeWithMetadata.name.contains(currentState.searchQuery, ignoreCase = true)
            }
        }
        
        // Apply grade filters
        if (currentState.minGrade != null || currentState.maxGrade != null) {
            val minGradeInt = currentState.minGrade?.let { GradeUtils.gradeToPoints(it, currentState.gradingSystem) }
            val maxGradeInt = currentState.maxGrade?.let { GradeUtils.gradeToPoints(it, currentState.gradingSystem) }
            
            filteredRoutes = filteredRoutes.filter { route ->
                route.grade?.let { grade ->
                    when {
                        minGradeInt != null && maxGradeInt != null -> grade in minGradeInt..maxGradeInt
                        minGradeInt != null -> grade >= minGradeInt
                        maxGradeInt != null -> grade <= maxGradeInt
                        else -> true
                    }
                } ?: false
            }
            filteredRoutesWithMetadata = filteredRoutesWithMetadata.filter { routeWithMetadata ->
                routeWithMetadata.grade?.let { grade ->
                    when {
                        minGradeInt != null && maxGradeInt != null -> grade in minGradeInt..maxGradeInt
                        minGradeInt != null -> grade >= minGradeInt
                        maxGradeInt != null -> grade <= maxGradeInt
                        else -> true
                    }
                } ?: false
            }
        }
        
        // Apply new routes filter (routes created within the last week)
        if (currentState.showNewRoutesOnly) {
            val oneWeekAgoMillis = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L)
            filteredRoutes = filteredRoutes.filter { route ->
                route.createdAt?.let { createdAtStr ->
                    try {
                        // Parse ISO 8601 format: 2025-10-08T12:18:41.000000Z
                        val cleanedStr = createdAtStr.replace("Z", "+00:00").substringBefore(".")
                        val format = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US)
                        format.timeZone = java.util.TimeZone.getTimeZone("UTC")
                        val createdAtMillis = format.parse(cleanedStr)?.time ?: 0L
                        createdAtMillis > oneWeekAgoMillis
                    } catch (e: Exception) {
                        false
                    }
                } ?: false
            }
            filteredRoutesWithMetadata = filteredRoutesWithMetadata.filter { routeWithMetadata ->
                routeWithMetadata.route.createdAt?.let { createdAtStr ->
                    try {
                        // Parse ISO 8601 format: 2025-10-08T12:18:41.000000Z
                        val cleanedStr = createdAtStr.replace("Z", "+00:00").substringBefore(".")
                        val format = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US)
                        format.timeZone = java.util.TimeZone.getTimeZone("UTC")
                        val createdAtMillis = format.parse(cleanedStr)?.time ?: 0L
                        createdAtMillis > oneWeekAgoMillis
                    } catch (e: Exception) {
                        false
                    }
                } ?: false
            }
        }
        
        // Apply climbed filter
        val currentLoggedRoutes = loggedRouteIds.value
        when (currentState.climbedFilter) {
            ClimbedFilter.CLIMBED -> {
                filteredRoutes = filteredRoutes.filter { route ->
                    currentLoggedRoutes.contains(route.id)
                }
                filteredRoutesWithMetadata = filteredRoutesWithMetadata.filter { routeWithMetadata ->
                    currentLoggedRoutes.contains(routeWithMetadata.id)
                }
            }
            ClimbedFilter.NOT_CLIMBED -> {
                filteredRoutes = filteredRoutes.filter { route ->
                    !currentLoggedRoutes.contains(route.id)
                }
                filteredRoutesWithMetadata = filteredRoutesWithMetadata.filter { routeWithMetadata ->
                    !currentLoggedRoutes.contains(routeWithMetadata.id)
                }
            }
            ClimbedFilter.ALL -> {
                // No filtering needed
            }
        }
        
        _uiState.value = currentState.copy(
            routes = filteredRoutes,
            routesWithMetadata = filteredRoutesWithMetadata
        )
    }
}
