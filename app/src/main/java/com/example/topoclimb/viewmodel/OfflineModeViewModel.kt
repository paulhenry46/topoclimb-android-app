package com.example.topoclimb.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.topoclimb.utils.NetworkConnectivityManager
import com.example.topoclimb.utils.OfflineModeManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * ViewModel for managing global offline mode state
 */
class OfflineModeViewModel(application: Application) : AndroidViewModel(application) {
    
    private val networkManager = NetworkConnectivityManager(application)
    private val offlineModeManager = OfflineModeManager(application)
    
    private val _isOnline = MutableStateFlow(true)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()
    
    private val _isOfflineModeActive = MutableStateFlow(false)
    val isOfflineModeActive: StateFlow<Boolean> = _isOfflineModeActive.asStateFlow()
    
    private val _showOfflineDialog = MutableStateFlow(false)
    val showOfflineDialog: StateFlow<Boolean> = _showOfflineDialog.asStateFlow()
    
    init {
        // Monitor network connectivity
        viewModelScope.launch {
            networkManager.isNetworkAvailable.collect { isAvailable ->
                val wasOnline = _isOnline.value
                _isOnline.value = isAvailable
                
                // Show dialog when transitioning from online to offline
                if (wasOnline && !isAvailable) {
                    _showOfflineDialog.value = true
                }
            }
        }
        
        // Combine network state and offline mode settings
        viewModelScope.launch {
            combine(
                _isOnline,
                offlineModeManager.isOfflineModeEnabled
            ) { isOnline, isOfflineEnabled ->
                // Offline mode is active when:
                // 1. Feature is enabled AND
                // 2. Network is not available
                !isOnline && isOfflineEnabled
            }.collect { isOfflineActive ->
                _isOfflineModeActive.value = isOfflineActive
            }
        }
    }
    
    /**
     * Show the offline mode dialog
     */
    fun showOfflineDialog() {
        _showOfflineDialog.value = true
    }
    
    /**
     * Dismiss the offline mode dialog
     */
    fun dismissOfflineDialog() {
        _showOfflineDialog.value = false
    }
    
    /**
     * Force a retry of network connection
     */
    fun retryConnection() {
        viewModelScope.launch {
            val isConnected = networkManager.checkCurrentConnectivity()
            _isOnline.value = isConnected
            if (isConnected) {
                _showOfflineDialog.value = false
            }
        }
    }
    
    /**
     * Enable or disable offline mode feature
     */
    fun setOfflineModeEnabled(enabled: Boolean) {
        offlineModeManager.setOfflineModeEnabled(enabled)
    }
    
    /**
     * Get whether offline mode feature is enabled
     */
    fun isOfflineModeFeatureEnabled(): Boolean {
        return offlineModeManager.getOfflineModeEnabled()
    }
    
    companion object {
        @Volatile
        private var instance: OfflineModeViewModel? = null
        
        fun getInstance(application: Application): OfflineModeViewModel {
            return instance ?: synchronized(this) {
                instance ?: OfflineModeViewModel(application).also { instance = it }
            }
        }
    }
}
