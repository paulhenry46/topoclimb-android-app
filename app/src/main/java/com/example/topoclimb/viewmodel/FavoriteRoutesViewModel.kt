package com.example.topoclimb.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.topoclimb.data.RouteWithMetadata
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class FavoriteRoutesUiState(
    val favoriteRoutes: List<RouteWithMetadata> = emptyList()
)

class FavoriteRoutesViewModel(
    application: Application
) : AndroidViewModel(application) {
    
    private val _uiState = MutableStateFlow(FavoriteRoutesUiState())
    val uiState: StateFlow<FavoriteRoutesUiState> = _uiState.asStateFlow()
    
    private val sharedPreferences = application.getSharedPreferences(
        "favorite_routes_prefs",
        Context.MODE_PRIVATE
    )
    private val gson = Gson()
    
    companion object {
        private const val FAVORITE_ROUTES_KEY = "favorite_routes"
    }
    
    init {
        loadFavoriteRoutes()
    }
    
    private fun loadFavoriteRoutes() {
        viewModelScope.launch {
            val jsonString = sharedPreferences.getString(FAVORITE_ROUTES_KEY, null)
            val routes = if (jsonString != null) {
                try {
                    val type = object : TypeToken<List<RouteWithMetadata>>() {}.type
                    gson.fromJson<List<RouteWithMetadata>>(jsonString, type)
                } catch (e: Exception) {
                    emptyList()
                }
            } else {
                emptyList()
            }
            _uiState.value = _uiState.value.copy(favoriteRoutes = routes)
        }
    }
    
    fun toggleFavorite(route: RouteWithMetadata) {
        viewModelScope.launch {
            val currentFavorites = _uiState.value.favoriteRoutes.toMutableList()
            val existingIndex = currentFavorites.indexOfFirst { it.id == route.id }
            
            if (existingIndex != -1) {
                // Route is already in favorites, remove it
                currentFavorites.removeAt(existingIndex)
            } else {
                // Add route to favorites
                currentFavorites.add(route)
            }
            
            // Save to SharedPreferences
            val jsonString = gson.toJson(currentFavorites)
            sharedPreferences.edit().putString(FAVORITE_ROUTES_KEY, jsonString).apply()
            
            // Update UI state
            _uiState.value = _uiState.value.copy(favoriteRoutes = currentFavorites)
        }
    }
    
    fun isFavorite(routeId: Int): Boolean {
        return _uiState.value.favoriteRoutes.any { it.id == routeId }
    }
}
