package com.example.topoclimb.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.topoclimb.data.BackendConfig
import com.example.topoclimb.data.LoginRequest
import com.example.topoclimb.network.MultiBackendRetrofitManager
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
    val showRestartWarning: Boolean = false,
    val loginInProgress: Boolean = false,
    val loginError: String? = null,
    val instanceMeta: com.example.topoclimb.data.InstanceMeta? = null,
    val metaLoading: Boolean = false
)

class BackendManagementViewModel(
    application: Application
) : AndroidViewModel(application) {
    
    private val repository = BackendConfigRepository(application)
    private val retrofitManager = MultiBackendRetrofitManager()
    
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
        _uiState.value = _uiState.value.copy(error = null, successMessage = null, loginError = null)
    }
    
    fun dismissRestartWarning() {
        _uiState.value = _uiState.value.copy(showRestartWarning = false)
    }
    
    fun login(backendId: String, email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loginInProgress = true, loginError = null)
            
            try {
                val backend = repository.getBackend(backendId)
                if (backend == null) {
                    _uiState.value = _uiState.value.copy(
                        loginInProgress = false,
                        loginError = "Backend not found"
                    )
                    return@launch
                }
                
                val apiService = retrofitManager.getApiService(backend)
                val response = apiService.login(LoginRequest(email, password))
                
                repository.authenticateBackend(backendId, response.token, response.user)
                    .onSuccess {
                        _uiState.value = _uiState.value.copy(
                            loginInProgress = false,
                            successMessage = "Successfully logged in as ${response.user.name}"
                        )
                        // Load user's logged routes after successful login
                        try {
                            val userLogsResponse = apiService.getUserLogs("Bearer ${response.token}")
                            RouteDetailViewModel.updateSharedLoggedRoutes(userLogsResponse.data.toSet())
                        } catch (e: Exception) {
                            // Silently fail if we can't load logs
                            println("Failed to load user logs after login: ${e.message}")
                        }
                    }
                    .onFailure { exception ->
                        _uiState.value = _uiState.value.copy(
                            loginInProgress = false,
                            loginError = exception.message ?: "Failed to save authentication"
                        )
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    loginInProgress = false,
                    loginError = e.message ?: "Login failed"
                )
            }
        }
    }
    
    fun logout(backendId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(error = null, successMessage = null)
            
            repository.logoutBackend(backendId)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        successMessage = "Successfully logged out"
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        error = exception.message ?: "Failed to log out"
                    )
                }
        }
    }
    
    fun setDefaultBackend(backendId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(error = null, successMessage = null)
            
            repository.setDefaultBackend(backendId)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        successMessage = "Default instance updated"
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        error = exception.message ?: "Failed to set default instance"
                    )
                }
        }
    }
    
    fun fetchInstanceMeta(baseUrl: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(metaLoading = true, instanceMeta = null)
            
            try {
                val tempBackend = BackendConfig(
                    name = "Temp",
                    baseUrl = baseUrl,
                    enabled = true
                )
                val apiService = retrofitManager.getApiService(tempBackend)
                val meta = apiService.getMeta()
                
                _uiState.value = _uiState.value.copy(
                    instanceMeta = meta,
                    metaLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    instanceMeta = null,
                    metaLoading = false
                )
            }
        }
    }
    
    fun clearInstanceMeta() {
        _uiState.value = _uiState.value.copy(instanceMeta = null, metaLoading = false)
    }
}
