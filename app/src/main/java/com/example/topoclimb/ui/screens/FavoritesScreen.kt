package com.example.topoclimb.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.topoclimb.data.Federated
import com.example.topoclimb.data.RouteWithMetadata
import com.example.topoclimb.data.Site
import com.example.topoclimb.ui.components.RouteCard
import com.example.topoclimb.viewmodel.FavoriteRoutesViewModel
import com.example.topoclimb.viewmodel.SitesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    onSiteClick: (String, Int) -> Unit,
    viewModel: SitesViewModel = viewModel(),
    favoriteRoutesViewModel: FavoriteRoutesViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val favoriteRoutesUiState by favoriteRoutesViewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    
    // State for route bottom sheet
    var showRouteBottomSheet by remember { mutableStateOf(false) }
    var selectedRoute by remember { mutableStateOf<RouteWithMetadata?>(null) }
    
    // Filter sites based on favorite flag
    val favoriteSites = uiState.sites.filter { it.data.id == uiState.favoriteSiteId }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Favorites") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Tab selector
            PrimaryTabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Sites") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Routes") }
                )
            }
            
            // Content based on selected tab
            when (selectedTab) {
                0 -> FavoriteSitesTab(
                    favoriteSites = favoriteSites,
                    isLoading = uiState.isLoading,
                    isRefreshing = uiState.isRefreshing,
                    error = uiState.error,
                    onSiteClick = onSiteClick,
                    onFavoriteClick = { viewModel.toggleFavorite(it) },
                    onRefresh = { viewModel.refreshSites() },
                    onRetry = { viewModel.loadSites() }
                )
                1 -> FavoriteRoutesTab(
                    favoriteRoutes = favoriteRoutesUiState.favoriteRoutes,
                    onRouteClick = { route ->
                        selectedRoute = route
                        showRouteBottomSheet = true
                    },
                    onRemoveFavorite = { favoriteRoutesViewModel.toggleFavorite(it) }
                )
            }
        }
    }
    
    // Bottom Sheet for Route Details
    if (showRouteBottomSheet && selectedRoute != null) {
        com.example.topoclimb.ui.components.RouteDetailBottomSheet(
            routeWithMetadata = selectedRoute!!,
            onDismiss = { showRouteBottomSheet = false },
            favoriteRoutesViewModel = favoriteRoutesViewModel
        )
    }
}

@Composable
private fun FavoriteSitesTab(
    favoriteSites: List<Federated<Site>>,
    isLoading: Boolean,
    isRefreshing: Boolean,
    error: String?,
    onSiteClick: (String, Int) -> Unit,
    onFavoriteClick: (Int) -> Unit,
    onRefresh: () -> Unit,
    onRetry: () -> Unit
) {
    when {
        isLoading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        error != null -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Error: $error",
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onRetry) {
                        Text("Retry")
                    }
                }
            }
        }
        else -> {
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = onRefresh,
                modifier = Modifier.fillMaxSize()
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (favoriteSites.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        modifier = Modifier.size(48.dp),
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = "No Favorite Site",
                                        style = MaterialTheme.typography.titleLarge,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = "No favorite site selected. Tap the star on a site card to set it as favorite.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                    } else {
                        items(favoriteSites) { federatedSite ->
                            SiteItem(
                                site = federatedSite.data,
                                backendName = federatedSite.backend.backendName,
                                onClick = { onSiteClick(federatedSite.backend.backendId, federatedSite.data.id) },
                                isFavorite = true,
                                onFavoriteClick = { onFavoriteClick(federatedSite.data.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FavoriteRoutesTab(
    favoriteRoutes: List<RouteWithMetadata>,
    onRouteClick: (RouteWithMetadata) -> Unit,
    onRemoveFavorite: (RouteWithMetadata) -> Unit
) {
    if (favoriteRoutes.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "No Favorite Routes",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Your favorite routes will appear here. Tap the star button next to the register button in the route overview tab to add routes to your favorites.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(favoriteRoutes) { routeWithMetadata ->
                RouteCard(
                    thumbnail = routeWithMetadata.thumbnail,
                    grade = routeWithMetadata.grade?.toString(),
                    color = routeWithMetadata.color,
                    name = routeWithMetadata.name,
                    localId = routeWithMetadata.lineLocalId ?: routeWithMetadata.sectorLocalId,
                    numberLogs = routeWithMetadata.numberLogs,
                    numberComments = routeWithMetadata.numberComments,
                    onClick = { onRouteClick(routeWithMetadata) }
                )
            }
        }
    }
}
