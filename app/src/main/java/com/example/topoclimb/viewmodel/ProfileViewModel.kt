package com.example.topoclimb.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.topoclimb.cache.CacheManager
import com.example.topoclimb.cache.CachePreferences
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
    val userStats: UserStats? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isAuthenticated: Boolean = false,
    val instanceName: String? = null,
    val isUpdating: Boolean = false,
    val updateError: String? = null,
    val updateSuccess: Boolean = false,
    val isLoadingStats: Boolean = false,
    val statsError: String? = null,
    val qrCodeUrl: String? = null,
    val isLoadingQRCode: Boolean = false,
    val qrCodeError: String? = null,
    val authenticatedBackends: List<com.example.topoclimb.data.BackendConfig> = emptyList(),
    val isCacheEnabled: Boolean = true
)

class ProfileViewModel(
    application: Application
) : AndroidViewModel(application) {
    
    private val repository = BackendConfigRepository(application)
    private val retrofitManager = MultiBackendRetrofitManager()
    private val cacheManager = CacheManager(application)
    private val cachePreferences = CachePreferences(application)
    
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()
    
    init {
        // Load cache preference
        _uiState.value = _uiState.value.copy(isCacheEnabled = cachePreferences.isCacheEnabled)
        
        viewModelScope.launch {
            repository.backends.collect { backends ->
                updateProfile()
                updateAuthenticatedBackends(backends)
            }
        }
    }
    
    private fun updateAuthenticatedBackends(backends: List<com.example.topoclimb.data.BackendConfig>) {
        val authenticated = backends.filter { it.isAuthenticated() }
        _uiState.value = _uiState.value.copy(authenticatedBackends = authenticated)
    }
    
    private fun updateProfile() {
        val defaultBackend = repository.getDefaultBackend()
        
        if (defaultBackend != null && defaultBackend.isAuthenticated()) {
            _uiState.value = _uiState.value.copy(
                user = defaultBackend.user,
                isAuthenticated = true,
                instanceName = defaultBackend.name,
                isLoading = false
            )
        } else {
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
        // Fetch user stats
        fetchUserStats()
    }
    
    private fun fetchUserStats() {
        val defaultBackend = repository.getDefaultBackend()
        
        if (defaultBackend == null || !defaultBackend.isAuthenticated()) {
            return
        }
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingStats = true, statsError = null)
            
            try {
                val apiService = retrofitManager.getApiService(defaultBackend)
                val authToken = "Bearer ${defaultBackend.authToken}"
                val stats = apiService.getUserStats(authToken)
                
                _uiState.value = _uiState.value.copy(
                    userStats = stats,
                    isLoadingStats = false,
                    statsError = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoadingStats = false,
                    statsError = e.message ?: "Failed to load user stats"
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
    
    fun fetchQRCode(backendId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingQRCode = true, qrCodeError = null, qrCodeUrl = null)
            
            try {
                val backend = repository.getBackend(backendId)
                if (backend == null || !backend.isAuthenticated()) {
                    _uiState.value = _uiState.value.copy(
                        isLoadingQRCode = false,
                        qrCodeError = "Not authenticated to this instance"
                    )
                    return@launch
                }
                
                val apiService = retrofitManager.getApiService(backend)
                val authToken = "Bearer ${backend.authToken}"
                val response = apiService.getUserQRCode(authToken)
                
                _uiState.value = _uiState.value.copy(
                    qrCodeUrl = response.url,
                    isLoadingQRCode = false,
                    qrCodeError = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoadingQRCode = false,
                    qrCodeError = e.message ?: "Failed to load QR code"
                )
            }
        }
    }
    
    fun clearQRCode() {
        _uiState.value = _uiState.value.copy(
            qrCodeUrl = null,
            isLoadingQRCode = false,
            qrCodeError = null
        )
    }
    
    fun toggleCache(enabled: Boolean) {
        cachePreferences.isCacheEnabled = enabled
        _uiState.value = _uiState.value.copy(isCacheEnabled = enabled)
        
        // If cache is disabled, clear it
        if (!enabled) {
            clearCache()
        }
    }
    
    fun clearCache() {
        viewModelScope.launch {
            try {
                cacheManager.clearAllCache()
            } catch (e: Exception) {
                // Silently fail - cache clearing is not critical
            }
        }
    }
}
