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
import kotlinx.coroutines.launch

data class SitesUiState(
    val sites: List<Federated<Site>> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val favoriteSiteId: Int? = null
)

class SitesViewModel(
    application: Application
) : AndroidViewModel(application) {
    
    private val repository = FederatedTopoClimbRepository(application)
    
    private val _uiState = MutableStateFlow(SitesUiState())
    val uiState: StateFlow<SitesUiState> = _uiState.asStateFlow()
    
    init {
        loadSites()
    }
    
    fun loadSites() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            repository.getSites()
                .onSuccess { sites ->
                    _uiState.value = _uiState.value.copy(
                        sites = sites,
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
    
    fun toggleFavorite(siteId: Int) {
        _uiState.value = _uiState.value.copy(
            favoriteSiteId = if (_uiState.value.favoriteSiteId == siteId) null else siteId
        )
    }
}
