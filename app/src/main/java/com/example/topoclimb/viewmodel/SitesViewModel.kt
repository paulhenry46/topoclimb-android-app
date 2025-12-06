package com.example.topoclimb.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.topoclimb.data.Federated
import com.example.topoclimb.data.Site
import com.example.topoclimb.repository.FederatedTopoClimbRepository
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
    val favoriteSiteId: Int? = null
)

class SitesViewModel(
    application: Application
) : AndroidViewModel(application) {
    
    private val repository = FederatedTopoClimbRepository(application)
    private val backendConfigRepository = repository.getBackendConfigRepository()
    
    private val sharedPreferences = application.getSharedPreferences(
        "favorite_sites_prefs",
        Context.MODE_PRIVATE
    )
    
    companion object {
        private const val FAVORITE_SITE_ID_KEY = "favorite_site_id"
        private const val NO_FAVORITE_SITE = -1
    }
    
    private val _uiState = MutableStateFlow(SitesUiState())
    val uiState: StateFlow<SitesUiState> = _uiState.asStateFlow()
    
    init {
        // Load persisted favorite site ID
        loadFavoriteSiteId()
        loadSites()
        // Listen to backend configuration changes and refresh sites
        viewModelScope.launch {
            backendConfigRepository.backends
                .drop(1) // Skip the first emission to avoid double loading on init
                .collect {
                    // Reload sites when backends change
                    loadSites()
                }
        }
    }
    
    private fun loadFavoriteSiteId() {
        val savedFavoriteId = sharedPreferences.getInt(FAVORITE_SITE_ID_KEY, NO_FAVORITE_SITE)
        if (savedFavoriteId != NO_FAVORITE_SITE) {
            _uiState.value = _uiState.value.copy(favoriteSiteId = savedFavoriteId)
        }
    }
    
    /**
     * Persists favorite site ID to SharedPreferences
     * @param siteId Site ID to save, or null to clear
     */
    private fun saveFavoriteSiteId(siteId: Int?) {
        sharedPreferences.edit().apply {
            if (siteId != null) {
                putInt(FAVORITE_SITE_ID_KEY, siteId)
            } else {
                remove(FAVORITE_SITE_ID_KEY)
            }
            apply()
        }
    }
    
    fun loadSites() {
        fetchSites(isRefresh = false)
    }
    
    fun refreshSites() {
        fetchSites(isRefresh = true)
    }
    
    /**
     * Fetches sites from repository and updates UI state
     * @param isRefresh Whether this is a refresh operation (affects loading state)
     */
    private fun fetchSites(isRefresh: Boolean) {
        viewModelScope.launch {
            _uiState.value = if (isRefresh) {
                _uiState.value.copy(isRefreshing = true, error = null)
            } else {
                _uiState.value.copy(isLoading = true, error = null)
            }
            
            repository.getSites()
                .onSuccess { sites ->
                    _uiState.value = _uiState.value.copy(
                        sites = sites,
                        isLoading = false,
                        isRefreshing = false
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        error = exception.message ?: "Unknown error",
                        isLoading = false,
                        isRefreshing = false
                    )
                }
        }
    }
    
    fun toggleFavorite(siteId: Int) {
        val newFavoriteId = if (_uiState.value.favoriteSiteId == siteId) null else siteId
        _uiState.value = _uiState.value.copy(favoriteSiteId = newFavoriteId)
        saveFavoriteSiteId(newFavoriteId)
    }
}
