package com.example.topoclimb.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.topoclimb.data.Route
import com.example.topoclimb.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RouteDetailUiState(
    val route: Route? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val pictureBitmap: ByteArray? = null,
    val circleSvgContent: String? = null,
    val isFocusMode: Boolean = false
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
                // Use OkHttp to download the SVG content
                val client = RetrofitInstance.okHttpClient
                val request = okhttp3.Request.Builder().url(url).build()
                val response = client.newCall(request).execute()
                
                if (response.isSuccessful) {
                    val svgContent = response.body?.string()
                    _uiState.value = _uiState.value.copy(circleSvgContent = svgContent)
                }
            } catch (e: Exception) {
                // Handle error silently
            }
        }
    }
    
    fun toggleFocusMode() {
        _uiState.value = _uiState.value.copy(isFocusMode = !_uiState.value.isFocusMode)
    }
}
