package com.example.topoclimb.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.topoclimb.data.BackendConfig
import com.example.topoclimb.repository.BackendConfigRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class BackendManagementUiState(
    val backends: List<BackendConfig> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val showRestartWarning: Boolean = false
)

class BackendManagementViewModel(
    application: Application
) : AndroidViewModel(application) {
    
    private val repository = BackendConfigRepository(application)
    
    private val _uiState = MutableStateFlow(BackendManagementUiState())
    val uiState: StateFlow<BackendManagementUiState> = _uiState.asStateFlow()
    
    init {
        viewModelScope.launch {
            repository.backends.collect { backends ->
                _uiState.value = _uiState.value.copy(backends = backends)
            }
        }
    }
    
    fun addBackend(name: String, baseUrl: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, successMessage = null)
            
            val backend = BackendConfig(
                name = name,
                baseUrl = baseUrl,
                enabled = true
            )
            
            repository.addBackend(backend)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "TopoClimb instance added successfully",
                        showRestartWarning = true
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Failed to add TopoClimb instance"
                    )
                }
        }
    }
    
    fun updateBackend(backend: BackendConfig) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, successMessage = null)
            
            repository.updateBackend(backend)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "TopoClimb instance updated successfully",
                        showRestartWarning = true
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Failed to update TopoClimb instance"
                    )
                }
        }
    }
    
    fun deleteBackend(backendId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, successMessage = null)
            
            repository.deleteBackend(backendId)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "TopoClimb instance deleted successfully",
                        showRestartWarning = true
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Failed to delete TopoClimb instance"
                    )
                }
        }
    }
    
    fun toggleBackendEnabled(backendId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(error = null, successMessage = null)
            
            repository.toggleBackendEnabled(backendId)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        successMessage = "TopoClimb instance status updated",
                        showRestartWarning = true
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        error = exception.message ?: "Failed to toggle TopoClimb instance status"
                    )
                }
        }
    }
    
    fun clearMessages() {
        _uiState.value = _uiState.value.copy(error = null, successMessage = null)
    }
    
    fun dismissRestartWarning() {
        _uiState.value = _uiState.value.copy(showRestartWarning = false)
    }
}
