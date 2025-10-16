package com.example.topoclimb.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.topoclimb.data.Area
import com.example.topoclimb.data.Route
import com.example.topoclimb.data.Sector
import com.example.topoclimb.repository.TopoClimbRepository
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
    val sectors: List<Sector> = emptyList(),
    val selectedSectorId: Int? = null,
    val error: String? = null,
    val svgMapContent: String? = null
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
            
            // Load sectors for the area
            val sectorsResult = repository.getSectorsByArea(areaId)
            val sectors = sectorsResult.getOrNull() ?: emptyList()
            
            // Load routes for the area (all routes initially)
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
            
            _uiState.value = AreaDetailUiState(
                isLoading = false,
                area = area,
                routes = routes,
                sectors = sectors,
                error = null,
                svgMapContent = svgContent
            )
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
                    _uiState.value = currentState.copy(
                        selectedSectorId = null,
                        routes = routes
                    )
                }
            } else {
                // Selected - fetch lines for this sector, then routes for each line
                val currentState = _uiState.value
                _uiState.value = currentState.copy(selectedSectorId = sectorId)
                
                val linesResult = repository.getLinesBySector(sectorId)
                if (linesResult.isSuccess) {
                    val lines = linesResult.getOrNull() ?: emptyList()
                    val allRoutes = mutableListOf<Route>()
                    
                    // Fetch routes for each line
                    for (line in lines) {
                        val routesResult = repository.getRoutesByLine(line.id)
                        if (routesResult.isSuccess) {
                            allRoutes.addAll(routesResult.getOrNull() ?: emptyList())
                        }
                    }
                    
                    _uiState.value = currentState.copy(
                        selectedSectorId = sectorId,
                        routes = allRoutes
                    )
                } else {
                    // Error fetching lines, keep current state but mark sector as selected
                    _uiState.value = currentState.copy(
                        selectedSectorId = sectorId,
                        routes = emptyList()
                    )
                }
            }
        }
    }
}
