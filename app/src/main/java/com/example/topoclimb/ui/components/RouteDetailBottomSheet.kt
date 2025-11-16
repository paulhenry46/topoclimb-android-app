package com.example.topoclimb.ui.components

import android.webkit.WebView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lens
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.launch
import coil.ImageLoader
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.request.CachePolicy
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.SubcomposeAsyncImage
import coil.compose.AsyncImagePainter
import com.example.topoclimb.data.GradingSystem
import com.example.topoclimb.data.RouteWithMetadata
import com.example.topoclimb.R
import com.example.topoclimb.ui.components.route.LogCard
import com.example.topoclimb.utils.GradeUtils
import com.example.topoclimb.utils.NetworkUtils
import com.example.topoclimb.viewmodel.RouteDetailViewModel
import java.text.SimpleDateFormat
import java.util.*

// Z-index constants for layering
private const val Z_INDEX_SVG_OVERLAY = 1f
private const val Z_INDEX_FOCUS_TOGGLE = 2f

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteDetailBottomSheet(
    routeWithMetadata: RouteWithMetadata,
    onDismiss: () -> Unit,
    gradingSystem: GradingSystem? = null,
    onStartLogging: ((routeId: Int, routeName: String, routeGrade: Int?, areaType: String?) -> Unit)? = null,
    viewModel: RouteDetailViewModel = viewModel(),
    favoriteRoutesViewModel: com.example.topoclimb.viewmodel.FavoriteRoutesViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val favoriteRoutesUiState by favoriteRoutesViewModel.uiState.collectAsState()
    val pagerState = rememberPagerState(pageCount = { 2 })
    val coroutineScope = rememberCoroutineScope()
    
    // Check if this route is in favorites
    val isBookmarked = favoriteRoutesUiState.favoriteRoutes.any { it.id == routeWithMetadata.id }
    
    LaunchedEffect(routeWithMetadata.id) {
        viewModel.loadRouteDetails(routeWithMetadata.id)
    }
    
    // Detect system dark mode
    val isDarkTheme = isSystemInDarkTheme()
    
    // Generate dynamic color scheme from route color
    val dynamicColorScheme = remember(routeWithMetadata.color, isDarkTheme) {
        com.example.topoclimb.ui.utils.generateColorSchemeFromHex(
            routeWithMetadata.color,
            isDark = isDarkTheme
        )
    }
    
    MaterialTheme(colorScheme = dynamicColorScheme) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
            // Tab Content with HorizontalPager for swipe gesture
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            ) { page ->
                when (page) {
                    0 -> OverviewTab(
                        routeWithMetadata = routeWithMetadata,
                        uiState = uiState,
                        isBookmarked = isBookmarked,
                        onBookmarkClick = { favoriteRoutesViewModel.toggleFavorite(routeWithMetadata) },
                        onFocusToggle = { viewModel.toggleFocusMode() },
                        viewModel = viewModel,
                        onLogCreated = { 
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(1)
                            }
                        },
                        onStartLogging = onStartLogging,
                        gradingSystem = gradingSystem
                    )
                    1 -> LogsTab(
                        uiState = uiState,
                        routeWithMetadata = routeWithMetadata,
                        gradingSystem = gradingSystem,
                        viewModel = viewModel,
                        onStartLogging = onStartLogging
                    )
                }
            }
            
            // Tabs at the bottom - sync with pager
            SecondaryTabRow(
                selectedTabIndex = pagerState.currentPage,
                modifier = Modifier.fillMaxWidth(),
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            ) {
                Tab(
                    selected = pagerState.currentPage == 0,
                    onClick = { 
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(0)
                        }
                    },
                    text = { Text("Overview") },
                    selectedContentColor = MaterialTheme.colorScheme.primary,
                    unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Tab(
                    selected = pagerState.currentPage == 1,
                    onClick = { 
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(1)
                        }
                    },
                    text = { 
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Logs")
                            // Badge showing number of logs
                            if (uiState.logs.isNotEmpty()) {
                                Badge(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ) {
                                    Text(uiState.logs.size.toString())
                                }
                            }
                        }
                    },
                    selectedContentColor = MaterialTheme.colorScheme.primary,
                    unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
    }
}

@Composable
private fun OverviewTab(
    routeWithMetadata: RouteWithMetadata,
    uiState: com.example.topoclimb.viewmodel.RouteDetailUiState,
    isBookmarked: Boolean,
    onBookmarkClick: () -> Unit,
    onFocusToggle: () -> Unit,
    viewModel: RouteDetailViewModel,
    onLogCreated: () -> Unit,
    onStartLogging: ((routeId: Int, routeName: String, routeGrade: Int?, areaType: String?) -> Unit)? = null,
    gradingSystem: GradingSystem? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 400.dp, max = 600.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Photo with Circle SVG Overlay - Full width at the top
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        ) {
            // Route photo - use filtered_picture when focus mode is enabled
            val pictureUrl = if (uiState.isFocusMode) {
                uiState.route?.filteredPicture ?: uiState.route?.picture ?: routeWithMetadata.thumbnail
            } else {
                uiState.route?.picture ?: routeWithMetadata.thumbnail
            }
            
            SubcomposeAsyncImage(
                model = pictureUrl,
                contentDescription = "Route photo",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            ) {
                val state = painter.state
                val context = LocalContext.current
                val isNetworkAvailable = NetworkUtils.isNetworkAvailable(context)
                
                when (state) {
                    is AsyncImagePainter.State.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    is AsyncImagePainter.State.Error -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!isNetworkAvailable) {
                                // No network - show no_network drawable
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.no_network),
                                        contentDescription = "No network",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(80.dp)
                                    )
                                    Text(
                                        text = "No network",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            } else {
                                Text(
                                    text = "Failed to load image",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    else -> {
                        Image(
                            painter = painter,
                            contentDescription = contentDescription,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = contentScale
                        )
                    }
                }
            }
            
            // Circle SVG overlay - only show when not in focus mode
            if (!uiState.isFocusMode) {
                uiState.circleSvgContent?.let { svgContent ->
                    val context = LocalContext.current
                    val imageLoader = remember(context) {
                        ImageLoader.Builder(context)
                            .components {
                                add(SvgDecoder.Factory())
                            }
                            .build()
                    }

                    val svgBytes = remember(svgContent) { svgContent.toByteArray(Charsets.UTF_8) }
                    val request = remember(svgBytes, imageLoader) {
                        ImageRequest.Builder(context)
                            .data(svgBytes)
                            // Désactiver la mise en cache si le SVG change fréquemment
                            .diskCachePolicy(CachePolicy.DISABLED)
                            .memoryCachePolicy(CachePolicy.DISABLED)
                            .build()
                    }

                    SubcomposeAsyncImage(
                        model = request,
                        imageLoader = imageLoader,
                        contentDescription = "Circle SVG overlay",
                        modifier = Modifier
                            .fillMaxSize()
                            .zIndex(Z_INDEX_SVG_OVERLAY),
                        contentScale = ContentScale.Crop, // ou ContentScale.Fit si vous voulez contenir sans crop
                        alignment = Alignment.Center
                    ) {
                        when (val state = painter.state) {
                            is AsyncImagePainter.State.Loading -> {
                                // Optionnel : afficher rien ou un indicateur léger
                            }
                            is AsyncImagePainter.State.Error -> {
                                // Optionnel : fallback si échec du rendu SVG
                            }
                            else -> {
                                // Affiche le SVG rendu
                                SubcomposeAsyncImageContent()
                            }
                        }
                    }
                } ?: run {
                    println("RouteDetailBottomSheet: SVG content is null, not showing overlay")
                }
            } else {
                println("RouteDetailBottomSheet: Focus mode is enabled, hiding SVG overlay")
            }
            
            // Focus toggle at bottom-left
            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .zIndex(Z_INDEX_FOCUS_TOGGLE)  // Ensure focus toggle is always on top
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(topEnd = 16.dp)
                    )
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Focus",
                    modifier = Modifier.padding(start = 8.dp),
                    style = MaterialTheme.typography.bodySmall
                )
                Switch(
                    checked = uiState.isFocusMode,
                    onCheckedChange = { onFocusToggle() }
                )
            }
        }
        
        // Content with padding
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Route Name and Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Name and Sector/Line
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = routeWithMetadata.name,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Calculate local ID display
                    val localId = if (routeWithMetadata.lineCount == 1) {
                        routeWithMetadata.sectorLocalId?.let { "Sector n°$it" }
                    } else {
                        routeWithMetadata.lineLocalId?.let { "Line n°$it" }
                    }

                    localId?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Right: Bookmark and Success buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(onClick = onBookmarkClick) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Default.Star else Icons.Outlined.Star,
                            contentDescription = if (isBookmarked) "Remove bookmark" else "Bookmark",
                            tint = if (isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(onClick = {
                        if (onStartLogging != null) {
                            // Use new 3-step flow
                            onStartLogging(routeWithMetadata.id, routeWithMetadata.name, routeWithMetadata.grade, null)
                        } else {
                            // Fallback to old flow
                            onLogCreated()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = if (uiState.isRouteLogged) "Already logged" else "Log this route",
                            tint = if (uiState.isRouteLogged) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
            
            // Route Metadata Card
            Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Grade
                        routeWithMetadata.grade?.let { gradeInt ->
                            val gradeStr = GradeUtils.pointsToGrade(gradeInt, gradingSystem) ?: gradeInt.toString()
                            MetadataRow(
                                icon = Icons.Default.Star,
                                label = "Grade",
                                value = gradeStr
                            )
                        }
                        
                        // Height
                        routeWithMetadata.height?.let { height ->
                            MetadataRow(
                                icon = Icons.Default.Info,
                                label = "Height",
                                value = "${height}m"
                            )
                        }
                        
                        // Opener(s)
                        uiState.route?.openers?.let { openers ->
                            if (openers.isNotEmpty()) {
                                MetadataRow(
                                    icon = Icons.Default.Person,
                                    label = "Opener(s)",
                                    value = openers.joinToString(", ")
                                )
                            }
                        }
                        
                        // Date of Creation
                        uiState.route?.createdAt?.let { createdAt ->
                            val formattedDate = remember(createdAt) {
                                try {
                                    val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.getDefault())
                                    val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                                    val date = inputFormat.parse(createdAt)
                                    date?.let { outputFormat.format(it) } ?: createdAt
                                } catch (e: Exception) {
                                    null
                                }
                            }
                            
                            formattedDate?.let { dateString ->
                                MetadataRow(
                                    icon = Icons.Default.DateRange,
                                    label = "Date of Creation",
                                    value = dateString
                                )
                            }
                        }
                        
                        // Tags
                        uiState.route?.tags?.let { tags ->
                            if (tags.isNotEmpty()) {
                                MetadataRow(
                                    icon = Icons.Default.Sell,
                                    label = "Tags",
                                    value = tags.joinToString(", ")
                                )
                            }
                        }
                    }
                }
                
                // Infobox using local primary color - shows route removal information
                val removalInfo = remember(uiState.route?.removingAt) {
                    calculateRemovalInfo(uiState.route?.removingAt)
                }
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = when (removalInfo.type) {
                            RemovalInfoType.URGENT -> MaterialTheme.colorScheme.errorContainer
                            RemovalInfoType.SCHEDULED -> MaterialTheme.colorScheme.tertiaryContainer
                            RemovalInfoType.NO_PLAN -> MaterialTheme.colorScheme.primaryContainer
                        }
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when (removalInfo.type) {
                                RemovalInfoType.URGENT -> Icons.Default.Warning
                                RemovalInfoType.SCHEDULED -> Icons.Default.Info
                                RemovalInfoType.NO_PLAN -> Icons.Default.Check
                            },
                            contentDescription = null,
                            tint = when (removalInfo.type) {
                                RemovalInfoType.URGENT -> MaterialTheme.colorScheme.onErrorContainer
                                RemovalInfoType.SCHEDULED -> MaterialTheme.colorScheme.onTertiaryContainer
                                RemovalInfoType.NO_PLAN -> MaterialTheme.colorScheme.onPrimaryContainer
                            },
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = removalInfo.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = when (removalInfo.type) {
                                RemovalInfoType.URGENT -> MaterialTheme.colorScheme.onErrorContainer
                                RemovalInfoType.SCHEDULED -> MaterialTheme.colorScheme.onTertiaryContainer
                                RemovalInfoType.NO_PLAN -> MaterialTheme.colorScheme.onPrimaryContainer
                            }
                        )
                    }
                }
            }
        }
    }

@Composable
private fun MetadataRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun LogsTab(
    uiState: com.example.topoclimb.viewmodel.RouteDetailUiState,
    routeWithMetadata: RouteWithMetadata,
    gradingSystem: GradingSystem?,
    viewModel: RouteDetailViewModel,
    onStartLogging: ((routeId: Int, routeName: String, routeGrade: Int?, areaType: String?) -> Unit)? = null
) {
    var showOnlyWithComments by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val isNetworkAvailable = NetworkUtils.isNetworkAvailable(context)
    
    // Filter logs based on the toggle state
    val filteredLogs = remember(uiState.logs, showOnlyWithComments) {
        if (showOnlyWithComments) {
            uiState.logs.filter { !it.comments.isNullOrBlank() }
        } else {
            uiState.logs
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 400.dp, max = 600.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Filter header
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Filter Logs",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (showOnlyWithComments) "Showing logs with comments" else "Showing all logs",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "With Comments",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Switch(
                            checked = showOnlyWithComments,
                            onCheckedChange = { showOnlyWithComments = it }
                        )
                    }
                }
            }
            
            // Content with pull-to-refresh
            PullToRefreshBox(
                isRefreshing = uiState.isRefreshingLogs,
                onRefresh = { viewModel.refreshLogs(routeWithMetadata.id) },
                modifier = Modifier.fillMaxSize()
            ) {
                when {
                    uiState.isLogsLoading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    uiState.logsError != null && !isNetworkAvailable && uiState.logs.isEmpty() -> {
                        // No internet and no cached logs
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.no_network),
                                    contentDescription = "No network",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(80.dp)
                                )
                                Text(
                                    text = "No network",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                    filteredLogs.isEmpty() && showOnlyWithComments && uiState.logs.isNotEmpty() -> {
                        // No logs with comments but there are logs
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.comment),
                                    contentDescription = "No comments",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(80.dp)
                                )
                                Text(
                                    text = "No logs with comments for now. Be the first to comment ! That always makes the route openers happy!",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                    uiState.logs.isEmpty() -> {
                        // No logs at all
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.pen),
                                    contentDescription = "No logs",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(80.dp)
                                )
                                Text(
                                    text = "No logs for now. Be the first to log !",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                    else -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            filteredLogs.forEach { log ->
                                LogCard(
                                    log = log,
                                    routeGrade = routeWithMetadata.grade,
                                    gradingSystem = gradingSystem
                                )
                            }
                        }
                    }
                }
            }
        }
        
        // FAB for adding logs
        if (onStartLogging != null) {
            FloatingActionButton(
                onClick = {
                    onStartLogging(routeWithMetadata.id, routeWithMetadata.name, routeWithMetadata.grade, null)
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Log"
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateLogDialog(
    routeWithMetadata: RouteWithMetadata,
    gradingSystem: GradingSystem?,
    uiState: com.example.topoclimb.viewmodel.RouteDetailUiState,
    onDismiss: () -> Unit,
    onCreateLog: (grade: Int, type: String, way: String, comment: String?, videoUrl: String?) -> Unit
) {
    var selectedGrade by remember { 
        mutableStateOf(
            routeWithMetadata.grade?.let { GradeUtils.pointsToGrade(it, gradingSystem) } ?: ""
        ) 
    }
    var selectedType by remember { mutableStateOf("work") }
    var selectedWay by remember { mutableStateOf("bouldering") }
    var comment by remember { mutableStateOf("") }
    var videoUrl by remember { mutableStateOf("") }
    var showTypeDropdown by remember { mutableStateOf(false) }
    var showWayDropdown by remember { mutableStateOf(false) }
    
    val types = listOf("work", "flash", "view")
    val ways = listOf("top-rope", "lead", "bouldering")
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Log Route",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
                
                // Show error if any
                if (uiState.createLogError != null) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = uiState.createLogError,
                            modifier = Modifier.padding(12.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                
                // Grade input
                OutlinedTextField(
                    value = selectedGrade,
                    onValueChange = { selectedGrade = it },
                    label = { Text("Grade") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                // Type dropdown
                ExposedDropdownMenuBox(
                    expanded = showTypeDropdown,
                    onExpandedChange = { showTypeDropdown = it }
                ) {
                    OutlinedTextField(
                        value = selectedType.capitalize(),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showTypeDropdown) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(
                        expanded = showTypeDropdown,
                        onDismissRequest = { showTypeDropdown = false }
                    ) {
                        types.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.capitalize()) },
                                onClick = {
                                    selectedType = type
                                    showTypeDropdown = false
                                }
                            )
                        }
                    }
                }
                
                // Way dropdown
                ExposedDropdownMenuBox(
                    expanded = showWayDropdown,
                    onExpandedChange = { showWayDropdown = it }
                ) {
                    OutlinedTextField(
                        value = selectedWay.capitalize(),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Way") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showWayDropdown) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(
                        expanded = showWayDropdown,
                        onDismissRequest = { showWayDropdown = false }
                    ) {
                        ways.forEach { way ->
                            DropdownMenuItem(
                                text = { Text(way.capitalize()) },
                                onClick = {
                                    selectedWay = way
                                    showWayDropdown = false
                                }
                            )
                        }
                    }
                }
                
                // Comment input
                OutlinedTextField(
                    value = comment,
                    onValueChange = { if (it.length <= 1000) comment = it },
                    label = { Text("Comment (optional)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    maxLines = 5,
                    supportingText = {
                        Text("${comment.length}/1000")
                    }
                )
                
                // Video URL input
                OutlinedTextField(
                    value = videoUrl,
                    onValueChange = { if (it.length <= 255) videoUrl = it },
                    label = { Text("Video URL (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    supportingText = {
                        Text("${videoUrl.length}/255")
                    }
                )
                
                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        enabled = !uiState.isCreatingLog
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            val gradePoints = GradeUtils.gradeToPoints(selectedGrade, gradingSystem)
                            if (gradePoints in 300..950) {
                                onCreateLog(
                                    gradePoints,
                                    selectedType,
                                    selectedWay,
                                    comment.takeIf { it.isNotBlank() },
                                    videoUrl.takeIf { it.isNotBlank() }
                                )
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !uiState.isCreatingLog && selectedGrade.isNotBlank()
                    ) {
                        if (uiState.isCreatingLog) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text("Create")
                        }
                    }
                }
            }
        }
    }
}

/**
 * Enum representing the type of removal information
 */
private enum class RemovalInfoType {
    NO_PLAN,    // No removal planned
    SCHEDULED,  // Removal scheduled more than a week away
    URGENT      // Removal scheduled within a week
}

/**
 * Data class holding removal information
 */
private data class RemovalInfo(
    val type: RemovalInfoType,
    val message: String
)

/**
 * Calculate removal information based on the removing_at date
 */
private fun calculateRemovalInfo(removingAt: String?): RemovalInfo {
    if (removingAt.isNullOrBlank()) {
        return RemovalInfo(
            type = RemovalInfoType.NO_PLAN,
            message = "There are no plans to dismantle this route at this time."
        )
    }
    
    try {
        // Parse the date - handle both formats: "2025-09-06 00:00:00" and "2025-09-06"
        val dateFormat = when {
            removingAt.contains(" ") -> SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            else -> SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        }
        
        val removalDate = dateFormat.parse(removingAt) ?: return RemovalInfo(
            type = RemovalInfoType.NO_PLAN,
            message = "There are no plans to dismantle this route at this time."
        )
        
        val now = Date()
        val daysDifference = ((removalDate.time - now.time) / (1000 * 60 * 60 * 24)).toInt()
        
        // Format date as dd/MM/yy
        val displayFormat = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
        val formattedDate = displayFormat.format(removalDate)
        
        return when {
            daysDifference < 0 -> {
                // Already passed, treat as no plan
                RemovalInfo(
                    type = RemovalInfoType.NO_PLAN,
                    message = "There are no plans to dismantle this route at this time."
                )
            }
            daysDifference <= 7 -> {
                // Within a week
                RemovalInfo(
                    type = RemovalInfoType.URGENT,
                    message = "This track is on its last legs! It will be dismantled on $formattedDate."
                )
            }
            else -> {
                // More than a week
                RemovalInfo(
                    type = RemovalInfoType.SCHEDULED,
                    message = "The removal of this route is scheduled for $formattedDate."
                )
            }
        }
    } catch (e: Exception) {
        // If parsing fails, return no plan
        return RemovalInfo(
            type = RemovalInfoType.NO_PLAN,
            message = "There are no plans to dismantle this route at this time."
        )
    }
}

private fun String.capitalize(): String {
    return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase(java.util.Locale.getDefault()) else it.toString() }
}
