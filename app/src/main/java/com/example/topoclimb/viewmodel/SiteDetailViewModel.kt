package com.example.topoclimb.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.topoclimb.data.Area
import com.example.topoclimb.data.Contest
import com.example.topoclimb.data.Federated
import com.example.topoclimb.data.Site
import com.example.topoclimb.repository.FederatedTopoClimbRepository
import com.example.topoclimb.repository.OfflineRepository
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
    val siteId: Int? = null,
    val isOfflineData: Boolean = false
)

class SiteDetailViewModel(
    application: Application
) : AndroidViewModel(application) {
    
    private val repository = FederatedTopoClimbRepository(application)
    private val offlineRepository = OfflineRepository(application)
    
    private val _uiState = MutableStateFlow(SiteDetailUiState())
    val uiState: StateFlow<SiteDetailUiState> = _uiState.asStateFlow()
    
    fun loadSiteDetails(backendId: String, siteId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true, 
                error = null,
                backendId = backendId,
                siteId = siteId,
                isOfflineData = false
            )
            
            // Try to load from network first
            var networkSuccess = true
            
            // Load site details
            repository.getSite(backendId, siteId)
                .onSuccess { site ->
                    _uiState.value = _uiState.value.copy(site = site)
                }
                .onFailure { exception ->
                    networkSuccess = false
                    // Try loading from offline cache
                    loadOfflineSiteDetails(siteId)
                    if (_uiState.value.site == null) {
                        _uiState.value = _uiState.value.copy(
                            error = exception.message ?: "Failed to load site details",
                            isLoading = false,
                            isRefreshing = false
                        )
                        return@launch
                    }
                }
            
            if (networkSuccess) {
                // Load areas from network
                repository.getAreasBySite(backendId, siteId)
                    .onSuccess { areas ->
                        _uiState.value = _uiState.value.copy(areas = areas)
                    }
                    .onFailure { exception ->
                        // Try loading from offline cache if network fails
                        loadOfflineAreas(siteId)
                    }
                
                // Load contests from network
                repository.getContestsBySite(backendId, siteId)
                    .onSuccess { contests ->
                        _uiState.value = _uiState.value.copy(contests = contests)
                    }
                    .onFailure { exception ->
                        // Try loading from offline cache if network fails
                        loadOfflineContests(siteId)
                    }
            } else {
                // Network failed, load everything from offline cache
                loadOfflineAreas(siteId)
                loadOfflineContests(siteId)
            }
            
            _uiState.value = _uiState.value.copy(isLoading = false, isRefreshing = false)
        }
    }
    
    private suspend fun loadOfflineSiteDetails(siteId: Int) {
        try {
            val cachedSite = offlineRepository.getCachedSite(siteId)
            if (cachedSite != null) {
                _uiState.value = _uiState.value.copy(
                    site = Federated(
                        data = cachedSite,
                        backend = com.example.topoclimb.data.BackendMetadata(
                            backendId = "",
                            backendName = "Offline",
                            baseUrl = ""
                        )
                    ),
                    isOfflineData = true
                )
            }
        } catch (e: Exception) {
            println("Failed to load offline site: ${e.message}")
        }
    }
    
    private suspend fun loadOfflineAreas(siteId: Int) {
        try {
            val cachedAreas = offlineRepository.getCachedAreas(siteId)
            _uiState.value = _uiState.value.copy(
                areas = cachedAreas.map { area ->
                    Federated(
                        data = area,
                        backend = com.example.topoclimb.data.BackendMetadata(
                            backendId = "",
                            backendName = "Offline",
                            baseUrl = ""
                        )
                    )
                },
                isOfflineData = true
            )
        } catch (e: Exception) {
            println("Failed to load offline areas: ${e.message}")
        }
    }
    
    private suspend fun loadOfflineContests(siteId: Int) {
        try {
            val cachedContests = offlineRepository.getCachedContests(siteId)
            _uiState.value = _uiState.value.copy(
                contests = cachedContests.map { contest ->
                    Federated(
                        data = contest,
                        backend = com.example.topoclimb.data.BackendMetadata(
                            backendId = "",
                            backendName = "Offline",
                            baseUrl = ""
                        )
                    )
                },
                isOfflineData = true
            )
        } catch (e: Exception) {
            println("Failed to load offline contests: ${e.message}")
        }
    }
    
    fun refreshSiteDetails() {
        val backendId = _uiState.value.backendId ?: return
        val siteId = _uiState.value.siteId ?: return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true, error = null)
            
            // Load site details
            repository.getSite(backendId, siteId)
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
            
            _uiState.value = _uiState.value.copy(isRefreshing = false)
        }
    }
}
