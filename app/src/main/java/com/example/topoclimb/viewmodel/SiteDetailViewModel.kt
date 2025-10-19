package com.example.topoclimb.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.topoclimb.data.Area
import com.example.topoclimb.data.Contest
import com.example.topoclimb.data.Federated
import com.example.topoclimb.data.Site
import com.example.topoclimb.repository.FederatedTopoClimbRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SiteDetailUiState(
    val site: Federated<Site>? = null,
    val areas: List<Federated<Area>> = emptyList(),
    val contests: List<Federated<Contest>> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class SiteDetailViewModel(
    application: Application
) : AndroidViewModel(application) {
    
    private val repository = FederatedTopoClimbRepository(application)
    
    private val _uiState = MutableStateFlow(SiteDetailUiState())
    val uiState: StateFlow<SiteDetailUiState> = _uiState.asStateFlow()
    
    fun loadSiteDetails(backendId: String, siteId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            // Load site details
            repository.getSite(backendId, siteId)
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
            repository.getAreasBySite(backendId, siteId)
                .onSuccess { areas ->
                    _uiState.value = _uiState.value.copy(areas = areas)
                }
                .onFailure { exception ->
                    // Don't fail the whole screen if areas fail to load
                    _uiState.value = _uiState.value.copy(areas = emptyList())
                }
            
            // Load contests
            repository.getContestsBySite(backendId, siteId)
                .onSuccess { contests ->
                    _uiState.value = _uiState.value.copy(contests = contests)
                }
                .onFailure { exception ->
                    // Don't fail the whole screen if contests fail to load
                    _uiState.value = _uiState.value.copy(contests = emptyList())
                }
            
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }
}
