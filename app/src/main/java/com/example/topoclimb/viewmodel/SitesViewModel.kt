package com.example.topoclimb.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.topoclimb.data.Site
import com.example.topoclimb.repository.TopoClimbRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SitesUiState(
    val sites: List<Site> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val favoriteSiteId: Int? = null
)

class SitesViewModel(
    private val repository: TopoClimbRepository = TopoClimbRepository()
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SitesUiState())
    val uiState: StateFlow<SitesUiState> = _uiState.asStateFlow()
    
    init {
        loadSites()
    }
    
    fun loadSites() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            repository.getSites()
                .onSuccess { response ->
                    _uiState.value = _uiState.value.copy(
                        sites = response.data,
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
