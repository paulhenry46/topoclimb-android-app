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
    val successMessage: String? = null
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
                        successMessage = "Backend added successfully"
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Failed to add backend"
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
                        successMessage = "Backend updated successfully"
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Failed to update backend"
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
                        successMessage = "Backend deleted successfully"
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Failed to delete backend"
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
                        successMessage = "Backend status updated"
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        error = exception.message ?: "Failed to toggle backend status"
                    )
                }
        }
    }
    
    fun clearMessages() {
        _uiState.value = _uiState.value.copy(error = null, successMessage = null)
    }
}
