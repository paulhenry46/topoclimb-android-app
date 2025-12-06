package com.example.topoclimb.viewmodel

import android.app.Application
import android.content.Context
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
import com.example.topoclimb.utils.CacheUtils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.joinAll
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
    val routeType: String?,
    val logType: String?,
    val logWay: String?,
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
    
    companion object {
        // Maximum counts for displaying items on the home screen
        private const val MAX_FRIENDS_FOR_LOGS = 5
        private const val MAX_LOGS_PER_FRIEND = 2
        private const val MAX_FRIEND_LOGS = 10
        private const val MAX_SITES_FOR_NEW_ROUTES = 3
        private const val MAX_ROUTES_PER_SITE = 5
        private const val MAX_NEW_ROUTES = 10
        
        // Cache keys
        private const val PREFS_NAME = "home_cache_prefs"
        private const val EVENTS_CACHE_KEY = "current_events_cache"
        private const val EVENTS_CACHE_TIME_KEY = "current_events_cache_time"
        private const val FRIEND_LOGS_CACHE_KEY = "friend_logs_cache"
        private const val FRIEND_LOGS_CACHE_TIME_KEY = "friend_logs_cache_time"
    }
    
    private val backendConfigRepository = BackendConfigRepository(application)
    private val federatedRepository = FederatedTopoClimbRepository(application)
    private val retrofitManager = MultiBackendRetrofitManager()
    
    private val sharedPreferences = application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    init {
        // Load from cache first (instant display)
        loadFromCache()
        
        viewModelScope.launch {
            backendConfigRepository.backends.collect { backends ->
                val defaultBackend = backendConfigRepository.getDefaultBackend()
                val isAuth = backends.any { it.isAuthenticated() }
                _uiState.value = _uiState.value.copy(
                    userName = defaultBackend?.user?.name,
                    isAuthenticated = isAuth
                )
                
                // Check if cache is stale and refresh from network
                val eventsCacheTime = sharedPreferences.getLong(EVENTS_CACHE_TIME_KEY, 0)
                val friendLogsCacheTime = sharedPreferences.getLong(FRIEND_LOGS_CACHE_TIME_KEY, 0)
                
                // Refresh events if cache is stale or empty
                if (CacheUtils.isCacheStale(eventsCacheTime) || _uiState.value.currentEvents.isEmpty()) {
                    loadCurrentEvents()
                }
                
                // Only load friend data if authenticated
                if (isAuth) {
                    // Refresh friend logs if cache is stale or empty
                    if (CacheUtils.isCacheStale(friendLogsCacheTime) || _uiState.value.friendLogs.isEmpty()) {
                        loadFriendLogs()
                    }
                }
            }
        }
    }
    
    /**
     * Load cached data from SharedPreferences for instant display
     */
    private fun loadFromCache() {
        loadCachedData(EVENTS_CACHE_KEY) { cachedEvents: List<CurrentEventWithSite> ->
            _uiState.value = _uiState.value.copy(currentEvents = cachedEvents)
        }
        
        loadCachedData(FRIEND_LOGS_CACHE_KEY) { cachedLogs: List<FriendLogWithDetails> ->
            _uiState.value = _uiState.value.copy(friendLogs = cachedLogs)
        }
    }
    
    /**
     * Generic function to load cached data from SharedPreferences
     */
    private inline fun <reified T> loadCachedData(cacheKey: String, crossinline onSuccess: (T) -> Unit) {
        val json = sharedPreferences.getString(cacheKey, null)
        if (json != null) {
            try {
                val type = object : TypeToken<T>() {}.type
                val cachedData: T = gson.fromJson(json, type)
                onSuccess(cachedData)
            } catch (e: Exception) {
                Log.e(TAG, "Error loading cached data for $cacheKey", e)
            }
        }
    }
    
    /**
     * Save current events to cache
     */
    private fun saveEventsToCache(events: List<CurrentEventWithSite>) {
        saveCacheData(EVENTS_CACHE_KEY, EVENTS_CACHE_TIME_KEY, events)
    }
    
    /**
     * Save friend logs to cache
     */
    private fun saveFriendLogsToCache(logs: List<FriendLogWithDetails>) {
        saveCacheData(FRIEND_LOGS_CACHE_KEY, FRIEND_LOGS_CACHE_TIME_KEY, logs)
    }
    
    /**
     * Generic function to save data to cache with timestamp
     */
    private fun <T> saveCacheData(dataKey: String, timeKey: String, data: T) {
        try {
            val json = gson.toJson(data)
            sharedPreferences.edit()
                .putString(dataKey, json)
                .putLong(timeKey, System.currentTimeMillis())
                .apply()
        } catch (e: Exception) {
            Log.e(TAG, "Error saving data to cache for $dataKey", e)
        }
    }
    
    fun loadAllData() {
        loadDataConcurrently(isRefresh = false)
    }
    
    fun refresh() {
        loadDataConcurrently(isRefresh = true)
    }
    
    /**
     * Loads all data concurrently
     * @param isRefresh Whether this is a refresh operation (affects loading state)
     */
    private fun loadDataConcurrently(isRefresh: Boolean) {
        viewModelScope.launch {
            _uiState.value = if (isRefresh) {
                _uiState.value.copy(isRefreshing = true, error = null)
            } else {
                _uiState.value.copy(isLoading = true, error = null)
            }
            
            // Load data concurrently - all jobs start immediately
            val jobs = mutableListOf(
                launch { loadCurrentEvents() },
                launch { loadNewRoutes() }
            )
            
            // Only load friend logs if authenticated
            if (_uiState.value.isAuthenticated) {
                jobs.add(launch { loadFriendLogs() })
            }
            
            jobs.joinAll()
            
            _uiState.value = if (isRefresh) {
                _uiState.value.copy(isRefreshing = false)
            } else {
                _uiState.value.copy(isLoading = false)
            }
        }
    }
    
    private suspend fun loadFriendLogs() {
        _uiState.value = _uiState.value.copy(isLoadingFriendLogs = true)
        
        try {
            val backends = backendConfigRepository.backends.value
            val allLogs = mutableListOf<FriendLogWithDetails>()
            
            // Fetch friends' logs from all authenticated backends
            // Note: This uses a sequential approach per backend due to API limitations
            backends.filter { it.isAuthenticated() }.forEach { backend ->
                try {
                    val apiService = retrofitManager.getApiService(backend)
                    val authToken = "Bearer ${backend.authToken}"
                    
                    // Get friends list first
                    val friendsResponse = apiService.getFriends(authToken)
                    
                    // For each friend, get their recent routes (limited to avoid too many calls)
                    friendsResponse.data.take(MAX_FRIENDS_FOR_LOGS).forEach { friend ->
                        try {
                            val routeLogsResponse = apiService.getUserRoutes(friend.id)
                            
                            // Take the most recent logs from each friend
                            routeLogsResponse.data.take(MAX_LOGS_PER_FRIEND).forEach { log ->
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
                                            routeType = route.type,
                                            logType = log.type,
                                            logWay = log.way,
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
            val sortedLogs = allLogs.sortedByDescending { it.logCreatedAt }.take(MAX_FRIEND_LOGS)
            
            // Save to cache
            saveFriendLogsToCache(sortedLogs)
            
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
            
            // Save to cache
            saveEventsToCache(allEvents)
            
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
                sites.take(MAX_SITES_FOR_NEW_ROUTES).forEach { federatedSite ->
                    val backend = backends.find { it.id == federatedSite.backend.backendId }
                    if (backend != null) {
                        try {
                            val apiService = retrofitManager.getApiService(backend)
                            val routesResponse = apiService.getRoutes(siteId = federatedSite.data.id)
                            
                            // Sort by created date and take the newest routes
                            val newRoutes = routesResponse.data
                                .sortedByDescending { it.createdAt }
                                .take(MAX_ROUTES_PER_SITE)
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
                    .take(MAX_NEW_ROUTES)
                
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
