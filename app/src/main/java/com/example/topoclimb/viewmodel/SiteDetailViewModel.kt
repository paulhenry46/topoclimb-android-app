package com.example.topoclimb.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.topoclimb.data.Area
import com.example.topoclimb.data.Contest
import com.example.topoclimb.data.Site
import com.example.topoclimb.repository.TopoClimbRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SiteDetailUiState(
    val site: Site? = null,
    val areas: List<Area> = emptyList(),
    val contests: List<Contest> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class SiteDetailViewModel(
    private val repository: TopoClimbRepository = TopoClimbRepository()
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SiteDetailUiState())
    val uiState: StateFlow<SiteDetailUiState> = _uiState.asStateFlow()
    
    fun loadSiteDetails(siteId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            // Load site details
            repository.getSite(siteId)
                .onSuccess { site ->
                    _uiState.value = _uiState.value.copy(site = site)
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        error = exception.message ?: "Failed to load site details",
                        isLoading = false
                    )
                    return@launch
                }
            
            // Load areas
            repository.getAreasBySite(siteId)
                .onSuccess { response ->
                    _uiState.value = _uiState.value.copy(areas = response.data)
                }
                .onFailure { exception ->
                    // Don't fail the whole screen if areas fail to load
                    _uiState.value = _uiState.value.copy(areas = emptyList())
                }
            
            // Load contests
            repository.getContestsBySite(siteId)
                .onSuccess { response ->
                    _uiState.value = _uiState.value.copy(contests = response.data)
                }
                .onFailure { exception ->
                    // Don't fail the whole screen if contests fail to load
                    _uiState.value = _uiState.value.copy(contests = emptyList())
                }
            
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }
}
