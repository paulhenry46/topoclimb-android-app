package com.example.topoclimb.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.topoclimb.data.Federated
import com.example.topoclimb.data.Site
import com.example.topoclimb.repository.FederatedTopoClimbRepository
import com.example.topoclimb.repository.OfflineRepository
import com.example.topoclimb.utils.NetworkConnectivityManager
import com.example.topoclimb.utils.OfflineModeManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch

data class SitesUiState(
    val sites: List<Federated<Site>> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val favoriteSiteId: Int? = null,
    val offlineSites: Set<Int> = emptySet(),
    val isOfflineMode: Boolean = false
)

class SitesViewModel(
    application: Application
) : AndroidViewModel(application) {
    
    private val repository = FederatedTopoClimbRepository(application)
    private val offlineRepository = OfflineRepository(application)
    private val offlineModeManager = OfflineModeManager(application)
    private val networkManager = NetworkConnectivityManager(application)
    private val backendConfigRepository = repository.getBackendConfigRepository()
    
    private val _uiState = MutableStateFlow(SitesUiState())
    val uiState: StateFlow<SitesUiState> = _uiState.asStateFlow()
    
    init {
        // Start initial load immediately
        loadSites()
        
        // Monitor offline sites changes
        viewModelScope.launch {
            try {
                offlineModeManager.offlineSites.collect { offlineSites ->
                    _uiState.value = _uiState.value.copy(offlineSites = offlineSites)
                }
            } catch (e: Exception) {
                println("Error collecting offline sites: ${e.message}")
            }
        }
        
        // Listen to backend configuration changes and refresh sites
        viewModelScope.launch {
            try {
                backendConfigRepository.backends
                    .drop(1) // Skip the first emission to avoid double loading on init
                    .collect {
                        // Reload sites when backends change
                        loadSites()
                    }
            } catch (e: Exception) {
                println("Error collecting backend changes: ${e.message}")
            }
        }
        
        // Monitor network connectivity
        viewModelScope.launch {
            try {
                networkManager.isNetworkAvailable
                    .drop(1) // Skip initial emission since we already called loadSites()
                    .collect { isOnline ->
                        _uiState.value = _uiState.value.copy(isOfflineMode = !isOnline)
                        if (isOnline) {
                            // When coming back online, reload sites from network
                            loadSites()
                        }
                        // Note: We don't load offline sites here on offline transition
                        // because loadSites() already handles fallback to cache on failure
                    }
            } catch (e: Exception) {
                println("Error monitoring network: ${e.message}")
            }
        }
    }
    
    fun loadSites() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            repository.getSites()
                .onSuccess { sites ->
                    _uiState.value = _uiState.value.copy(
                        sites = sites,
                        isLoading = false,
                        isRefreshing = false
                    )
                }
                .onFailure { exception ->
                    // If network fails, try loading from offline cache
                    loadOfflineSites()
                    _uiState.value = _uiState.value.copy(
                        error = exception.message ?: "Unknown error",
                        isLoading = false,
                        isRefreshing = false
                    )
                }
        }
    }
    
    private fun loadOfflineSites() {
        viewModelScope.launch {
            try {
                val cachedSites = offlineRepository.getCachedSites()
                if (cachedSites.isNotEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        sites = cachedSites,
                        isLoading = false,
                        isRefreshing = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                println("Error loading offline sites: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isRefreshing = false
                )
            }
        }
    }
    
    fun refreshSites() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true, error = null)
            repository.getSites()
                .onSuccess { sites ->
                    _uiState.value = _uiState.value.copy(
                        sites = sites,
                        isRefreshing = false
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        error = exception.message ?: "Unknown error",
                        isRefreshing = false
                    )
                }
        }
    }
    
    fun toggleFavorite(siteId: Int) {
        _uiState.value = _uiState.value.copy(
            favoriteSiteId = if (_uiState.value.favoriteSiteId == siteId) null else siteId
        )
    }
    
    fun toggleOfflineMode(siteId: Int, backendId: String, backendName: String) {
        viewModelScope.launch {
            if (offlineModeManager.isSiteOfflineEnabled(siteId)) {
                // Remove from offline mode
                offlineModeManager.removeOfflineSite(siteId)
                offlineRepository.removeCachedSite(siteId)
            } else {
                // Add to offline mode and cache the site data
                offlineModeManager.addOfflineSite(siteId)
                // Cache the site and its data
                cacheSiteData(siteId, backendId, backendName)
            }
        }
    }
    
    private suspend fun cacheSiteData(siteId: Int, backendId: String, backendName: String) {
        try {
            // Fetch and cache site details
            val siteResult = repository.getSite(backendId, siteId)
            siteResult.onSuccess { federatedSite ->
                offlineRepository.cacheSite(federatedSite.data, backendId, backendName)
                
                // Fetch and cache areas
                val areasResult = repository.getAreasBySite(backendId, siteId)
                areasResult.onSuccess { federatedAreas ->
                    val areas = federatedAreas.map { it.data }
                    offlineRepository.cacheAreas(areas)
                }
                
                // Fetch and cache routes for this site
                val routesResult = repository.getRoutes(siteId = siteId)
                routesResult.onSuccess { federatedRoutes ->
                    val routes = federatedRoutes.map { it.data }
                    offlineRepository.cacheRoutes(routes)
                }
            }
        } catch (e: Exception) {
            // Silently fail - the site might not be fully cached
            println("Failed to cache site data: ${e.message}")
        }
    }
}
