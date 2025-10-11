package com.example.topoclimb.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.topoclimb.data.Area
import com.example.topoclimb.repository.TopoClimbRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AreasUiState(
    val areas: List<Area> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class AreasViewModel(
    private val repository: TopoClimbRepository = TopoClimbRepository()
) : ViewModel() {
    
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
