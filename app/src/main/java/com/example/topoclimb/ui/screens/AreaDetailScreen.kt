package com.example.topoclimb.ui.screens

import android.webkit.JavascriptInterface
import android.webkit.WebView
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.topoclimb.ui.components.RouteCard
import com.example.topoclimb.viewmodel.AreaDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AreaDetailScreen(
    areaId: Int,
    onBackClick: () -> Unit,
    viewModel: AreaDetailViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
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
                    uiState.svgMapContent?.let { svgMap ->
                        item {
                            Text(
                                text = "Topo Map",
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .wrapContentHeight(),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                AndroidView(
                                    factory = { context ->
                                        WebView(context).apply {
                                            settings.javaScriptEnabled = true
                                            settings.loadWithOverviewMode = true
                                            settings.useWideViewPort = true
                                            settings.builtInZoomControls = true
                                            settings.displayZoomControls = false
                                            setBackgroundColor(android.graphics.Color.TRANSPARENT)
                                            
                                            // Add JavaScript interface for communication
                                            addJavascriptInterface(object {
                                                @JavascriptInterface
                                                fun onSectorSelected(sectorIdStr: String) {
                                                    // The SVG path's id is in format "sector_X" where X is the sector ID
                                                    if (sectorIdStr.isEmpty()) {
                                                        viewModel.filterRoutesBySector(null)
                                                    } else {
                                                        // Extract the numeric ID from "sector_X" format
                                                        val sectorId = sectorIdStr.removePrefix("sector_").toIntOrNull()
                                                        viewModel.filterRoutesBySector(sectorId)
                                                    }
                                                }
                                            }, "Android")
                                        }
                                    },
                                    update = { webView ->
                                        val htmlContent = """
                                            <!DOCTYPE html>
                                            <html>
                                            <head>
                                                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                                                <style>
                                                    body {
                                                        margin: 1em;
                                                        padding: 0;
                                                        display: flex;
                                                        justify-content: center;
                                                        align-items: flex-start;
                                                        background: transparent;
                                                        /* Remove tap highlight */
                                                        -webkit-tap-highlight-color: transparent;
                                                        -webkit-touch-callout: none;
                                                        -webkit-user-select: none;
                                                        user-select: none;
                                                    }
                                                    svg {
                                                        max-width: 100%;
                                                        height: auto;
                                                        background: transparent;
                                                        display: block;
                                                    }
                                                    svg path {
                                                        stroke: black;
                                                        fill: none;
                                                        cursor: pointer;
                                                        /* Increased stroke-width for better visibility */
                                                        stroke-width: 25;
                                                        /* Add invisible wider stroke for easier clicking */
                                                        paint-order: stroke;
                                                    }
                                                    svg path:hover {
                                                        stroke: #666;
                                                    }
                                                    svg path.selected {
                                                        stroke: red;
                                                        stroke-width: 30;
                                                    }
                                                </style>
                                                <script>
                                                    document.addEventListener('DOMContentLoaded', function() {
                                                        const paths = document.querySelectorAll('svg path');
                                                        
                                                        // SVG paths should have id attributes that are sector IDs
                                                        paths.forEach(function(path) {
                                                            // Create an invisible wider path for better tap tolerance
                                                            const hitArea = path.cloneNode(true);
                                                            hitArea.setAttribute('stroke', 'transparent');
                                                            hitArea.setAttribute('stroke-width', '45');
                                                            hitArea.setAttribute('fill', 'none');
                                                            hitArea.setAttribute('pointer-events', 'stroke');
                                                            hitArea.removeAttribute('id');
                                                            hitArea.removeAttribute('class');
                                                            
                                                            // Insert the hit area before the visible path
                                                            path.parentNode.insertBefore(hitArea, path);
                                                            
                                                            // Add click handler to the hit area
                                                            hitArea.addEventListener('click', function(e) {
                                                                // Prevent default behavior
                                                                e.preventDefault();
                                                                
                                                                const wasSelected = path.classList.contains('selected');
                                                                
                                                                // Remove selected class from all paths
                                                                paths.forEach(function(p) {
                                                                    p.classList.remove('selected');
                                                                });
                                                                
                                                                if (!wasSelected) {
                                                                    // Add selected class to clicked path
                                                                    path.classList.add('selected');
                                                                    // Notify Android app with sector ID from path's id attribute
                                                                    if (window.Android && window.Android.onSectorSelected && path.id) {
                                                                        window.Android.onSectorSelected(path.id);
                                                                    }
                                                                } else {
                                                                    // Deselected - show all routes
                                                                    if (window.Android && window.Android.onSectorSelected) {
                                                                        window.Android.onSectorSelected('');
                                                                    }
                                                                }
                                                            });
                                                            
                                                            // Also keep the click handler on the visible path itself
                                                            path.addEventListener('click', function(e) {
                                                                // Prevent default behavior
                                                                e.preventDefault();
                                                                
                                                                const wasSelected = this.classList.contains('selected');
                                                                
                                                                // Remove selected class from all paths
                                                                paths.forEach(function(p) {
                                                                    p.classList.remove('selected');
                                                                });
                                                                
                                                                if (!wasSelected) {
                                                                    // Add selected class to clicked path
                                                                    this.classList.add('selected');
                                                                    // Notify Android app with sector ID from path's id attribute
                                                                    if (window.Android && window.Android.onSectorSelected && this.id) {
                                                                        window.Android.onSectorSelected(this.id);
                                                                    }
                                                                } else {
                                                                    // Deselected - show all routes
                                                                    if (window.Android && window.Android.onSectorSelected) {
                                                                        window.Android.onSectorSelected('');
                                                                    }
                                                                }
                                                            });
                                                        });
                                                    });
                                                </script>
                                            </head>
                                            <body>
                                                $svgMap
                                            </body>
                                            </html>
                                        """.trimIndent()
                                        webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .wrapContentHeight()
                                )
                            }
                        }
                    }
                    
                    // Routes section
                    if (uiState.routes.isNotEmpty()) {
                        item {
                            Text(
                                text = if (uiState.selectedSectorId == null) {
                                    "Routes (${uiState.routes.size})"
                                } else {
                                    "Routes for selected sector (${uiState.routes.size})"
                                },
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                        items(uiState.routesWithMetadata) { routeWithMetadata ->
                            // Calculate local ID display with prefix
                            val localId = if (routeWithMetadata.lineCount == 1) {
                                routeWithMetadata.sectorLocalId?.let { "Sector n°$it" }
                            } else {
                                routeWithMetadata.lineLocalId?.let { "Line n°$it" }
                            }
                            
                            RouteCard(
                                thumbnail = routeWithMetadata.thumbnail,
                                grade = routeWithMetadata.grade,
                                color = routeWithMetadata.color,
                                name = routeWithMetadata.name,
                                localId = localId
                            )
                        }
                    }
                    
                    // Empty routes state
                    if (uiState.routes.isEmpty() && uiState.selectedSectorId != null) {
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
                                        text = "No routes available for the selected sector.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    } else if (uiState.routes.isEmpty() && uiState.selectedSectorId == null) {
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

