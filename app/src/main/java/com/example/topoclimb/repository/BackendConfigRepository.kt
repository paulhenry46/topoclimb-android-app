package com.example.topoclimb.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.topoclimb.AppConfig
import com.example.topoclimb.data.BackendConfig
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Repository for managing backend configurations
 * Uses SharedPreferences for persistence
 */
class BackendConfigRepository(context: Context) {
    
    private val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    private val gson = Gson()
    
    private val _backends = MutableStateFlow<List<BackendConfig>>(emptyList())
    val backends: StateFlow<List<BackendConfig>> = _backends.asStateFlow()
    
    init {
        loadBackends()
    }
    
    /**
     * Load backends from SharedPreferences
     * If no backends exist, initialize with default backend from AppConfig
     */
    private fun loadBackends() {
        val json = sharedPreferences.getString(KEY_BACKENDS, null)
        val loadedBackends = if (json != null) {
            val type = object : TypeToken<List<BackendConfig>>() {}.type
            gson.fromJson<List<BackendConfig>>(json, type)
        } else {
            // Initialize with default backend from AppConfig
            listOf(
                BackendConfig(
                    name = "Default Backend",
                    baseUrl = AppConfig.API_BASE_URL,
                    enabled = true
                )
            )
        }
        _backends.value = loadedBackends
        // Save if we initialized with default
        if (json == null) {
            saveBackends()
        }
    }
    
    /**
     * Save backends to SharedPreferences
     */
    private fun saveBackends() {
        val json = gson.toJson(_backends.value)
        sharedPreferences.edit().putString(KEY_BACKENDS, json).apply()
    }
    
    /**
     * Add a new backend configuration
     */
    fun addBackend(backend: BackendConfig): Result<BackendConfig> {
        return try {
            if (!backend.isValid()) {
                return Result.failure(IllegalArgumentException("Invalid backend URL"))
            }
            
            // Check for duplicate URL
            if (_backends.value.any { it.baseUrl == backend.baseUrl }) {
                return Result.failure(IllegalArgumentException("A TopoClimb instance with this URL already exists"))
            }
            
            val updatedBackends = _backends.value + backend
            _backends.value = updatedBackends
            saveBackends()
            Result.success(backend)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Update an existing backend configuration
     */
    fun updateBackend(backend: BackendConfig): Result<BackendConfig> {
        return try {
            if (!backend.isValid()) {
                return Result.failure(IllegalArgumentException("Invalid backend URL"))
            }
            
            // Check for duplicate URL (excluding the backend being updated)
            if (_backends.value.any { it.baseUrl == backend.baseUrl && it.id != backend.id }) {
                return Result.failure(IllegalArgumentException("A TopoClimb instance with this URL already exists"))
            }
            
            val updatedBackends = _backends.value.map { 
                if (it.id == backend.id) {
                    backend.copy(updatedAt = System.currentTimeMillis())
                } else {
                    it
                }
            }
            _backends.value = updatedBackends
            saveBackends()
            Result.success(backend)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Delete a backend configuration
     */
    fun deleteBackend(backendId: String): Result<Unit> {
        return try {
            val updatedBackends = _backends.value.filter { it.id != backendId }
            if (updatedBackends.isEmpty()) {
                return Result.failure(IllegalStateException("Cannot delete the last TopoClimb instance"))
            }
            _backends.value = updatedBackends
            saveBackends()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Toggle backend enabled state
     */
    fun toggleBackendEnabled(backendId: String): Result<Unit> {
        return try {
            val updatedBackends = _backends.value.map { 
                if (it.id == backendId) {
                    it.copy(
                        enabled = !it.enabled, 
                        updatedAt = System.currentTimeMillis()
                    )
                } else {
                    it
                }
            }
            
            // Ensure at least one backend is enabled
            if (updatedBackends.none { it.enabled }) {
                return Result.failure(IllegalStateException("At least one TopoClimb instance must be enabled"))
            }
            
            _backends.value = updatedBackends
            saveBackends()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get all enabled backends
     */
    fun getEnabledBackends(): List<BackendConfig> {
        return _backends.value.filter { it.enabled }
    }
    
    /**
     * Get a specific backend by ID
     */
    fun getBackend(backendId: String): BackendConfig? {
        return _backends.value.find { it.id == backendId }
    }
    
    /**
     * Get the default backend for user profile data
     */
    fun getDefaultBackend(): BackendConfig? {
        return _backends.value.find { it.isDefault } 
            ?: _backends.value.find { it.isAuthenticated() }
    }
    
    /**
     * Set a backend as the default for user profile data
     */
    fun setDefaultBackend(backendId: String): Result<Unit> {
        return try {
            val updatedBackends = _backends.value.map { 
                it.copy(
                    isDefault = it.id == backendId,
                    updatedAt = System.currentTimeMillis()
                )
            }
            _backends.value = updatedBackends
            saveBackends()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Authenticate a backend with user credentials
     */
    fun authenticateBackend(backendId: String, authToken: String, user: com.example.topoclimb.data.User): Result<Unit> {
        return try {
            val updatedBackends = _backends.value.map { 
                if (it.id == backendId) {
                    it.copy(
                        authToken = authToken,
                        user = user,
                        isDefault = _backends.value.none { b -> b.isDefault },
                        updatedAt = System.currentTimeMillis()
                    )
                } else {
                    it
                }
            }
            _backends.value = updatedBackends
            saveBackends()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Log out from a backend
     */
    fun logoutBackend(backendId: String): Result<Unit> {
        return try {
            val updatedBackends = _backends.value.map { 
                if (it.id == backendId) {
                    it.copy(
                        authToken = null,
                        user = null,
                        isDefault = false,
                        updatedAt = System.currentTimeMillis()
                    )
                } else {
                    it
                }
            }
            _backends.value = updatedBackends
            saveBackends()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    companion object {
        private const val PREFS_NAME = "topoclimb_backends"
        private const val KEY_BACKENDS = "backends"
    }
}
