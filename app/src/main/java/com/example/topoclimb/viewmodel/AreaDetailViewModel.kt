package com.example.topoclimb.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.topoclimb.data.Area
import com.example.topoclimb.data.AreaType
import com.example.topoclimb.data.GradingSystem
import com.example.topoclimb.data.Route
import com.example.topoclimb.data.RouteWithMetadata
import com.example.topoclimb.data.Sector
import com.example.topoclimb.data.SectorSchema
import com.example.topoclimb.repository.OfflineRepository
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

enum class ViewMode {
    MAP,      // Traditional interactive map view
    SCHEMA    // Schema view with sector overlays
}

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
    // Schema view state
    val schemas: List<SectorSchema> = emptyList(),
    val allSchemas: List<SectorSchema> = emptyList(), // All schemas including those without paths/bg
    val schemaError: String? = null, // Error message when loading schemas
    val currentSchemaIndex: Int = 0,
    val viewMode: ViewMode = ViewMode.MAP,
    // Filter state
    val searchQuery: String = "",
    val minGrade: String? = null,
    val maxGrade: String? = null,
    val showNewRoutesOnly: Boolean = false,
    val climbedFilter: ClimbedFilter = ClimbedFilter.ALL,
    val showFavoritesOnly: Boolean = false,
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

class AreaDetailViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = TopoClimbRepository()
    private val offlineRepository = OfflineRepository(application)
    private val httpClient = OkHttpClient()
    
    private val _uiState = MutableStateFlow(AreaDetailUiState())
    val uiState: StateFlow<AreaDetailUiState> = _uiState.asStateFlow()
    
    // Store all routes before filtering
    private var allRoutesCache: List<Route> = emptyList()
    private var allRoutesWithMetadataCache: List<RouteWithMetadata> = emptyList()
    
    // Get logged routes from shared state
    private val loggedRouteIds: StateFlow<Set<Int>> = RouteDetailViewModel.sharedLoggedRouteIds
    
    // Store favorite route IDs for filtering
    private var favoriteRouteIds: Set<Int> = emptySet()
    
    fun setFavoriteRouteIds(ids: Set<Int>) {
        favoriteRouteIds = ids
        if (_uiState.value.showFavoritesOnly) {
            applyFilters()
        }
    }
    
    fun loadAreaDetails(backendId: String, siteId: Int, areaId: Int) {
        viewModelScope.launch {
            _uiState.value = AreaDetailUiState(isLoading = true, backendId = backendId, siteId = siteId, areaId = areaId)
            
            // Try to load from network first
            val result = fetchAreaData(siteId, areaId)
            
            if (result.isFailure) {
                // Try loading from offline cache
                val offlineResult = loadOfflineAreaData(siteId, areaId)
                if (offlineResult != null) {
                    val (area, routes) = offlineResult
                    _uiState.value = AreaDetailUiState(
                        isLoading = false,
                        isRefreshing = false,
                        area = area,
                        routes = routes,
                        routesWithMetadata = emptyList(),
                        sectors = emptyList(),
                        error = null,
                        backendId = backendId,
                        siteId = siteId,
                        areaId = areaId,
                        gradingSystem = null,
                        siteName = null
                    )
                    // Cache routes for filtering
                    allRoutesCache = routes
                    allRoutesWithMetadataCache = emptyList()
                    return@launch
                }
                
                // No offline data available
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
            
            val (area, gradingSystem, siteName, sectors, routes, routesWithMetadata, svgContent, schemas, allSchemas, schemaError) = result.getOrNull()!!
            
            // Cache all routes for filtering
            allRoutesCache = routes
            allRoutesWithMetadataCache = routesWithMetadata
            
            // Determine initial view mode
            val initialViewMode = determineInitialViewMode(area, schemas, svgContent)
            
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
                gradingSystem = gradingSystem,
                schemas = schemas,
                allSchemas = allSchemas,
                schemaError = schemaError,
                viewMode = initialViewMode
            )
            
            // If we're starting in schema mode, filter routes by the first schema's sector
            if (initialViewMode == ViewMode.SCHEMA) {
                schemas.firstOrNull()?.id?.let { firstSchemaId ->
                    filterRoutesBySector(firstSchemaId)
                }
            }
        }
    }
    
    private suspend fun loadOfflineAreaData(siteId: Int, areaId: Int): Pair<Area, List<Route>>? {
        return try {
            val cachedArea = offlineRepository.getCachedArea(areaId)
            val cachedRoutes = offlineRepository.getCachedRoutes(siteId)
            
            if (cachedArea != null) {
                println("DEBUG: Loaded offline area $areaId with ${cachedRoutes.size} routes from site $siteId")
                Pair(cachedArea, cachedRoutes)
            } else {
                println("DEBUG: No offline data for area $areaId")
                null
            }
        } catch (e: Exception) {
            println("Failed to load offline area data: ${e.message}")
            e.printStackTrace()
            null
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
            
            val (area, gradingSystem, siteName, sectors, routes, routesWithMetadata, svgContent, schemas, allSchemas, schemaError) = result.getOrNull()!!
            
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
                gradingSystem = gradingSystem,
                schemas = schemas,
                allSchemas = allSchemas,
                schemaError = schemaError
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
            
            // Load routes with sector and line metadata using the chain:
            // getSectorsByArea -> getLinesBySector -> getRoutesByLine
            val allRoutesWithMetadata = mutableListOf<RouteWithMetadata>()
            for (sector in sectors) {
                val linesResult = repository.getLinesBySector(sector.id)
                if (linesResult.isSuccess) {
                    val lines = linesResult.getOrNull() ?: emptyList()
                    
                    // Fetch routes for each line and enrich with metadata
                    for (line in lines) {
                        val routesResult = repository.getRoutesByLine(line.id)
                        if (routesResult.isSuccess) {
                            val routes = routesResult.getOrNull() ?: emptyList()
                            routes.forEach { route ->
                                val updatedRoute = if (route.siteId == 0 || route.siteName.isNullOrBlank()) {
                                    route.copy(
                                        siteId = if (route.siteId == 0) siteId else route.siteId,
                                        siteName = route.siteName?.takeIf { it.isNotBlank() } ?: siteName
                                    )
                                } else {
                                    route
                                }
                                allRoutesWithMetadata.add(
                                    RouteWithMetadata(
                                        route = updatedRoute,
                                        lineLocalId = line.localId,
                                        sectorLocalId = sector.localId,
                                        lineCount = lines.size
                                    )
                                )
                            }
                        }
                    }
                }
            }
            
            val routes = allRoutesWithMetadata.map { it.route }
            val routesWithMetadata = allRoutesWithMetadata
            
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
            
            // Load schemas for trad areas only
            val allSchemas: List<SectorSchema>
            val schemas: List<SectorSchema>
            var schemaError: String? = null
            
            if (area?.type == AreaType.TRAD) {
                val schemasResult = repository.getAreaSchemas(areaId)
                if (schemasResult.isSuccess) {
                    allSchemas = schemasResult.getOrNull() ?: emptyList()
                    schemas = allSchemas.filter { it.paths != null && it.bg != null }
                    
                    // Add debug info about what we got
                    if (allSchemas.isEmpty()) {
                        schemaError = "API returned empty schemas list"
                    } else if (schemas.isEmpty()) {
                        schemaError = "Received ${allSchemas.size} schema(s) but all have null paths or bg. Schemas: ${allSchemas.map { "${it.name}(paths=${it.paths != null}, bg=${it.bg != null})" }}"
                    }
                } else {
                    allSchemas = emptyList()
                    schemas = emptyList()
                    schemaError = "Failed to load schemas: ${schemasResult.exceptionOrNull()?.message ?: "Unknown error"}"
                }
            } else {
                allSchemas = emptyList()
                schemas = emptyList()
            }
            
            Result.success(AreaData(area, gradingSystem, siteName, sectors, routes, routesWithMetadata, svgContent, schemas, allSchemas, schemaError))
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
        val svgContent: String?,
        val schemas: List<SectorSchema>,
        val allSchemas: List<SectorSchema>,
        val schemaError: String?
    )
    
    /**
     * Determines the initial view mode based on area type and available data
     * Returns SCHEMA mode for trad areas with schemas but no map, otherwise MAP mode
     */
    private fun determineInitialViewMode(
        area: Area?,
        schemas: List<SectorSchema>,
        svgContent: String?
    ): ViewMode {
        return if (area?.type == AreaType.TRAD && 
                   schemas.isNotEmpty() && 
                   svgContent == null) {
            ViewMode.SCHEMA
        } else {
            ViewMode.MAP
        }
    }
    
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
        // This is now a local filtering operation - no network requests
        val currentState = _uiState.value
        
        // If in schema mode, also update the current schema index to match this sector
        val newSchemaIndex = if (currentState.viewMode == ViewMode.SCHEMA && sectorId != null) {
            currentState.schemas.indexOfFirst { it.id == sectorId }.takeIf { it >= 0 } ?: currentState.currentSchemaIndex
        } else {
            currentState.currentSchemaIndex
        }
        
        _uiState.value = currentState.copy(
            selectedSectorId = sectorId,
            currentSchemaIndex = newSchemaIndex
        )
        
        // Apply filters to show routes for the selected sector (or all routes if null)
        applyFilters()
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
    
    fun toggleFavoritesFilter(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(showFavoritesOnly = enabled)
        applyFilters()
    }
    
    fun clearFilters() {
        _uiState.value = _uiState.value.copy(
            searchQuery = "",
            minGrade = null,
            maxGrade = null,
            showNewRoutesOnly = false,
            climbedFilter = ClimbedFilter.ALL,
            showFavoritesOnly = false,
            groupingOption = GroupingOption.NONE
        )
        applyFilters()
    }
    
    fun toggleViewMode() {
        val currentState = _uiState.value
        val newMode = if (currentState.viewMode == ViewMode.MAP) ViewMode.SCHEMA else ViewMode.MAP
        _uiState.value = currentState.copy(viewMode = newMode)
        
        // When switching to schema mode, select the first schema's sector if available
        if (newMode == ViewMode.SCHEMA && currentState.schemas.isNotEmpty()) {
            val firstSchema = currentState.schemas[0]
            filterRoutesBySector(firstSchema.id)
        } else if (newMode == ViewMode.MAP) {
            // When switching back to map mode, clear sector filter
            filterRoutesBySector(null)
        }
    }
    
    fun navigateToNextSchema() {
        val currentState = _uiState.value
        if (currentState.schemas.isEmpty()) return
        
        val nextIndex = (currentState.currentSchemaIndex + 1) % currentState.schemas.size
        _uiState.value = currentState.copy(currentSchemaIndex = nextIndex)
        
        // Update sector filter to match the new schema
        val schema = currentState.schemas[nextIndex]
        filterRoutesBySector(schema.id)
    }
    
    fun navigateToPreviousSchema() {
        val currentState = _uiState.value
        if (currentState.schemas.isEmpty()) return
        
        val prevIndex = if (currentState.currentSchemaIndex > 0) {
            currentState.currentSchemaIndex - 1
        } else {
            currentState.schemas.size - 1
        }
        _uiState.value = currentState.copy(currentSchemaIndex = prevIndex)
        
        // Update sector filter to match the new schema
        val schema = currentState.schemas[prevIndex]
        filterRoutesBySector(schema.id)
    }
    
    fun selectSchemaByIndex(index: Int) {
        val currentState = _uiState.value
        if (index < 0 || index >= currentState.schemas.size) return
        
        _uiState.value = currentState.copy(currentSchemaIndex = index)
        
        // Update sector filter to match the selected schema
        val schema = currentState.schemas[index]
        filterRoutesBySector(schema.id)
    }
    
    private fun applyFilters() {
        val currentState = _uiState.value
        var filteredRoutes = allRoutesCache
        var filteredRoutesWithMetadata = allRoutesWithMetadataCache
        
        // Apply sector filter if a sector is selected
        if (currentState.selectedSectorId != null) {
            val selectedSector = currentState.sectors.find { it.id == currentState.selectedSectorId }
            filteredRoutesWithMetadata = filteredRoutesWithMetadata.filter { routeWithMetadata ->
                routeWithMetadata.sectorLocalId == selectedSector?.localId
            }
            filteredRoutes = filteredRoutesWithMetadata.map { it.route }
        }
        
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
        
        // Apply favorites filter
        if (currentState.showFavoritesOnly) {
            filteredRoutes = filteredRoutes.filter { route ->
                favoriteRouteIds.contains(route.id)
            }
            filteredRoutesWithMetadata = filteredRoutesWithMetadata.filter { routeWithMetadata ->
                favoriteRouteIds.contains(routeWithMetadata.id)
            }
        }
        
        _uiState.value = currentState.copy(
            routes = filteredRoutes,
            routesWithMetadata = filteredRoutesWithMetadata
        )
    }
}
