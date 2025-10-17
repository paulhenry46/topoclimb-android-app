package com.example.topoclimb.ui.screens

import android.webkit.JavascriptInterface
import android.webkit.WebView
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
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
    val density = LocalDensity.current
    
    // Remember the map height once it's been measured
    var mapHeight by remember { mutableStateOf(0.dp) }
    
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
                    // SVG Map section - moved to top for better integration
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
                                    .then(
                                        if (mapHeight > 0.dp) {
                                            Modifier.heightIn(min = mapHeight)
                                        } else {
                                            Modifier
                                        }
                                    )
                                    .wrapContentHeight()
                                    .onGloballyPositioned { coordinates ->
                                        // Capture the actual height when first measured
                                        if (mapHeight == 0.dp) {
                                            with(density) {
                                                mapHeight = coordinates.size.height.toDp()
                                            }
                                        }
                                    },
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
                                        // Get the currently selected sector to restore after reload
                                        val selectedSectorId = uiState.selectedSectorId
                                        
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
                                                    // Store selected sector ID globally
                                                    var currentSelectedSectorId = ${if (selectedSectorId != null) "'sector_$selectedSectorId'" else "null"};
                                                    
                                                    document.addEventListener('DOMContentLoaded', function() {
                                                        const paths = document.querySelectorAll('svg path');
                                                        
                                                        // Restore selected state if exists
                                                        if (currentSelectedSectorId) {
                                                            const selectedPath = document.getElementById(currentSelectedSectorId);
                                                            if (selectedPath) {
                                                                selectedPath.classList.add('selected');
                                                            }
                                                        }
                                                        
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
                                                                    currentSelectedSectorId = path.id;
                                                                    // Notify Android app with sector ID from path's id attribute
                                                                    if (window.Android && window.Android.onSectorSelected && path.id) {
                                                                        window.Android.onSectorSelected(path.id);
                                                                    }
                                                                } else {
                                                                    // Deselected - show all routes
                                                                    currentSelectedSectorId = null;
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
                                                                    currentSelectedSectorId = this.id;
                                                                    // Notify Android app with sector ID from path's id attribute
                                                                    if (window.Android && window.Android.onSectorSelected && this.id) {
                                                                        window.Android.onSectorSelected(this.id);
                                                                    }
                                                                } else {
                                                                    // Deselected - show all routes
                                                                    currentSelectedSectorId = null;
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
                    
                    // Filter section
                    item {
                        FilterSection(
                            searchQuery = uiState.searchQuery,
                            minGrade = uiState.minGrade,
                            maxGrade = uiState.maxGrade,
                            showNewRoutesOnly = uiState.showNewRoutesOnly,
                            selectedSectorId = uiState.selectedSectorId,
                            sectors = uiState.sectors,
                            onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                            onMinGradeChange = { viewModel.updateMinGrade(it) },
                            onMaxGradeChange = { viewModel.updateMaxGrade(it) },
                            onNewRoutesToggle = { viewModel.toggleNewRoutesFilter(it) },
                            onSectorSelected = { viewModel.filterRoutesBySector(it) },
                            onClearFilters = { viewModel.clearFilters() }
                        )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterSection(
    searchQuery: String,
    minGrade: String?,
    maxGrade: String?,
    showNewRoutesOnly: Boolean,
    selectedSectorId: Int?,
    sectors: List<com.example.topoclimb.data.Sector>,
    onSearchQueryChange: (String) -> Unit,
    onMinGradeChange: (String?) -> Unit,
    onMaxGradeChange: (String?) -> Unit,
    onNewRoutesToggle: (Boolean) -> Unit,
    onSectorSelected: (Int?) -> Unit,
    onClearFilters: () -> Unit
) {
    var showFilters by remember { mutableStateOf(false) }
    val hasActiveFilters = searchQuery.isNotEmpty() || minGrade != null || maxGrade != null || showNewRoutesOnly
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Search bar with filter toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Search routes...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear search")
                            }
                        }
                    },
                    singleLine = true
                )
                
                // Filter toggle button
                IconButton(
                    onClick = { showFilters = !showFilters },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = if (showFilters || hasActiveFilters) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.surface
                    )
                ) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = if (showFilters) "Hide filters" else "Show filters",
                        tint = if (showFilters || hasActiveFilters) 
                            MaterialTheme.colorScheme.onPrimary 
                        else 
                            MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            
            // Filter options panel
            if (showFilters) {
                Spacer(modifier = Modifier.height(16.dp))
                
                // Grade filters
                Text(
                    text = "Grade Range",
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Min grade dropdown
                    GradeDropdown(
                        label = "Min Grade",
                        selectedGrade = minGrade,
                        onGradeSelected = onMinGradeChange,
                        modifier = Modifier.weight(1f)
                    )
                    
                    // Max grade dropdown
                    GradeDropdown(
                        label = "Max Grade",
                        selectedGrade = maxGrade,
                        onGradeSelected = onMaxGradeChange,
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // New routes filter
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Show only new routes (last week)",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Switch(
                        checked = showNewRoutesOnly,
                        onCheckedChange = onNewRoutesToggle
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Sector filter
                Text(
                    text = "Filter by Sector",
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                var expandedSectorDropdown by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expandedSectorDropdown,
                    onExpandedChange = { expandedSectorDropdown = it }
                ) {
                    OutlinedTextField(
                        value = sectors.find { it.id == selectedSectorId }?.name ?: "All sectors",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSectorDropdown) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedSectorDropdown,
                        onDismissRequest = { expandedSectorDropdown = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("All sectors") },
                            onClick = {
                                onSectorSelected(null)
                                expandedSectorDropdown = false
                            }
                        )
                        sectors.forEach { sector ->
                            DropdownMenuItem(
                                text = { Text(sector.name) },
                                onClick = {
                                    onSectorSelected(sector.id)
                                    expandedSectorDropdown = false
                                }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Clear filters button
                if (hasActiveFilters) {
                    Button(
                        onClick = onClearFilters,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Clear all filters")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GradeDropdown(
    label: String,
    selectedGrade: String?,
    onGradeSelected: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val grades = listOf(
        "3a", "3b", "3c",
        "4a", "4b", "4c",
        "5a", "5b", "5c",
        "6a", "6a+", "6b", "6b+", "6c", "6c+",
        "7a", "7a+", "7b", "7b+", "7c", "7c+",
        "8a", "8a+", "8b", "8b+", "8c", "8c+",
        "9a", "9a+", "9b", "9b+", "9c"
    )
    
    var expanded by remember { mutableStateOf(false) }
    
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedGrade ?: "Select",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("None") },
                onClick = {
                    onGradeSelected(null)
                    expanded = false
                }
            )
            grades.forEach { grade ->
                DropdownMenuItem(
                    text = { Text(grade) },
                    onClick = {
                        onGradeSelected(grade)
                        expanded = false
                    }
                )
            }
        }
    }
}

