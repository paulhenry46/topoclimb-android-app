package com.example.topoclimb.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.topoclimb.data.Route
import com.example.topoclimb.repository.TopoClimbRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RoutesUiState(
    val routes: List<Route> = emptyList(),
    val filteredRoutes: List<Route> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedGrade: String? = null,
    val selectedType: String? = null,
    val selectedSiteId: Int? = null
)

class RoutesViewModel(
    private val repository: TopoClimbRepository = TopoClimbRepository()
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(RoutesUiState())
    val uiState: StateFlow<RoutesUiState> = _uiState.asStateFlow()
    
    init {
        loadRoutes()
    }
    
    fun loadRoutes(siteId: Int? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null,
                selectedSiteId = siteId
            )
            repository.getRoutes(siteId)
                .onSuccess { routes ->
                    _uiState.value = _uiState.value.copy(
                        routes = routes,
                        filteredRoutes = routes,
                        isLoading = false
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        error = exception.message ?: "Unknown error",
                        isLoading = false
                    )
                }
        }
    }
    
    fun filterByGrade(grade: String?) {
        _uiState.value = _uiState.value.copy(selectedGrade = grade)
        applyFilters()
    }
    
    fun filterByType(type: String?) {
        _uiState.value = _uiState.value.copy(selectedType = type)
        applyFilters()
    }
    
    fun clearFilters() {
        _uiState.value = _uiState.value.copy(
            selectedGrade = null,
            selectedType = null,
            filteredRoutes = _uiState.value.routes
        )
    }
    
    private fun applyFilters() {
        val filtered = _uiState.value.routes.filter { route ->
            val matchesGrade = _uiState.value.selectedGrade?.let { 
                route.grade == it 
            } ?: true
            
            val matchesType = _uiState.value.selectedType?.let { 
                route.type == it 
            } ?: true
            
            matchesGrade && matchesType
        }
        
        _uiState.value = _uiState.value.copy(filteredRoutes = filtered)
    }
}
