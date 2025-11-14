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
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val backendId: String? = null,
    val siteId: Int? = null
)

class SiteDetailViewModel(
    application: Application
) : AndroidViewModel(application) {
    
    private val repository = FederatedTopoClimbRepository(application)
    
    private val _uiState = MutableStateFlow(SiteDetailUiState())
    val uiState: StateFlow<SiteDetailUiState> = _uiState.asStateFlow()
    
    fun loadSiteDetails(backendId: String, siteId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true, 
                error = null,
                backendId = backendId,
                siteId = siteId
            )
            
            // Load site details (with cache)
            repository.getSite(backendId, siteId, forceRefresh = false)
                .onSuccess { site ->
                    _uiState.value = _uiState.value.copy(site = site)
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        error = exception.message ?: "Failed to load site details",
                        isLoading = false,
                        isRefreshing = false
                    )
                    return@launch
                }
            
            // Load areas (with cache)
            repository.getAreasBySite(backendId, siteId, forceRefresh = false)
                .onSuccess { areas ->
                    _uiState.value = _uiState.value.copy(areas = areas)
                }
                .onFailure { exception ->
                    // Don't fail the whole screen if areas fail to load
                    _uiState.value = _uiState.value.copy(areas = emptyList())
                }
            
            // Load contests (with cache)
            repository.getContestsBySite(backendId, siteId, forceRefresh = false)
                .onSuccess { contests ->
                    _uiState.value = _uiState.value.copy(contests = contests)
                }
                .onFailure { exception ->
                    // Don't fail the whole screen if contests fail to load
                    _uiState.value = _uiState.value.copy(contests = emptyList())
                }
            
            _uiState.value = _uiState.value.copy(isLoading = false, isRefreshing = false)
        }
    }
    
    fun refreshSiteDetails() {
        val backendId = _uiState.value.backendId ?: return
        val siteId = _uiState.value.siteId ?: return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true, error = null)
            
            // Force refresh from network
            repository.getSite(backendId, siteId, forceRefresh = true)
                .onSuccess { site ->
                    _uiState.value = _uiState.value.copy(site = site)
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        error = exception.message ?: "Failed to refresh site details",
                        isRefreshing = false
                    )
                    return@launch
                }
            
            // Force refresh areas
            repository.getAreasBySite(backendId, siteId, forceRefresh = true)
                .onSuccess { areas ->
                    _uiState.value = _uiState.value.copy(areas = areas)
                }
                .onFailure { exception ->
                    // Don't fail the whole screen if areas fail to load
                    _uiState.value = _uiState.value.copy(areas = emptyList())
                }
            
            // Force refresh contests
            repository.getContestsBySite(backendId, siteId, forceRefresh = true)
                .onSuccess { contests ->
                    _uiState.value = _uiState.value.copy(contests = contests)
                }
                .onFailure { exception ->
                    // Don't fail the whole screen if contests fail to load
                    _uiState.value = _uiState.value.copy(contests = emptyList())
                }
            
            _uiState.value = _uiState.value.copy(isRefreshing = false)
        }
    }
}
