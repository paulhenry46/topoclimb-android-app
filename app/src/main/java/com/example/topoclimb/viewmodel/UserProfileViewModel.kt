package com.example.topoclimb.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.topoclimb.data.AddFriendRequest
import com.example.topoclimb.data.GradingSystem
import com.example.topoclimb.data.Route
import com.example.topoclimb.data.UserProfile
import com.example.topoclimb.data.UserRouteLog
import com.example.topoclimb.network.MultiBackendRetrofitManager
import com.example.topoclimb.repository.BackendConfigRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val TAG = "UserProfileViewModel"

/**
 * Data class representing a route log with its associated route details and grading system
 */
data class RouteLogWithDetails(
    val log: UserRouteLog,
    val route: Route?,
    val gradingSystem: GradingSystem? = null
)

data class UserProfileUiState(
    val userProfile: UserProfile? = null,
    val routeLogs: List<RouteLogWithDetails> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingRoutes: Boolean = false,
    val error: String? = null,
    val routesError: String? = null,
    val isFriend: Boolean = false,
    val isCheckingFriendship: Boolean = false,
    val isAddingFriend: Boolean = false,
    val isRemovingFriend: Boolean = false,
    val friendActionMessage: String? = null,
    val friendActionError: String? = null
)

class UserProfileViewModel(
    application: Application
) : AndroidViewModel(application) {
    
    private val repository = BackendConfigRepository(application)
    private val retrofitManager = MultiBackendRetrofitManager()
    
    private val _uiState = MutableStateFlow(UserProfileUiState())
    val uiState: StateFlow<UserProfileUiState> = _uiState.asStateFlow()
    
    private var currentUserId: Int? = null
    private var currentBackendId: String? = null
    
    /**
     * Load user profile and their routes
     */
    fun loadUserProfile(userId: Int, backendId: String) {
        currentUserId = userId
        currentBackendId = backendId
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null,
                userProfile = null,
                routeLogs = emptyList()
            )
            
            try {
                val backend = repository.getBackend(backendId)
                if (backend == null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Backend not found"
                    )
                    return@launch
                }
                
                val apiService = retrofitManager.getApiService(backend)
                
                // Fetch user profile
                val profileResponse = apiService.getUserProfile(userId)
                
                _uiState.value = _uiState.value.copy(
                    userProfile = profileResponse.data,
                    isLoading = false
                )
                
                // Check if user is a friend
                checkFriendship(userId, backendId)
                
                // Fetch user's routes
                loadUserRoutes(userId, backendId)
                
            } catch (e: Exception) {
                Log.e(TAG, "Error loading user profile", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load user profile: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Load user's route logs with route details and grading systems
     */
    private fun loadUserRoutes(userId: Int, backendId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingRoutes = true, routesError = null)
            
            try {
                val backend = repository.getBackend(backendId)
                if (backend == null) {
                    _uiState.value = _uiState.value.copy(
                        isLoadingRoutes = false,
                        routesError = "Backend not found"
                    )
                    return@launch
                }
                
                val apiService = retrofitManager.getApiService(backend)
                
                // Fetch user's routes
                val routeLogsResponse = apiService.getUserRoutes(userId)
                val routeLogs = routeLogsResponse.data
                
                // Cache for grading systems by site ID to avoid redundant API calls
                val gradingSystemCache = mutableMapOf<Int, GradingSystem?>()
                
                // Note: Fetching route details sequentially is not optimal for large lists.
                // A future optimization could batch requests or use an API endpoint that returns
                // route details alongside logs. For now, we limit to recent logs and handle
                // failures gracefully.
                val routeLogsWithDetails = routeLogs.take(20).map { log ->
                    try {
                        val routeResponse = apiService.getRoute(log.routeId)
                        val route = routeResponse.data
                        
                        // Get grading system for this route's site (cached if already fetched)
                        val gradingSystem = route.siteId.let { siteId ->
                            if (gradingSystemCache.containsKey(siteId)) {
                                gradingSystemCache[siteId]
                            } else {
                                try {
                                    val siteResponse = apiService.getSite(siteId)
                                    val gs = siteResponse.data.gradingSystem
                                    gradingSystemCache[siteId] = gs
                                    gs
                                } catch (e: Exception) {
                                    Log.e(TAG, "Error fetching site $siteId grading system", e)
                                    gradingSystemCache[siteId] = null
                                    null
                                }
                            }
                        }
                        
                        RouteLogWithDetails(log = log, route = route, gradingSystem = gradingSystem)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error fetching route ${log.routeId}", e)
                        RouteLogWithDetails(log = log, route = null, gradingSystem = null)
                    }
                }
                
                _uiState.value = _uiState.value.copy(
                    routeLogs = routeLogsWithDetails,
                    isLoadingRoutes = false
                )
                
            } catch (e: Exception) {
                Log.e(TAG, "Error loading user routes", e)
                _uiState.value = _uiState.value.copy(
                    isLoadingRoutes = false,
                    routesError = "Failed to load routes: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Check if the user is already a friend
     */
    private fun checkFriendship(userId: Int, backendId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCheckingFriendship = true)
            
            try {
                val backend = repository.getBackend(backendId)
                if (backend == null || !backend.isAuthenticated()) {
                    _uiState.value = _uiState.value.copy(
                        isCheckingFriendship = false,
                        isFriend = false
                    )
                    return@launch
                }
                
                val apiService = retrofitManager.getApiService(backend)
                val authToken = "Bearer ${backend.authToken}"
                
                // Fetch friends list and check if user is in it
                val friendsResponse = apiService.getFriends(authToken)
                val isFriend = friendsResponse.data.any { it.id == userId }
                
                _uiState.value = _uiState.value.copy(
                    isCheckingFriendship = false,
                    isFriend = isFriend
                )
                
            } catch (e: Exception) {
                Log.e(TAG, "Error checking friendship", e)
                _uiState.value = _uiState.value.copy(
                    isCheckingFriendship = false,
                    isFriend = false
                )
            }
        }
    }
    
    /**
     * Add user as friend
     */
    fun addFriend() {
        val userId = currentUserId ?: return
        val backendId = currentBackendId ?: return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isAddingFriend = true,
                friendActionMessage = null,
                friendActionError = null
            )
            
            try {
                val backend = repository.getBackend(backendId)
                if (backend == null || !backend.isAuthenticated()) {
                    _uiState.value = _uiState.value.copy(
                        isAddingFriend = false,
                        friendActionError = "Not authenticated"
                    )
                    return@launch
                }
                
                val apiService = retrofitManager.getApiService(backend)
                val authToken = "Bearer ${backend.authToken}"
                val request = AddFriendRequest(friendId = userId)
                
                val response = apiService.addFriend(request, authToken)
                
                _uiState.value = _uiState.value.copy(
                    isAddingFriend = false,
                    isFriend = true,
                    friendActionMessage = response.message
                )
                
            } catch (e: Exception) {
                Log.e(TAG, "Error adding friend", e)
                _uiState.value = _uiState.value.copy(
                    isAddingFriend = false,
                    friendActionError = "Failed to add friend: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Remove user from friends
     */
    fun removeFriend() {
        val userId = currentUserId ?: return
        val backendId = currentBackendId ?: return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isRemovingFriend = true,
                friendActionMessage = null,
                friendActionError = null
            )
            
            try {
                val backend = repository.getBackend(backendId)
                if (backend == null || !backend.isAuthenticated()) {
                    _uiState.value = _uiState.value.copy(
                        isRemovingFriend = false,
                        friendActionError = "Not authenticated"
                    )
                    return@launch
                }
                
                val apiService = retrofitManager.getApiService(backend)
                val authToken = "Bearer ${backend.authToken}"
                
                val response = apiService.removeFriend(userId, authToken)
                
                _uiState.value = _uiState.value.copy(
                    isRemovingFriend = false,
                    isFriend = false,
                    friendActionMessage = response.message
                )
                
            } catch (e: Exception) {
                Log.e(TAG, "Error removing friend", e)
                _uiState.value = _uiState.value.copy(
                    isRemovingFriend = false,
                    friendActionError = "Failed to remove friend: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Check if the current user is authenticated
     */
    fun isAuthenticated(backendId: String): Boolean {
        val backend = repository.getBackend(backendId)
        return backend?.isAuthenticated() == true
    }
    
    /**
     * Check if the profile being viewed is the current user's own profile
     */
    fun isOwnProfile(backendId: String): Boolean {
        val backend = repository.getBackend(backendId)
        return backend?.user?.id == currentUserId
    }
    
    /**
     * Clear action messages
     */
    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            friendActionMessage = null,
            friendActionError = null
        )
    }
    
    /**
     * Refresh the profile data
     */
    fun refresh() {
        val userId = currentUserId ?: return
        val backendId = currentBackendId ?: return
        loadUserProfile(userId, backendId)
    }
}
