package com.example.topoclimb.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.topoclimb.data.Route
import com.example.topoclimb.ui.components.SvgMapView
import com.example.topoclimb.viewmodel.AreaDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AreaDetailScreen(
    areaId: Int,
    onBackClick: () -> Unit,
    viewModel: AreaDetailViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(areaId) {
        viewModel.loadAreaDetails(areaId)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.area?.name ?: "Area Details") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                        Button(onClick = { viewModel.loadAreaDetails(areaId) }) {
                            Text("Retry")
                        }
                    }
                }
            }
            uiState.area != null -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Area info card
                    item {
                        uiState.area?.let { area ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = area.name,
                                        style = MaterialTheme.typography.headlineMedium
                                    )
                                    area.description?.let { description ->
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = description,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                    if (area.latitude != null && area.longitude != null) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "Location: ${area.latitude}, ${area.longitude}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    // SVG Map section
                    if (uiState.svgPaths.isNotEmpty()) {
                        item {
                            Text(
                                text = "Topo Map",
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                            if (uiState.selectedSectorId != null) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Sector ${uiState.selectedSectorId} selected - Tap to deselect",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            } else {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Tap on a sector to view its routes",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                SvgMapView(
                                    svgPaths = uiState.svgPaths,
                                    svgDimensions = uiState.svgDimensions,
                                    selectedSectorId = uiState.selectedSectorId,
                                    onPathTapped = { sectorId ->
                                        viewModel.onSectorTapped(sectorId)
                                    },
                                    modifier = Modifier
                                )
                            }
                        }
                    }
                    
                    // Routes section
                    if (uiState.routes.isNotEmpty()) {
                        item {
                            val routesTitle = if (uiState.selectedSectorId != null) {
                                "Routes in Sector ${uiState.selectedSectorId} (${uiState.routes.size})"
                            } else {
                                "Routes (${uiState.routes.size})"
                            }
                            Text(
                                text = routesTitle,
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                        items(uiState.routes) { route ->
                            RouteItem(route)
                        }
                    }
                    
                    // Empty routes state
                    if (uiState.routes.isEmpty()) {
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
                                        text = "No routes available for this area.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
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
fun RouteItem(route: Route) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = route.name,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                route.grade?.let { grade ->
                    Text(
                        text = grade,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            route.type?.let { type ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Type: $type",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            
            route.height?.let { height ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Height: ${height}m",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            
            route.description?.let { description ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
