package com.example.topoclimb.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.topoclimb.R
import com.example.topoclimb.data.Federated
import com.example.topoclimb.data.Site
import com.example.topoclimb.viewmodel.SitesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SitesScreen(
    onSiteClick: (String, Int) -> Unit,
    viewModel: SitesViewModel = viewModel(),
    favoriteOnly: Boolean = false,
    onManageInstancesClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Filter sites based on favorite flag
    val displaySites = if (favoriteOnly) {
        uiState.sites.filter { it.data.id == uiState.favoriteSiteId }
    } else {
        uiState.sites
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (favoriteOnly) "Favorite Site" else "Climbing Sites") },
                actions = {
                    // Show offline indicator if in offline mode
                    if (uiState.isOfflineMode) {
                        IconButton(onClick = {
                            // Could show offline dialog here if needed
                        }) {
                            Icon(
                                imageVector = Icons.Default.CloudOff,
                                contentDescription = "Offline mode",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Error: ${uiState.error}",
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadSites() }) {
                            Text("Retry")
                        }
                    }
                }
            }
            else -> {
                PullToRefreshBox(
                    isRefreshing = uiState.isRefreshing,
                    onRefresh = { viewModel.refreshSites() },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (displaySites.isEmpty() && favoriteOnly) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(32.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "No favorite site selected. Tap the star on a site card to set it as favorite.",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        } else {
                            items(displaySites) { federatedSite ->
                                SiteItem(
                                    site = federatedSite.data,
                                    backendName = federatedSite.backend.backendName,
                                    onClick = { onSiteClick(federatedSite.backend.backendId, federatedSite.data.id) },
                                    isFavorite = federatedSite.data.id == uiState.favoriteSiteId,
                                    onFavoriteClick = { viewModel.toggleFavorite(federatedSite.data.id) },
                                    isOfflineEnabled = uiState.offlineSites.contains(federatedSite.data.id),
                                    onOfflineToggle = {
                                        viewModel.toggleOfflineMode(
                                            federatedSite.data.id,
                                            federatedSite.backend.backendId,
                                            federatedSite.backend.backendName
                                        )
                                    }
                                )
                            }
                            
                            // Add "Site not found" card only for all sites page (not favorites)
                            if (!favoriteOnly) {
                                item {
                                    SiteNotFoundCard(onManageInstancesClick = onManageInstancesClick)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SiteItem(
    site: Site,
    backendName: String,
    onClick: () -> Unit,
    isFavorite: Boolean = false,
    onFavoriteClick: () -> Unit = {},
    isOfflineEnabled: Boolean = false,
    onOfflineToggle: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Banner background image
            site.banner?.let { bannerUrl ->
                AsyncImage(
                    model = bannerUrl,
                    contentDescription = "Site banner",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            
            // Overlay gradient for better text visibility
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
            ) {}
            
            // Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Logo and action buttons row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    // Logo
                    site.profilePicture?.let { logoUrl ->
                        AsyncImage(
                            model = logoUrl,
                            contentDescription = "Site logo",
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }
                    
                    // Action buttons (offline and favorite)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Offline mode toggle button
                        IconButton(
                            onClick = onOfflineToggle,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = if (isOfflineEnabled) {
                                    Icons.Filled.CloudDone
                                } else {
                                    Icons.Filled.CloudDownload
                                },
                                contentDescription = if (isOfflineEnabled) "Disable offline mode" else "Enable offline mode",
                                tint = if (isOfflineEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        
                        // Favorite star button
                        IconButton(
                            onClick = onFavoriteClick,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.Star,
                                contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                                tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
                
                // Site info at the bottom
                Column {
                    Text(
                        text = site.name,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    site.description?.let { description ->
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 2
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Source: $backendName",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun SiteNotFoundCard(onManageInstancesClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clickable(onClick = onManageInstancesClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Drawing takes full width and most of the height
            Icon(
                painter = painterResource(id = R.drawable.bino_montain),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Text and chevron row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "The site you're looking for is not here?",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "You can find new sites by adding a new Topoclimb instance.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Chevron icon vertically centered between title and text
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Go to instance manager",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}
