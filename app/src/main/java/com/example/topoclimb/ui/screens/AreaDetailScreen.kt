package com.example.topoclimb.ui.screens

import android.annotation.SuppressLint
import android.webkit.JavascriptInterface
import android.webkit.WebView
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.topoclimb.data.AreaType
import com.example.topoclimb.ui.components.RouteCard
import com.example.topoclimb.ui.components.SchemaView
import com.example.topoclimb.ui.state.ClimbedFilter
import com.example.topoclimb.ui.state.GroupingOption
import com.example.topoclimb.ui.state.ViewMode
import com.example.topoclimb.utils.GradeUtils
import com.example.topoclimb.viewmodel.AreaDetailViewModel

// Constants for grouping labels
private const val UNKNOWN_GRADE_LABEL = "Unknown"
private const val UNKNOWN_SECTOR_LABEL = "Unknown Sector"

@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AreaDetailScreen(
    backendId: String,
    siteId: Int,
    areaId: Int,
    onBackClick: () -> Unit,
    onStartLogging: ((routeId: Int, routeName: String, routeGrade: Int?, areaType: String?) -> Unit)? = null,
    viewModel: AreaDetailViewModel = viewModel(),
    favoriteRoutesViewModel: com.example.topoclimb.viewmodel.FavoriteRoutesViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val favoriteRoutesUiState by favoriteRoutesViewModel.uiState.collectAsState()
    val context = LocalContext.current
    val density = LocalDensity.current
    val primaryColorHex = String.format("#%06X", 0xFFFFFF and MaterialTheme.colorScheme.primary.toArgb())
    
    // Get shared logged routes state
    val loggedRouteIds by com.example.topoclimb.viewmodel.RouteDetailViewModel.sharedLoggedRouteIds.collectAsState()
    
    // Get shared route to show state
    val routeToShowId by com.example.topoclimb.viewmodel.RouteDetailViewModel.routeToShow.collectAsState()
    
    // Update favorite route IDs in viewModel when they change
    LaunchedEffect(favoriteRoutesUiState.favoriteRoutes) {
        val favoriteIds = favoriteRoutesUiState.favoriteRoutes.map { it.id }.toSet()
        viewModel.setFavoriteRouteIds(favoriteIds)
    }
    
    // Remember the map height once it's been measured
    var mapHeight by remember { mutableStateOf(0.dp) }
    
    // State for bottom sheet
    var showBottomSheet by remember { mutableStateOf(false) }
    var selectedRouteWithMetadata by remember { mutableStateOf<com.example.topoclimb.data.RouteWithMetadata?>(null) }
    
    // Schema cache - scoped to this screen only, cleared when leaving
    val schemaCache = remember { mutableMapOf<Int, com.example.topoclimb.ui.components.SchemaData>() }
    
    // Clear cache when leaving the screen
    DisposableEffect(Unit) {
        onDispose {
            schemaCache.clear()
        }
    }
    
    // Handle route to show after logging workflow
    LaunchedEffect(routeToShowId, uiState.routesWithMetadata) {
        routeToShowId?.let { routeId ->
            // Find the route in the list
            val route = uiState.routesWithMetadata.find { it.id == routeId }
            if (route != null) {
                selectedRouteWithMetadata = route
                showBottomSheet = true
                // Clear the route to show state
                com.example.topoclimb.viewmodel.RouteDetailViewModel.setRouteToShow(null)
            }
        }
    }
    
    // TODO: Update AreaDetailViewModel to use backendId for federated data
    LaunchedEffect(backendId, siteId, areaId) {
        viewModel.loadAreaDetails(backendId, siteId, areaId)
    }
    
    // State for filter modal
    var showFilterModal by remember { mutableStateOf(false) }
    
    // Check if there are active filters (excluding search query)
    val hasActiveFilters = uiState.minGrade != null || uiState.maxGrade != null || 
        uiState.showNewRoutesOnly || uiState.climbedFilter != com.example.topoclimb.ui.state.ClimbedFilter.ALL || 
        uiState.showFavoritesOnly
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.area?.name ?: "Area Details") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Filter list icon with indicator
                    Box {
                        IconButton(onClick = { showFilterModal = true }) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = "Filters",
                                tint = if (hasActiveFilters)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurface
                            )
                        }
                        if (hasActiveFilters) {
                            Badge(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = (-8).dp, y = 8.dp)
                            )
                        }
                    }
                    
                    // Show view mode toggle for trad areas (regardless of schema availability)
                    if (uiState.area?.type == AreaType.TRAD) {
                        IconButton(onClick = { viewModel.toggleViewMode() }) {
                            Icon(
                                imageVector = if (uiState.viewMode == ViewMode.SCHEMA) 
                                    Icons.Outlined.Map 
                                else 
                                    Icons.Filled.Map,
                                contentDescription = if (uiState.viewMode == ViewMode.SCHEMA)
                                    "Switch to Map view"
                                else
                                    "Switch to Schema view",
                                tint = if (uiState.viewMode == ViewMode.SCHEMA)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurface
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
                        Button(onClick = { viewModel.loadAreaDetails(backendId, siteId, areaId) }) {
                            Text("Retry")
                        }
                    }
                }
            }
            uiState.area != null -> {
                PullToRefreshBox(
                    isRefreshing = uiState.isRefreshing,
                    onRefresh = { viewModel.refreshAreaDetails() },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                    // Map or Schema section based on view mode
                    if (uiState.viewMode == ViewMode.SCHEMA) {
                        // Schema view mode
                        item {
                            Text(
                                text = "Sector Schema",
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                        
                        // Show schema error if present
                        uiState.schemaError?.let { errorMessage ->
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.errorContainer
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = "Schema Loading Error",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                        Text(
                                            text = errorMessage,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                        if (uiState.allSchemas.isNotEmpty()) {
                                            Text(
                                                text = "All schemas: ${uiState.allSchemas.joinToString(", ") { it.name }}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onErrorContainer
                                            )
                                        }
                                        Button(
                                            onClick = { viewModel.toggleViewMode() },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.error
                                            )
                                        ) {
                                            Text("Switch to Map View")
                                        }
                                    }
                                }
                            }
                        }
                        
                        item {
                            // Check if there's a schema for the selected sector
                            val currentSchema = if (uiState.selectedSectorId != null) {
                                // User has selected a specific sector, try to find its schema
                                uiState.schemas.find { it.id == uiState.selectedSectorId }
                            } else {
                                // No specific sector selected, use current schema index
                                uiState.schemas.getOrNull(uiState.currentSchemaIndex)
                            }
                            
                            if (currentSchema != null) {
                                // Get filtered route IDs to show in schema
                                val filteredRouteIds = uiState.routesWithMetadata.map { it.id }.toSet()
                                
                                SchemaView(
                                    schema = currentSchema,
                                    filteredRouteIds = filteredRouteIds,
                                    onRouteClick = { routeId ->
                                        // Find and open the route
                                        val route = uiState.routesWithMetadata.find { it.id == routeId }
                                        if (route != null) {
                                            selectedRouteWithMetadata = route
                                            showBottomSheet = true
                                        }
                                    },
                                    onPreviousClick = { viewModel.navigateToPreviousSchema() },
                                    onNextClick = { viewModel.navigateToNextSchema() },
                                    hasPrevious = uiState.schemas.size > 1,
                                    hasNext = uiState.schemas.size > 1,
                                    schemaCache = schemaCache,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .wrapContentHeight()
                                )
                            } else if (uiState.schemaError == null) {
                                // No schema available and no error shown yet - show placeholder
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = "No schema available for this sector",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Button(onClick = { viewModel.toggleViewMode() }) {
                                                Text("Switch to Map View")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // MAP mode - show map if available, or suggest schema view for trad areas
                        if (uiState.svgMapContent != null) {
                            // SVG Map section - moved to top for better integration
                            val svgMap = uiState.svgMapContent
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
                                                        stroke: $primaryColorHex;
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
                        } else if (uiState.area?.type == AreaType.TRAD && uiState.schemas.isNotEmpty()) {
                            // No map available but schemas exist - show placeholder suggesting schema view
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
                                        .height(200.dp)
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = "Map not available",
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = "Switch to Schema View to see route details",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Button(onClick = { viewModel.toggleViewMode() }) {
                                                Text("Switch to Schema View")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    // Filter section - now only search bar
                    item {
                        FilterSection(
                            searchQuery = uiState.searchQuery,
                            onSearchQueryChange = { viewModel.updateSearchQuery(it) }
                        )
                    }
                    
                    // Routes section
                    if (uiState.routes.isNotEmpty()) {
                        // Group routes if grouping is enabled
                        val groupedRoutes = when (uiState.groupingOption) {
                            com.example.topoclimb.ui.state.GroupingOption.BY_GRADE -> {
                                uiState.routesWithMetadata.groupBy { 
                                    it.grade?.let { gradeInt -> 
                                        GradeUtils.pointsToGrade(gradeInt, uiState.gradingSystem) 
                                    } ?: UNKNOWN_GRADE_LABEL 
                                }
                            }
                            com.example.topoclimb.ui.state.GroupingOption.BY_SECTOR -> {
                                uiState.routesWithMetadata.groupBy { routeWithMetadata ->
                                    // Get sector name from sectors list
                                    routeWithMetadata.sectorLocalId?.let { sectorLocalId ->
                                        uiState.sectors.find { it.localId == sectorLocalId }?.name
                                    } ?: UNKNOWN_SECTOR_LABEL
                                }
                            }
                            com.example.topoclimb.ui.state.GroupingOption.NONE -> {
                                mapOf("" to uiState.routesWithMetadata)
                            }
                        }
                        
                        groupedRoutes.forEach { (groupKey, routesInGroup) ->
                            // Show group header if grouping is enabled
                            if (uiState.groupingOption != com.example.topoclimb.ui.state.GroupingOption.NONE) {
                                item {
                                    Text(
                                        text = "$groupKey (${routesInGroup.size})",
                                        style = MaterialTheme.typography.titleLarge,
                                        modifier = Modifier.padding(top = 8.dp)
                                    )
                                }
                            } else {
                                // No grouping - show general header
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
                            }
                            
                            items(routesInGroup) { routeWithMetadata ->
                                // Calculate local ID display with prefix
                                val localId = if (routeWithMetadata.lineCount == 1) {
                                    routeWithMetadata.sectorLocalId?.let { "Sector n°$it" }
                                } else {
                                    routeWithMetadata.lineLocalId?.let { "Line n°$it" }
                                }
                                
                                RouteCard(
                                    thumbnail = routeWithMetadata.thumbnail,
                                    grade = routeWithMetadata.grade?.let { GradeUtils.pointsToGrade(it, uiState.gradingSystem) },
                                    color = routeWithMetadata.color,
                                    name = routeWithMetadata.name,
                                    localId = localId,
                                    isClimbed = loggedRouteIds.contains(routeWithMetadata.id),
                                    numberLogs = routeWithMetadata.numberLogs,
                                    numberComments = routeWithMetadata.numberComments,
                                    onClick = {
                                        selectedRouteWithMetadata = routeWithMetadata
                                        showBottomSheet = true
                                    }
                                )
                            }
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
        
        // Bottom Sheet for Route Details
        if (showBottomSheet && selectedRouteWithMetadata != null) {
            com.example.topoclimb.ui.components.RouteDetailBottomSheet(
                routeWithMetadata = selectedRouteWithMetadata!!,
                onDismiss = { showBottomSheet = false },
                gradingSystem = uiState.gradingSystem,
                onStartLogging = if (onStartLogging != null) {
                    { routeId, routeName, routeGrade, _ ->
                        // Pass the area type from uiState
                        onStartLogging(routeId, routeName, routeGrade, uiState.area?.type)
                        showBottomSheet = false // Close the bottom sheet when logging starts
                    }
                } else null,
                favoriteRoutesViewModel = favoriteRoutesViewModel
            )
        }
        
        // Filter Modal Dialog
        if (showFilterModal) {
            FilterModalDialog(
                minGrade = uiState.minGrade,
                maxGrade = uiState.maxGrade,
                grades = uiState.gradingSystem?.points?.keys?.toList(),
                showNewRoutesOnly = uiState.showNewRoutesOnly,
                selectedSectorId = uiState.selectedSectorId,
                sectors = uiState.sectors,
                climbedFilter = uiState.climbedFilter,
                groupingOption = uiState.groupingOption,
                showFavoritesOnly = uiState.showFavoritesOnly,
                onMinGradeChange = { viewModel.updateMinGrade(it) },
                onMaxGradeChange = { viewModel.updateMaxGrade(it) },
                onNewRoutesToggle = { viewModel.toggleNewRoutesFilter(it) },
                onSectorSelected = { viewModel.filterRoutesBySector(it) },
                onClimbedFilterChange = { viewModel.setClimbedFilter(it) },
                onGroupingOptionChange = { viewModel.setGroupingOption(it) },
                onFavoritesToggle = { viewModel.toggleFavoritesFilter(it) },
                onClearFilters = { viewModel.clearFilters() },
                onDismiss = { showFilterModal = false }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterSection(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Search bar only
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier.fillMaxWidth(),
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
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedBorderColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterModalDialog(
    minGrade: String?,
    maxGrade: String?,
    grades: List<String>?,
    showNewRoutesOnly: Boolean,
    selectedSectorId: Int?,
    sectors: List<com.example.topoclimb.data.Sector>,
    climbedFilter: com.example.topoclimb.ui.state.ClimbedFilter,
    groupingOption: com.example.topoclimb.ui.state.GroupingOption,
    showFavoritesOnly: Boolean,
    onMinGradeChange: (String?) -> Unit,
    onMaxGradeChange: (String?) -> Unit,
    onNewRoutesToggle: (Boolean) -> Unit,
    onSectorSelected: (Int?) -> Unit,
    onClimbedFilterChange: (com.example.topoclimb.ui.state.ClimbedFilter) -> Unit,
    onGroupingOptionChange: (com.example.topoclimb.ui.state.GroupingOption) -> Unit,
    onFavoritesToggle: (Boolean) -> Unit,
    onClearFilters: () -> Unit,
    onDismiss: () -> Unit
) {
    val hasActiveFilters = minGrade != null || maxGrade != null || showNewRoutesOnly || 
        climbedFilter != com.example.topoclimb.ui.state.ClimbedFilter.ALL || showFavoritesOnly
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filters & Grouping") },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Grade filters
                item {
                    Text(
                        text = "Grade Range",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    GradeRangeSlider(
                        minGrade = minGrade,
                        maxGrade = maxGrade,
                        grades = grades,
                        onMinGradeChange = onMinGradeChange,
                        onMaxGradeChange = onMaxGradeChange,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                // New routes filter
                item {
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
                }
                
                // Favorites filter
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Show only favorite routes",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Switch(
                            checked = showFavoritesOnly,
                            onCheckedChange = onFavoritesToggle
                        )
                    }
                }
                
                // Climbed filter
                item {
                    Text(
                        text = "Climbed Status",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = climbedFilter == com.example.topoclimb.ui.state.ClimbedFilter.CLIMBED,
                            onClick = { 
                                onClimbedFilterChange(
                                    if (climbedFilter == com.example.topoclimb.ui.state.ClimbedFilter.CLIMBED) 
                                        com.example.topoclimb.ui.state.ClimbedFilter.ALL 
                                    else 
                                        com.example.topoclimb.ui.state.ClimbedFilter.CLIMBED
                                ) 
                            },
                            label = { Text("Climbed") },
                            leadingIcon = if (climbedFilter == com.example.topoclimb.ui.state.ClimbedFilter.CLIMBED) {
                                { Icon(Icons.Default.Check, contentDescription = "Selected") }
                            } else null,
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = climbedFilter == com.example.topoclimb.ui.state.ClimbedFilter.NOT_CLIMBED,
                            onClick = { 
                                onClimbedFilterChange(
                                    if (climbedFilter == com.example.topoclimb.ui.state.ClimbedFilter.NOT_CLIMBED) 
                                        com.example.topoclimb.ui.state.ClimbedFilter.ALL 
                                    else 
                                        com.example.topoclimb.ui.state.ClimbedFilter.NOT_CLIMBED
                                ) 
                            },
                            label = { Text("Not Climbed") },
                            leadingIcon = if (climbedFilter == com.example.topoclimb.ui.state.ClimbedFilter.NOT_CLIMBED) {
                                { Icon(Icons.Default.Check, contentDescription = "Selected") }
                            } else null,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                
                // Grouping options
                item {
                    Text(
                        text = "Group Routes By",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = groupingOption == com.example.topoclimb.ui.state.GroupingOption.BY_GRADE,
                            onClick = { 
                                onGroupingOptionChange(
                                    if (groupingOption == com.example.topoclimb.ui.state.GroupingOption.BY_GRADE) 
                                        com.example.topoclimb.ui.state.GroupingOption.NONE 
                                    else 
                                        com.example.topoclimb.ui.state.GroupingOption.BY_GRADE
                                ) 
                            },
                            label = { Text("Grade") },
                            leadingIcon = if (groupingOption == com.example.topoclimb.ui.state.GroupingOption.BY_GRADE) {
                                { Icon(Icons.Default.Check, contentDescription = "Selected") }
                            } else null,
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = groupingOption == com.example.topoclimb.ui.state.GroupingOption.BY_SECTOR,
                            onClick = { 
                                onGroupingOptionChange(
                                    if (groupingOption == com.example.topoclimb.ui.state.GroupingOption.BY_SECTOR) 
                                        com.example.topoclimb.ui.state.GroupingOption.NONE 
                                    else 
                                        com.example.topoclimb.ui.state.GroupingOption.BY_SECTOR
                                ) 
                            },
                            label = { Text("Sector") },
                            leadingIcon = if (groupingOption == com.example.topoclimb.ui.state.GroupingOption.BY_SECTOR) {
                                { Icon(Icons.Default.Check, contentDescription = "Selected") }
                            } else null,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                
                // Sector filter
                item {
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
                                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true)
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
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        },
        dismissButton = if (hasActiveFilters) {
            {
                TextButton(onClick = onClearFilters) {
                    Text("Clear All")
                }
            }
        } else null
    )
}

@Composable
fun GradeRangeSlider(
    minGrade: String?,
    maxGrade: String?,
    grades: List<String>?,
    onMinGradeChange: (String?) -> Unit,
    onMaxGradeChange: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val grades = grades ?: listOf(
        "1a", "10c"
    )
    
    // Convert grade strings to slider indices
    val minIndex = minGrade?.let { grades.indexOf(it) } ?: 0
    val maxIndex = maxGrade?.let { grades.indexOf(it) } ?: (grades.size - 1)
    
    // State for slider values
    var sliderRange by remember(minIndex, maxIndex) { 
        mutableStateOf(minIndex.toFloat()..maxIndex.toFloat()) 
    }
    
    // State to track live grade values while dragging
    var liveMinGrade by remember { mutableStateOf(minGrade) }
    var liveMaxGrade by remember { mutableStateOf(maxGrade) }
    
    // Update live grades when external values change
    LaunchedEffect(minGrade, maxGrade) {
        liveMinGrade = minGrade
        liveMaxGrade = maxGrade
    }
    
    Column(modifier = modifier) {
        // Display selected range (live updates while dragging)
        Text(
            text = "${liveMinGrade ?: grades.first()} - ${liveMaxGrade ?: grades.last()}",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        // Range slider - The RangeSlider itself handles gesture conflicts properly
        RangeSlider(
            value = sliderRange,
            onValueChange = { range ->
                sliderRange = range
                // Update live grades in real-time
                val newMinIndex = range.start.toInt()
                val newMaxIndex = range.endInclusive.toInt()
                liveMinGrade = grades.getOrNull(newMinIndex) ?: grades.first()
                liveMaxGrade = grades.getOrNull(newMaxIndex) ?: grades.last()
            },
            onValueChangeFinished = {
                val newMinIndex = sliderRange.start.toInt()
                val newMaxIndex = sliderRange.endInclusive.toInt()
                onMinGradeChange(grades[newMinIndex])
                onMaxGradeChange(grades[newMaxIndex])
            },
            valueRange = 0f..(grades.size - 1).toFloat(),
            steps = grades.size - 2, // steps between start and end
            modifier = Modifier.fillMaxWidth()
        )
    }
}