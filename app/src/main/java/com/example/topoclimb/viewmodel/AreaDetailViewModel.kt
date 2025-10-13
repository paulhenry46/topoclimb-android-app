package com.example.topoclimb.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.topoclimb.data.Area
import com.example.topoclimb.data.Line
import com.example.topoclimb.data.Route
import com.example.topoclimb.data.Sector
import com.example.topoclimb.repository.TopoClimbRepository
import com.example.topoclimb.utils.SvgParser
import com.example.topoclimb.utils.SvgPathData
import com.example.topoclimb.utils.SvgDimensions
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
    val area: Area? = null,
    val routes: List<Route> = emptyList(),
    val error: String? = null,
    val svgMapContent: String? = null,
    val svgPaths: List<SvgPathData> = emptyList(),
    val svgDimensions: SvgDimensions? = null,
    val selectedSectorId: Int? = null,
    val sectors: List<Sector> = emptyList()
)

class AreaDetailViewModel : ViewModel() {
    private val repository = TopoClimbRepository()
    private val httpClient = OkHttpClient()
    
    private val _uiState = MutableStateFlow(AreaDetailUiState())
    val uiState: StateFlow<AreaDetailUiState> = _uiState.asStateFlow()
    
    fun loadAreaDetails(areaId: Int) {
        viewModelScope.launch {
            _uiState.value = AreaDetailUiState(isLoading = true)
            
            // Load area details
            val areaResult = repository.getArea(areaId)
            
            if (areaResult.isFailure) {
                _uiState.value = AreaDetailUiState(
                    isLoading = false,
                    error = areaResult.exceptionOrNull()?.message ?: "Failed to load area details"
                )
                return@launch
            }
            
            val area = areaResult.getOrNull()
            
            // Load routes for the area
            val routesResult = repository.getRoutesByArea(areaId)
            val routes = routesResult.getOrNull() ?: emptyList()
            
            // Load sectors for the area
            val sectorsResult = repository.getSectorsByArea(areaId)
            val sectors = sectorsResult.getOrNull() ?: emptyList()
            
            // Fetch SVG map content from URL if available
            var svgContent: String? = null
            var svgPaths: List<SvgPathData> = emptyList()
            var svgDimensions: SvgDimensions? = null
            
            area?.svgMap?.let { mapUrl ->
                try {
                    svgContent = withContext(Dispatchers.IO) {
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
                    
                    // Parse SVG content to extract paths
                    svgContent?.let { content ->
                        val (dims, paths) = SvgParser.parseSvg(content)
                        svgDimensions = dims
                        svgPaths = paths
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            
            _uiState.value = AreaDetailUiState(
                isLoading = false,
                area = area,
                routes = routes,
                error = null,
                svgMapContent = svgContent,
                svgPaths = svgPaths,
                svgDimensions = svgDimensions,
                sectors = sectors
            )
        }
    }
    
    fun onSectorTapped(sectorId: Int) {
        viewModelScope.launch {
            val currentState = _uiState.value
            
            // Toggle selection - if same sector is tapped, deselect it
            val newSelectedSectorId = if (currentState.selectedSectorId == sectorId) null else sectorId
            
            if (newSelectedSectorId != null) {
                // Fetch lines for this sector
                val linesResult = repository.getLinesBySector(sectorId)
                val lines = linesResult.getOrNull() ?: emptyList()
                
                // Fetch routes for all lines
                val routesForSector = mutableListOf<Route>()
                lines.forEach { line ->
                    val routesResult = repository.getRoutesByLine(line.id)
                    routesResult.getOrNull()?.let { routesForSector.addAll(it) }
                }
                
                _uiState.value = currentState.copy(
                    selectedSectorId = newSelectedSectorId,
                    routes = routesForSector
                )
            } else {
                // Deselect - show all routes for the area
                val routesResult = repository.getRoutesByArea(currentState.area?.id ?: return@launch)
                val routes = routesResult.getOrNull() ?: emptyList()
                
                _uiState.value = currentState.copy(
                    selectedSectorId = null,
                    routes = routes
                )
            }
        }
    }
}
