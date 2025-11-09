package com.example.topoclimb.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.topoclimb.data.User
import com.example.topoclimb.data.UserStats
import com.example.topoclimb.data.UserUpdateRequest
import com.example.topoclimb.network.MultiBackendRetrofitManager
import com.example.topoclimb.repository.BackendConfigRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProfileUiState(
    val user: User? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isAuthenticated: Boolean = false,
    val instanceName: String? = null,
    val stats: UserStats? = null,
    val isLoadingStats: Boolean = false,
    val statsError: String? = null,
    val isUpdating: Boolean = false,
    val updateError: String? = null,
    val updateSuccess: Boolean = false,
    val debugMode: Boolean = false,
    val statsRawJson: String? = null
)

class ProfileViewModel(
    application: Application
) : AndroidViewModel(application) {
    
    private val repository = BackendConfigRepository(application)
    private val retrofitManager = MultiBackendRetrofitManager()
    
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()
    
    init {
        viewModelScope.launch {
            repository.backends.collect { backends ->
                updateProfile()
            }
        }
    }
    
    private fun updateProfile() {
        val defaultBackend = repository.getDefaultBackend()
        
        println("ProfileViewModel: updateProfile called - defaultBackend: ${defaultBackend?.name}, isAuthenticated: ${defaultBackend?.isAuthenticated()}")
        
        if (defaultBackend != null && defaultBackend.isAuthenticated()) {
            _uiState.value = _uiState.value.copy(
                user = defaultBackend.user,
                isAuthenticated = true,
                instanceName = defaultBackend.name,
                isLoading = false
            )
            // Automatically load stats when user is authenticated
            println("ProfileViewModel: User is authenticated, loading stats...")
            loadUserStats()
        } else {
            println("ProfileViewModel: User is not authenticated, resetting state")
            _uiState.value = ProfileUiState(
                user = null,
                isAuthenticated = false,
                instanceName = null,
                isLoading = false
            )
        }
    }
    
    fun refresh() {
        // Reload backends from SharedPreferences to pick up changes from other repository instances
        repository.reloadBackends()
        // Force update profile from current backend state
        updateProfile()
    }
    
    fun loadUserStats() {
        val defaultBackend = repository.getDefaultBackend()
        
        println("ProfileViewModel: loadUserStats called - defaultBackend: ${defaultBackend?.name}, isAuthenticated: ${defaultBackend?.isAuthenticated()}")
        
        if (defaultBackend == null) {
            println("ProfileViewModel: Cannot load stats - no default backend")
            _uiState.value = _uiState.value.copy(
                statsError = "No backend configured",
                isLoadingStats = false
            )
            return
        }
        
        if (!defaultBackend.isAuthenticated()) {
            println("ProfileViewModel: Cannot load stats - backend not authenticated")
            _uiState.value = _uiState.value.copy(
                statsError = "Not authenticated",
                isLoadingStats = false
            )
            return
        }
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingStats = true, statsError = null, statsRawJson = null)
            
            try {
                println("ProfileViewModel: Loading stats for backend: ${defaultBackend.name}")
                val apiService = retrofitManager.getApiService(defaultBackend)
                val authToken = "Bearer ${defaultBackend.authToken}"
                println("ProfileViewModel: Calling getUserStats with authToken: ${authToken.take(20)}...")
                
                // Get the raw JSON response for debug display
                val rawResponseBody = apiService.getUserStatsRaw(authToken)
                val rawJson = rawResponseBody.string()
                println("ProfileViewModel: Raw JSON response: $rawJson")
                
                // Parse the raw JSON to get the stats object
                val gson = com.google.gson.Gson()
                val response = gson.fromJson(rawJson, com.example.topoclimb.data.UserStatsResponse::class.java)
                
                println("ProfileViewModel: Stats loaded successfully: ${response.data}")
                
                _uiState.value = _uiState.value.copy(
                    stats = response.data,
                    isLoadingStats = false,
                    statsError = null,
                    statsRawJson = rawJson
                )
            } catch (e: Exception) {
                println("ProfileViewModel: Failed to load stats: ${e.message}")
                e.printStackTrace()
                
                // Try to capture error details
                val errorJson = "Error: ${e.javaClass.simpleName}\nMessage: ${e.message}\nCause: ${e.cause?.message}\n\nStack trace:\n${e.stackTraceToString()}"
                
                _uiState.value = _uiState.value.copy(
                    isLoadingStats = false,
                    statsError = e.message ?: "Failed to load stats",
                    statsRawJson = errorJson
                )
            }
        }
    }
    
    fun updateUserInfo(name: String?, birthDate: String?, gender: String?) {
        val defaultBackend = repository.getDefaultBackend()
        
        if (defaultBackend == null || !defaultBackend.isAuthenticated()) {
            return
        }
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUpdating = true, updateError = null, updateSuccess = false)
            
            try {
                val apiService = retrofitManager.getApiService(defaultBackend)
                val authToken = "Bearer ${defaultBackend.authToken}"
                val request = UserUpdateRequest(
                    name = name,
                    birthDate = birthDate,
                    gender = gender
                )
                
                val response = apiService.updateUser(request, authToken)
                
                // Update the backend config with the new user data
                repository.updateUserInBackend(defaultBackend.id, response.data)
                
                _uiState.value = _uiState.value.copy(
                    user = response.data,
                    isUpdating = false,
                    updateError = null,
                    updateSuccess = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isUpdating = false,
                    updateError = e.message ?: "Failed to update profile",
                    updateSuccess = false
                )
            }
        }
    }
    
    fun clearUpdateStatus() {
        _uiState.value = _uiState.value.copy(
            updateSuccess = false,
            updateError = null
        )
    }
    
    fun toggleDebugMode() {
        _uiState.value = _uiState.value.copy(
            debugMode = !_uiState.value.debugMode
        )
        println("ProfileViewModel: Debug mode ${if (_uiState.value.debugMode) "enabled" else "disabled"}")
    }
}
