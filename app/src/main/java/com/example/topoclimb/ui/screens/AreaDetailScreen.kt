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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.topoclimb.data.GradingSystem
import com.example.topoclimb.ui.components.RouteCard
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
    areaId: Int,
    onBackClick: () -> Unit,
    onStartLogging: ((routeId: Int, routeName: String, routeGrade: Int?, areaType: String?) -> Unit)? = null,
    viewModel: AreaDetailViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val density = LocalDensity.current
    val primaryColorHex = String.format("#%06X", 0xFFFFFF and MaterialTheme.colorScheme.primary.toArgb())
    
    // Get shared logged routes state
    val loggedRouteIds by com.example.topoclimb.viewmodel.RouteDetailViewModel.sharedLoggedRouteIds.collectAsState()
    
    // Remember the map height once it's been measured
    var mapHeight by remember { mutableStateOf(0.dp) }
    
    // State for bottom sheet
    var showBottomSheet by remember { mutableStateOf(false) }
    var selectedRouteWithMetadata by remember { mutableStateOf<com.example.topoclimb.data.RouteWithMetadata?>(null) }
    
    // TODO: Update AreaDetailViewModel to use backendId for federated data
    LaunchedEffect(backendId, areaId) {
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
                    }
                    
                    // Filter section
                    item {
                        FilterSection(
                            searchQuery = uiState.searchQuery,
                            minGrade = uiState.minGrade,
                            maxGrade = uiState.maxGrade,
                            gradingSystem = uiState.gradingSystem,
                            showNewRoutesOnly = uiState.showNewRoutesOnly,
                            selectedSectorId = uiState.selectedSectorId,
                            sectors = uiState.sectors,
                            climbedFilter = uiState.climbedFilter,
                            groupingOption = uiState.groupingOption,
                            onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                            onMinGradeChange = { viewModel.updateMinGrade(it) },
                            onMaxGradeChange = { viewModel.updateMaxGrade(it) },
                            onNewRoutesToggle = { viewModel.toggleNewRoutesFilter(it) },
                            onSectorSelected = { viewModel.filterRoutesBySector(it) },
                            onClimbedFilterChange = { viewModel.setClimbedFilter(it) },
                            onGroupingOptionChange = { viewModel.setGroupingOption(it) },
                            onClearFilters = { viewModel.clearFilters() }
                        )
                    }
                    
                    // Routes section
                    if (uiState.routes.isNotEmpty()) {
                        // Group routes if grouping is enabled
                        val groupedRoutes = when (uiState.groupingOption) {
                            com.example.topoclimb.viewmodel.GroupingOption.BY_GRADE -> {
                                uiState.routesWithMetadata.groupBy { 
                                    it.grade?.let { gradeInt -> 
                                        GradeUtils.pointsToGrade(gradeInt, uiState.gradingSystem) 
                                    } ?: UNKNOWN_GRADE_LABEL 
                                }
                            }
                            com.example.topoclimb.viewmodel.GroupingOption.BY_SECTOR -> {
                                uiState.routesWithMetadata.groupBy { routeWithMetadata ->
                                    // Get sector name from sectors list
                                    routeWithMetadata.sectorLocalId?.let { sectorLocalId ->
                                        uiState.sectors.find { it.localId == sectorLocalId }?.name
                                    } ?: UNKNOWN_SECTOR_LABEL
                                }
                            }
                            com.example.topoclimb.viewmodel.GroupingOption.NONE -> {
                                mapOf("" to uiState.routesWithMetadata)
                            }
                        }
                        
                        groupedRoutes.forEach { (groupKey, routesInGroup) ->
                            // Show group header if grouping is enabled
                            if (uiState.groupingOption != com.example.topoclimb.viewmodel.GroupingOption.NONE) {
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
                } else null
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterSection(
    searchQuery: String,
    minGrade: String?,
    maxGrade: String?,
    gradingSystem: GradingSystem?,
    showNewRoutesOnly: Boolean,
    selectedSectorId: Int?,
    sectors: List<com.example.topoclimb.data.Sector>,
    climbedFilter: com.example.topoclimb.viewmodel.ClimbedFilter,
    groupingOption: com.example.topoclimb.viewmodel.GroupingOption,
    onSearchQueryChange: (String) -> Unit,
    onMinGradeChange: (String?) -> Unit,
    onMaxGradeChange: (String?) -> Unit,
    onNewRoutesToggle: (Boolean) -> Unit,
    onSectorSelected: (Int?) -> Unit,
    onClimbedFilterChange: (com.example.topoclimb.viewmodel.ClimbedFilter) -> Unit,
    onGroupingOptionChange: (com.example.topoclimb.viewmodel.GroupingOption) -> Unit,
    onClearFilters: () -> Unit
) {
    var showFilters by remember { mutableStateOf(false) }
    val hasActiveFilters = searchQuery.isNotEmpty() || minGrade != null || maxGrade != null || showNewRoutesOnly || climbedFilter != com.example.topoclimb.viewmodel.ClimbedFilter.ALL
    
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
                
                // Grade range slider
                GradeRangeSlider(
                    minGrade = minGrade,
                    maxGrade = maxGrade,
                    gradingSystem = gradingSystem,
                    onMinGradeChange = onMinGradeChange,
                    onMaxGradeChange = onMaxGradeChange,
                    modifier = Modifier.fillMaxWidth()
                )
                
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
                
                // Climbed filter
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
                        selected = climbedFilter == com.example.topoclimb.viewmodel.ClimbedFilter.ALL,
                        onClick = { onClimbedFilterChange(com.example.topoclimb.viewmodel.ClimbedFilter.ALL) },
                        label = { Text("All") },
                        leadingIcon = if (climbedFilter == com.example.topoclimb.viewmodel.ClimbedFilter.ALL) {
                            { Icon(Icons.Default.Check, contentDescription = "Selected") }
                        } else null,
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = climbedFilter == com.example.topoclimb.viewmodel.ClimbedFilter.CLIMBED,
                        onClick = { onClimbedFilterChange(com.example.topoclimb.viewmodel.ClimbedFilter.CLIMBED) },
                        label = { Text("Climbed") },
                        leadingIcon = if (climbedFilter == com.example.topoclimb.viewmodel.ClimbedFilter.CLIMBED) {
                            { Icon(Icons.Default.Check, contentDescription = "Selected") }
                        } else null,
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = climbedFilter == com.example.topoclimb.viewmodel.ClimbedFilter.NOT_CLIMBED,
                        onClick = { onClimbedFilterChange(com.example.topoclimb.viewmodel.ClimbedFilter.NOT_CLIMBED) },
                        label = { Text("Not Climbed") },
                        leadingIcon = if (climbedFilter == com.example.topoclimb.viewmodel.ClimbedFilter.NOT_CLIMBED) {
                            { Icon(Icons.Default.Check, contentDescription = "Selected") }
                        } else null,
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Grouping options
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
                        selected = groupingOption == com.example.topoclimb.viewmodel.GroupingOption.NONE,
                        onClick = { onGroupingOptionChange(com.example.topoclimb.viewmodel.GroupingOption.NONE) },
                        label = { Text("None") },
                        leadingIcon = if (groupingOption == com.example.topoclimb.viewmodel.GroupingOption.NONE) {
                            { Icon(Icons.Default.Check, contentDescription = "Selected") }
                        } else null,
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = groupingOption == com.example.topoclimb.viewmodel.GroupingOption.BY_GRADE,
                        onClick = { onGroupingOptionChange(com.example.topoclimb.viewmodel.GroupingOption.BY_GRADE) },
                        label = { Text("Grade") },
                        leadingIcon = if (groupingOption == com.example.topoclimb.viewmodel.GroupingOption.BY_GRADE) {
                            { Icon(Icons.Default.Check, contentDescription = "Selected") }
                        } else null,
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = groupingOption == com.example.topoclimb.viewmodel.GroupingOption.BY_SECTOR,
                        onClick = { onGroupingOptionChange(com.example.topoclimb.viewmodel.GroupingOption.BY_SECTOR) },
                        label = { Text("Sector") },
                        leadingIcon = if (groupingOption == com.example.topoclimb.viewmodel.GroupingOption.BY_SECTOR) {
                            { Icon(Icons.Default.Check, contentDescription = "Selected") }
                        } else null,
                        modifier = Modifier.weight(1f)
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

@Composable
fun GradeRangeSlider(
    minGrade: String?,
    maxGrade: String?,
    gradingSystem : GradingSystem?,
    onMinGradeChange: (String?) -> Unit,
    onMaxGradeChange: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val gradesFallback = listOf(
        "3a", "3b", "3c",
        "4a", "4b", "4c",
        "5a", "5b", "5c",
        "6a", "6a+", "6b", "6b+", "6c", "6c+",
        "7a", "7a+", "7b", "7b+", "7c", "7c+",
        "8a", "8a+", "8b", "8b+", "8c", "8c+",
        "9a", "9a+", "9b", "9b+", "9c"
    )
    val grades = gradingSystem?.points?.keys?.toList() ?: gradesFallback

    val minIndex = minGrade?.let { grades.indexOf(it) } ?: 0
    val maxIndex = maxGrade?.let { grades.indexOf(it) } ?: (grades.size - 1)

    var sliderRange by remember(minIndex, maxIndex) {
        mutableStateOf(minIndex.toFloat()..maxIndex.toFloat())
    }

    LaunchedEffect(minIndex, maxIndex) {
        sliderRange = minIndex.toFloat()..maxIndex.toFloat()
    }

    val view = androidx.compose.ui.platform.LocalView.current
    androidx.compose.runtime.DisposableEffect(view) {
        onDispose { view.parent?.requestDisallowInterceptTouchEvent(false) }
    }

    // NestedScrollConnection qui consomme la composante verticale
    val nestedScrollConnection = remember {
        object : androidx.compose.ui.input.nestedscroll.NestedScrollConnection {
            override fun onPreScroll(available: androidx.compose.ui.geometry.Offset, source: androidx.compose.ui.input.nestedscroll.NestedScrollSource): androidx.compose.ui.geometry.Offset {
                return if (available.y != 0f) androidx.compose.ui.geometry.Offset(0f, available.y) else androidx.compose.ui.geometry.Offset.Zero
            }
            override suspend fun onPreFling(available: androidx.compose.ui.unit.Velocity): androidx.compose.ui.unit.Velocity {
                return if (available.y != 0f) androidx.compose.ui.unit.Velocity(0f, available.y) else androidx.compose.ui.unit.Velocity.Zero
            }
        }
    }

    Column(modifier = modifier) {
        val currentMinIndex = sliderRange.start.toInt().coerceIn(0, grades.lastIndex)
        val currentMaxIndex = sliderRange.endInclusive.toInt().coerceIn(0, grades.lastIndex)
        val currentMinLabel = grades[currentMinIndex]
        val currentMaxLabel = grades[currentMaxIndex]

        Text(
            text = "$currentMinLabel - $currentMaxLabel",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        RangeSlider(
            value = sliderRange,
            onValueChange = { range ->
                sliderRange = range
                val newMin = grades[range.start.toInt().coerceIn(0, grades.lastIndex)]
                val newMax = grades[range.endInclusive.toInt().coerceIn(0, grades.lastIndex)]
                onMinGradeChange(newMin)
                onMaxGradeChange(newMax)
            },
            onValueChangeFinished = {
                val newMinIndex = sliderRange.start.toInt()
                val newMaxIndex = sliderRange.endInclusive.toInt()
                onMinGradeChange(grades[newMinIndex])
                onMaxGradeChange(grades[newMaxIndex])
            },
            valueRange = 0f..(grades.size - 1).toFloat(),
            steps = (grades.size - 2).coerceAtLeast(0),
            modifier = Modifier
                .fillMaxWidth()
                .nestedScroll(nestedScrollConnection)
                .pointerInput(view) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            val anyPressed = event.changes.any { it.pressed }
                            if (anyPressed) {
                                view.parent?.requestDisallowInterceptTouchEvent(true)
                            } else {
                                view.parent?.requestDisallowInterceptTouchEvent(false)
                            }
                            // Ne pas consommer : laisser RangeSlider gérer le geste
                        }
                    }
                }
        )
    }
}
