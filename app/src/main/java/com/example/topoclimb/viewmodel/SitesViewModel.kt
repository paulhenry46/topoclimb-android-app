package com.example.topoclimb.viewmodel

import android.app.Application
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
    
    private val _uiState = MutableStateFlow(SitesUiState())
    val uiState: StateFlow<SitesUiState> = _uiState.asStateFlow()
    
    init {
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
                    _uiState.value = _uiState.value.copy(
                        error = exception.message ?: "Unknown error",
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
}
