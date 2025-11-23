package com.example.topoclimb.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.topoclimb.data.AddFriendRequest
import com.example.topoclimb.data.BackendConfig
import com.example.topoclimb.data.Friend
import com.example.topoclimb.network.MultiBackendRetrofitManager
import com.example.topoclimb.repository.BackendConfigRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Data class to hold friend with their instance information
 */
data class FriendWithInstance(
    val friend: Friend,
    val instanceName: String,
    val backendId: String
)

data class FriendsUiState(
    val friends: List<FriendWithInstance> = emptyList(),
    val searchResults: List<FriendWithInstance> = emptyList(),
    val searchQuery: String = "",
    val isLoadingFriends: Boolean = false,
    val isLoadingSearch: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val isAuthenticated: Boolean = false
)

class FriendsViewModel(
    application: Application
) : AndroidViewModel(application) {
    
    private val repository = BackendConfigRepository(application)
    private val retrofitManager = MultiBackendRetrofitManager()
    
    private val _uiState = MutableStateFlow(FriendsUiState())
    val uiState: StateFlow<FriendsUiState> = _uiState.asStateFlow()
    
    private var searchJob: Job? = null
    
    init {
        viewModelScope.launch {
            repository.backends.collect { backends ->
                val isAuth = backends.any { it.isAuthenticated() }
                _uiState.value = _uiState.value.copy(isAuthenticated = isAuth)
                if (isAuth) {
                    loadFriends()
                }
            }
        }
    }
    
    fun loadFriends() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingFriends = true, error = null)
            
            try {
                val backends = repository.backends.value
                val allFriends = mutableListOf<FriendWithInstance>()
                
                // Fetch friends from all authenticated backends
                backends.filter { it.isAuthenticated() }.forEach { backend ->
                    try {
                        val apiService = retrofitManager.getApiService(backend)
                        val authToken = "Bearer ${backend.authToken}"
                        val response = apiService.getFriends(authToken)
                        
                        response.data.forEach { friend ->
                            allFriends.add(
                                FriendWithInstance(
                                    friend = friend,
                                    instanceName = backend.name,
                                    backendId = backend.id
                                )
                            )
                        }
                    } catch (e: Exception) {
                        // Log error but continue with other backends
                        println("Error loading friends from ${backend.name}: ${e.message}")
                    }
                }
                
                _uiState.value = _uiState.value.copy(
                    friends = allFriends,
                    isLoadingFriends = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to load friends: ${e.message}",
                    isLoadingFriends = false
                )
            }
        }
    }
    
    fun searchUsers(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        
        // Cancel previous search job
        searchJob?.cancel()
        
        if (query.length < 2) {
            _uiState.value = _uiState.value.copy(searchResults = emptyList())
            return
        }
        
        // Debounce search by 300ms
        searchJob = viewModelScope.launch {
            delay(300)
            
            _uiState.value = _uiState.value.copy(isLoadingSearch = true, error = null)
            
            try {
                val backends = repository.backends.value
                val allResults = mutableListOf<FriendWithInstance>()
                
                // Search users from all authenticated backends
                backends.filter { it.isAuthenticated() }.forEach { backend ->
                    try {
                        val apiService = retrofitManager.getApiService(backend)
                        val authToken = "Bearer ${backend.authToken}"
                        val response = apiService.searchUsers(query, authToken)
                        
                        response.data.forEach { user ->
                            allResults.add(
                                FriendWithInstance(
                                    friend = user,
                                    instanceName = backend.name,
                                    backendId = backend.id
                                )
                            )
                        }
                    } catch (e: Exception) {
                        // Log error but continue with other backends
                        println("Error searching users from ${backend.name}: ${e.message}")
                    }
                }
                
                _uiState.value = _uiState.value.copy(
                    searchResults = allResults,
                    isLoadingSearch = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to search users: ${e.message}",
                    isLoadingSearch = false
                )
            }
        }
    }
    
    fun addFriend(friendId: Int, backendId: String) {
        viewModelScope.launch {
            try {
                val backend = repository.backends.value.find { it.id == backendId }
                if (backend == null || !backend.isAuthenticated()) {
                    _uiState.value = _uiState.value.copy(error = "Backend not found or not authenticated")
                    return@launch
                }
                
                val apiService = retrofitManager.getApiService(backend)
                val authToken = "Bearer ${backend.authToken}"
                val request = AddFriendRequest(friendId = friendId)
                val response = apiService.addFriend(request, authToken)
                
                _uiState.value = _uiState.value.copy(
                    successMessage = response.message
                )
                
                // Reload friends list
                loadFriends()
                
                // Clear search results to show updated state
                _uiState.value = _uiState.value.copy(searchResults = emptyList(), searchQuery = "")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to add friend: ${e.message}"
                )
            }
        }
    }
    
    fun removeFriend(friendId: Int, backendId: String) {
        viewModelScope.launch {
            try {
                val backend = repository.backends.value.find { it.id == backendId }
                if (backend == null || !backend.isAuthenticated()) {
                    _uiState.value = _uiState.value.copy(error = "Backend not found or not authenticated")
                    return@launch
                }
                
                val apiService = retrofitManager.getApiService(backend)
                val authToken = "Bearer ${backend.authToken}"
                val response = apiService.removeFriend(friendId, authToken)
                
                _uiState.value = _uiState.value.copy(
                    successMessage = response.message
                )
                
                // Reload friends list
                loadFriends()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to remove friend: ${e.message}"
                )
            }
        }
    }
    
    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            error = null,
            successMessage = null
        )
    }
}
