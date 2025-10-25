package com.example.topoclimb.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.topoclimb.data.User
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
    val instanceName: String? = null
)

class ProfileViewModel(
    application: Application
) : AndroidViewModel(application) {
    
    private val repository = BackendConfigRepository(application)
    
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
        
        if (defaultBackend != null && defaultBackend.isAuthenticated()) {
            _uiState.value = ProfileUiState(
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
        // Force update profile from current backend state
        updateProfile()
    }
}
