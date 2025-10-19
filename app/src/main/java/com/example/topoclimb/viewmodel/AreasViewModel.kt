package com.example.topoclimb.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.topoclimb.data.Area
import com.example.topoclimb.data.Federated
import com.example.topoclimb.repository.FederatedTopoClimbRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AreasUiState(
    val areas: List<Federated<Area>> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class AreasViewModel(
    application: Application
) : AndroidViewModel(application) {
    
    private val repository = FederatedTopoClimbRepository(application)
    
    private val _uiState = MutableStateFlow(AreasUiState())
    val uiState: StateFlow<AreasUiState> = _uiState.asStateFlow()
    
    init {
        loadAreas()
    }
    
    fun loadAreas() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            repository.getAreas()
                .onSuccess { areas ->
                    _uiState.value = _uiState.value.copy(
                        areas = areas,
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
}
