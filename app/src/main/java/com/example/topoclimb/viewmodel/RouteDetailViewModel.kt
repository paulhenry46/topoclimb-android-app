package com.example.topoclimb.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.topoclimb.data.CreateLogRequest
import com.example.topoclimb.data.Log
import com.example.topoclimb.data.Route
import com.example.topoclimb.network.RetrofitInstance
import com.example.topoclimb.repository.BackendConfigRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Request

data class RouteDetailUiState(
    val route: Route? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val pictureBitmap: ByteArray? = null,
    val circleSvgContent: String? = null,
    val isFocusMode: Boolean = false,
    val isPictureLoading: Boolean = false,
    val logs: List<Log> = emptyList(),
    val isLogsLoading: Boolean = false,
    val logsError: String? = null,
    val isCreatingLog: Boolean = false,
    val createLogError: String? = null,
    val createLogSuccess: Boolean = false,
    val isRefreshingLogs: Boolean = false,
    val isRouteLogged: Boolean = false
)

class RouteDetailViewModel(
    application: Application
) : AndroidViewModel(application) {
    private val repository = BackendConfigRepository(application)
    
    private val _uiState = MutableStateFlow(RouteDetailUiState())
    val uiState: StateFlow<RouteDetailUiState> = _uiState.asStateFlow()
    
    private val _userLoggedRouteIds = MutableStateFlow<Set<Int>>(emptySet())
    val userLoggedRouteIds: StateFlow<Set<Int>> = _userLoggedRouteIds.asStateFlow()
    
    companion object {
        // Shared state for logged routes across all ViewModels
        private val _sharedLoggedRouteIds = MutableStateFlow<Set<Int>>(emptySet())
        val sharedLoggedRouteIds: StateFlow<Set<Int>> = _sharedLoggedRouteIds.asStateFlow()
        
        fun updateSharedLoggedRoutes(routeIds: Set<Int>) {
            _sharedLoggedRouteIds.value = routeIds
        }
        
        fun addLoggedRoute(routeId: Int) {
            _sharedLoggedRouteIds.value = _sharedLoggedRouteIds.value + routeId
        }
    }
    
    init {
        // Sync local state with shared state
        viewModelScope.launch {
            sharedLoggedRouteIds.collect { loggedRouteIds ->
                _userLoggedRouteIds.value = loggedRouteIds
            }
        }
    }
    
    fun loadRouteDetails(routeId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val routeResponse = RetrofitInstance.api.getRoute(routeId)
                val route = routeResponse.data
                _uiState.value = _uiState.value.copy(
                    route = route,
                    isLoading = false
                )
                
                // Load the picture and circle SVG if available
                route.picture?.let { loadPicture(it) }
                route.circle?.let { loadCircleSvg(it) }
                
                // Load logs for this route
                loadRouteLogs(routeId)
                
                // Check if this route is logged by the user
                updateRouteLoggedState(routeId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error occurred"
                )
            }
        }
    }
    
    private fun loadPicture(url: String) {
        viewModelScope.launch {
            try {
                // Picture will be loaded by Coil in the UI
                // We don't need to load it here
            } catch (e: Exception) {
                // Handle error silently
            }
        }
    }
    
    private fun loadCircleSvg(url: String) {
        viewModelScope.launch {
            try {
                // Use OkHttp to download the SVG content with IO dispatcher
                val svgContent = withContext(Dispatchers.IO) {
                    val client = RetrofitInstance.okHttpClient
                    val request = Request.Builder().url(url).build()
                    
                    client.newCall(request).execute().use { response ->
                        if (response.isSuccessful) {
                            response.body?.string()
                        } else {
                            println("RouteDetailViewModel: Failed to load SVG circle - HTTP ${response.code}")
                            null
                        }
                    }
                }
                
                if (svgContent != null) {
                    println("RouteDetailViewModel: Successfully loaded SVG circle (${svgContent.length} chars)")
                } else {
                    println("RouteDetailViewModel: SVG circle content is null")
                }
                
                _uiState.value = _uiState.value.copy(circleSvgContent = svgContent)
            } catch (e: Exception) {
                println("RouteDetailViewModel: Error loading SVG circle - ${e.message}")
                e.printStackTrace()
                // Handle error silently but log it
            }
        }
    }
    
    fun toggleFocusMode() {
        _uiState.value = _uiState.value.copy(isFocusMode = !_uiState.value.isFocusMode)
    }
    
    fun setPictureLoading(isLoading: Boolean) {
        _uiState.value = _uiState.value.copy(isPictureLoading = isLoading)
    }
    
    private fun loadRouteLogs(routeId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLogsLoading = true, logsError = null)
            
            try {
                val logsResponse = RetrofitInstance.api.getRouteLogs(routeId)
                _uiState.value = _uiState.value.copy(
                    logs = logsResponse.data,
                    isLogsLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLogsLoading = false,
                    logsError = e.message ?: "Failed to load logs"
                )
            }
        }
    }
    
    fun refreshLogs(routeId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshingLogs = true, logsError = null)
            
            try {
                val logsResponse = RetrofitInstance.api.getRouteLogs(routeId)
                _uiState.value = _uiState.value.copy(
                    logs = logsResponse.data,
                    isRefreshingLogs = false
                )
                
                // Update the logged state after refreshing
                updateRouteLoggedState(routeId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isRefreshingLogs = false,
                    logsError = e.message ?: "Failed to refresh logs"
                )
            }
        }
    }
    
    fun createLog(
        routeId: Int,
        grade: Int,
        type: String,
        way: String,
        comment: String?,
        videoUrl: String?,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isCreatingLog = true,
                createLogError = null,
                createLogSuccess = false
            )
            
            try {
                // Get auth token from repository
                val authToken = repository.getDefaultBackend()?.authToken
                if (authToken == null) {
                    _uiState.value = _uiState.value.copy(
                        isCreatingLog = false,
                        createLogError = "Not authenticated. Please log in."
                    )
                    return@launch
                }
                
                val request = CreateLogRequest(
                    grade = grade,
                    type = type,
                    way = way,
                    comment = comment?.takeIf { it.isNotBlank() },
                    videoUrl = videoUrl?.takeIf { it.isNotBlank() }
                )
                
                val response = RetrofitInstance.api.createRouteLog(
                    routeId = routeId,
                    request = request,
                    authToken = "Bearer $authToken"
                )
                
                // Add the new log to the shared logged routes set
                addLoggedRoute(routeId)
                
                _uiState.value = _uiState.value.copy(
                    isCreatingLog = false,
                    createLogSuccess = true,
                    isRouteLogged = true
                )
                
                // Refresh logs to show the new log
                refreshLogs(routeId)
                
                onSuccess()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isCreatingLog = false,
                    createLogError = e.message ?: "Failed to create log"
                )
            }
        }
    }
    
    fun resetCreateLogState() {
        _uiState.value = _uiState.value.copy(
            createLogError = null,
            createLogSuccess = false
        )
    }
    
    fun loadUserLoggedRoutes() {
        viewModelScope.launch {
            try {
                val authToken = repository.getDefaultBackend()?.authToken
                if (authToken != null) {
                    val response = RetrofitInstance.api.getUserLogs("Bearer $authToken")
                    updateSharedLoggedRoutes(response.data.toSet())
                }
            } catch (e: Exception) {
                // Silently fail - user might not be authenticated
                println("Failed to load user logged routes: ${e.message}")
            }
        }
    }
    
    private fun updateRouteLoggedState(routeId: Int) {
        _uiState.value = _uiState.value.copy(
            isRouteLogged = _userLoggedRouteIds.value.contains(routeId)
        )
    }
}
