package com.example.topoclimb.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.topoclimb.data.Contest
import com.example.topoclimb.data.Federated
import com.example.topoclimb.data.Route
import com.example.topoclimb.data.RouteWithMetadata
import com.example.topoclimb.data.Site
import com.example.topoclimb.network.MultiBackendRetrofitManager
import com.example.topoclimb.repository.BackendConfigRepository
import com.example.topoclimb.repository.FederatedTopoClimbRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val TAG = "HomeViewModel"

/**
 * Represents a friend's log with additional context
 */
data class FriendLogWithDetails(
    val friendId: Int,
    val friendName: String,
    val friendPhotoUrl: String?,
    val routeId: Int,
    val routeName: String?,
    val routeGrade: Int?,
    val routeColor: String?,
    val routeThumbnail: String?,
    val logType: String?,
    val logCreatedAt: String?,
    val instanceName: String,
    val backendId: String,
    val siteId: Int? = null,
    val siteName: String? = null
)

/**
 * Represents a current event (contest) with site information
 */
data class CurrentEventWithSite(
    val contest: Contest,
    val siteName: String?,
    val siteId: Int?,
    val backendId: String
)

data class HomeUiState(
    val userName: String? = null,
    val isAuthenticated: Boolean = false,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    
    // News section
    val friendLogs: List<FriendLogWithDetails> = emptyList(),
    val currentEvents: List<CurrentEventWithSite> = emptyList(),
    val isLoadingFriendLogs: Boolean = false,
    val isLoadingEvents: Boolean = false,
    
    // Climb section
    val favoriteSites: List<Federated<Site>> = emptyList(),
    val favoriteRoutes: List<RouteWithMetadata> = emptyList(),
    val newRoutes: List<RouteWithMetadata> = emptyList(),
    val isLoadingNewRoutes: Boolean = false
)

class HomeViewModel(
    application: Application
) : AndroidViewModel(application) {
    
    private val backendConfigRepository = BackendConfigRepository(application)
    private val federatedRepository = FederatedTopoClimbRepository(application)
    private val retrofitManager = MultiBackendRetrofitManager()
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    init {
        viewModelScope.launch {
            backendConfigRepository.backends.collect { backends ->
                val defaultBackend = backendConfigRepository.getDefaultBackend()
                val isAuth = backends.any { it.isAuthenticated() }
                _uiState.value = _uiState.value.copy(
                    userName = defaultBackend?.user?.name,
                    isAuthenticated = isAuth
                )
                if (isAuth) {
                    loadAllData()
                }
            }
        }
    }
    
    fun loadAllData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            // Load data concurrently using async
            val friendLogsJob = viewModelScope.launch { loadFriendLogs() }
            val eventsJob = viewModelScope.launch { loadCurrentEvents() }
            val newRoutesJob = viewModelScope.launch { loadNewRoutes() }
            
            // Wait for all to complete
            friendLogsJob.join()
            eventsJob.join()
            newRoutesJob.join()
            
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }
    
    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true, error = null)
            
            // Load data concurrently using async
            val friendLogsJob = viewModelScope.launch { loadFriendLogs() }
            val eventsJob = viewModelScope.launch { loadCurrentEvents() }
            val newRoutesJob = viewModelScope.launch { loadNewRoutes() }
            
            // Wait for all to complete
            friendLogsJob.join()
            eventsJob.join()
            newRoutesJob.join()
            
            _uiState.value = _uiState.value.copy(isRefreshing = false)
        }
    }
    
    private suspend fun loadFriendLogs() {
        _uiState.value = _uiState.value.copy(isLoadingFriendLogs = true)
        
        try {
            val backends = backendConfigRepository.backends.value
            val allLogs = mutableListOf<FriendLogWithDetails>()
            
            // Fetch friends' logs from all authenticated backends
            backends.filter { it.isAuthenticated() }.forEach { backend ->
                try {
                    val apiService = retrofitManager.getApiService(backend)
                    val authToken = "Bearer ${backend.authToken}"
                    
                    // Get friends list first
                    val friendsResponse = apiService.getFriends(authToken)
                    
                    // For each friend, get their recent routes (limited to avoid too many calls)
                    friendsResponse.data.take(5).forEach { friend ->
                        try {
                            val routeLogsResponse = apiService.getUserRoutes(friend.id)
                            
                            // Take the most recent logs from each friend
                            routeLogsResponse.data.take(2).forEach { log ->
                                try {
                                    val routeResponse = apiService.getRoute(log.routeId)
                                    val route = routeResponse.data
                                    
                                    allLogs.add(
                                        FriendLogWithDetails(
                                            friendId = friend.id,
                                            friendName = friend.name,
                                            friendPhotoUrl = friend.profilePhotoUrl,
                                            routeId = log.routeId,
                                            routeName = route.name,
                                            routeGrade = route.grade,
                                            routeColor = route.color,
                                            routeThumbnail = route.thumbnail,
                                            logType = log.type,
                                            logCreatedAt = log.createdAt,
                                            instanceName = backend.name,
                                            backendId = backend.id,
                                            siteId = route.siteId,
                                            siteName = route.siteName
                                        )
                                    )
                                } catch (e: Exception) {
                                    Log.e(TAG, "Error fetching route ${log.routeId}", e)
                                }
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error fetching routes for friend ${friend.id}", e)
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error loading friend logs from ${backend.name}", e)
                }
            }
            
            // Sort by created date and take the most recent 10
            val sortedLogs = allLogs.sortedByDescending { it.logCreatedAt }.take(10)
            
            _uiState.value = _uiState.value.copy(
                friendLogs = sortedLogs,
                isLoadingFriendLogs = false
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error loading friend logs", e)
            _uiState.value = _uiState.value.copy(
                isLoadingFriendLogs = false,
                error = "Failed to load friend activity"
            )
        }
    }
    
    private suspend fun loadCurrentEvents() {
        _uiState.value = _uiState.value.copy(isLoadingEvents = true)
        
        try {
            val backends = backendConfigRepository.backends.value
            val allEvents = mutableListOf<CurrentEventWithSite>()
            
            // Fetch current events from all enabled backends
            backends.filter { it.enabled }.forEach { backend ->
                try {
                    val apiService = retrofitManager.getApiService(backend)
                    val eventsResponse = apiService.getCurrentEvents()
                    
                    eventsResponse.data.forEach { contest ->
                        // Fetch site name for this contest
                        val siteName = contest.siteId?.let { siteId ->
                            try {
                                val siteResponse = apiService.getSite(siteId)
                                siteResponse.data.name
                            } catch (e: Exception) {
                                Log.e(TAG, "Error fetching site $siteId", e)
                                null
                            }
                        }
                        
                        allEvents.add(
                            CurrentEventWithSite(
                                contest = contest,
                                siteName = siteName,
                                siteId = contest.siteId,
                                backendId = backend.id
                            )
                        )
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error loading events from ${backend.name}", e)
                }
            }
            
            _uiState.value = _uiState.value.copy(
                currentEvents = allEvents,
                isLoadingEvents = false
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error loading current events", e)
            _uiState.value = _uiState.value.copy(
                isLoadingEvents = false
            )
        }
    }
    
    private suspend fun loadNewRoutes() {
        _uiState.value = _uiState.value.copy(isLoadingNewRoutes = true)
        
        try {
            // Get all sites and then fetch recent routes from favorite sites
            val sitesResult = federatedRepository.getSites()
            
            sitesResult.onSuccess { sites ->
                val allNewRoutes = mutableListOf<RouteWithMetadata>()
                val backends = backendConfigRepository.backends.value
                
                // Get routes from the first few sites (to show recent routes)
                sites.take(3).forEach { federatedSite ->
                    val backend = backends.find { it.id == federatedSite.backend.backendId }
                    if (backend != null) {
                        try {
                            val apiService = retrofitManager.getApiService(backend)
                            val routesResponse = apiService.getRoutes(siteId = federatedSite.data.id)
                            
                            // Sort by created date and take the newest routes
                            val newRoutes = routesResponse.data
                                .sortedByDescending { it.createdAt }
                                .take(5)
                                .map { route ->
                                    RouteWithMetadata(
                                        route = route,
                                        lineLocalId = null,
                                        sectorLocalId = null,
                                        lineCount = null
                                    )
                                }
                            
                            allNewRoutes.addAll(newRoutes)
                        } catch (e: Exception) {
                            Log.e(TAG, "Error loading routes from site ${federatedSite.data.name}", e)
                        }
                    }
                }
                
                // Sort all routes by created date and take the most recent
                val sortedNewRoutes = allNewRoutes
                    .distinctBy { it.id }
                    .sortedByDescending { it.createdAt }
                    .take(10)
                
                _uiState.value = _uiState.value.copy(
                    newRoutes = sortedNewRoutes,
                    isLoadingNewRoutes = false
                )
            }
            
            sitesResult.onFailure { exception ->
                Log.e(TAG, "Error getting sites for new routes", exception)
                _uiState.value = _uiState.value.copy(
                    isLoadingNewRoutes = false
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading new routes", e)
            _uiState.value = _uiState.value.copy(
                isLoadingNewRoutes = false
            )
        }
    }
    
    /**
     * Update favorite sites from the SitesViewModel
     */
    fun updateFavoriteSites(sites: List<Federated<Site>>) {
        _uiState.value = _uiState.value.copy(favoriteSites = sites.take(3))
    }
    
    /**
     * Update favorite routes from the FavoriteRoutesViewModel
     */
    fun updateFavoriteRoutes(routes: List<RouteWithMetadata>) {
        _uiState.value = _uiState.value.copy(favoriteRoutes = routes.take(3))
    }
}
