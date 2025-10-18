package com.example.topoclimb.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.topoclimb.data.Log
import com.example.topoclimb.data.Route
import com.example.topoclimb.network.RetrofitInstance
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
    val logsError: String? = null
)

class RouteDetailViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(RouteDetailUiState())
    val uiState: StateFlow<RouteDetailUiState> = _uiState.asStateFlow()
    
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
    
    fun loadRouteLogs(routeId: Int) {
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
}
