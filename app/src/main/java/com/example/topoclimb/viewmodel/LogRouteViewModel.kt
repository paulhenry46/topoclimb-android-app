package com.example.topoclimb.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.topoclimb.data.CreateLogRequest
import com.example.topoclimb.data.GradingSystem
import com.example.topoclimb.data.Site
import com.example.topoclimb.network.RetrofitInstance
import com.example.topoclimb.repository.BackendConfigRepository
import com.example.topoclimb.repository.FederatedTopoClimbRepository
import com.example.topoclimb.utils.NetworkConnectivityManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LogRouteUiState(
    val gradingSystem: GradingSystem? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
    val isOfflineMode: Boolean = false
)

class LogRouteViewModel(application: Application) : AndroidViewModel(application) {
    
    private val backendConfigRepository = BackendConfigRepository(application)
    private val federatedRepository = FederatedTopoClimbRepository(application)
    private val networkManager = NetworkConnectivityManager(application)
    
    private val _uiState = MutableStateFlow(LogRouteUiState())
    val uiState: StateFlow<LogRouteUiState> = _uiState.asStateFlow()
    
    init {
        // Monitor network connectivity
        viewModelScope.launch {
            networkManager.isNetworkAvailable.collect { isOnline ->
                _uiState.value = _uiState.value.copy(isOfflineMode = !isOnline)
            }
        }
    }
    
    /**
     * Load the grading system for a specific site
     */
    fun loadGradingSystem(backendId: String, siteId: Int) {
        viewModelScope.launch {
            try {
                val siteResult = federatedRepository.getSite(backendId, siteId)
                siteResult.onSuccess { federatedSite ->
                    _uiState.value = _uiState.value.copy(
                        gradingSystem = federatedSite.data.gradingSystem
                    )
                }.onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        error = "Failed to load grading system: ${exception.message}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to load grading system: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Create a route log
     */
    fun createRouteLog(
        routeId: Int,
        grade: Int,
        climbingType: String,
        climbingWay: String,
        comment: String?,
        videoUrl: String?
    ) {
        viewModelScope.launch {
            // Check if we're in offline mode
            if (_uiState.value.isOfflineMode) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Cannot log routes in offline mode. Please connect to the internet."
                )
                return@launch
            }
            
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val backend = backendConfigRepository.getDefaultBackend()
                if (backend?.authToken == null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Not authenticated"
                    )
                    return@launch
                }
                
                val request = CreateLogRequest(
                    grade = grade,
                    type = climbingType,
                    way = climbingWay,
                    comment = comment,
                    videoUrl = videoUrl
                )
                
                RetrofitInstance.api.createRouteLog(
                    routeId = routeId,
                    request = request,
                    authToken = "Bearer ${backend.authToken}"
                )
                
                // Update shared logged routes
                val response = RetrofitInstance.api.getUserLogs("Bearer ${backend.authToken}")
                RouteDetailViewModel.updateSharedLoggedRoutes(response.data.toSet())
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSuccess = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to create log"
                )
            }
        }
    }
    
    /**
     * Reset the success state after navigation
     */
    fun resetSuccessState() {
        _uiState.value = _uiState.value.copy(isSuccess = false)
    }
}
