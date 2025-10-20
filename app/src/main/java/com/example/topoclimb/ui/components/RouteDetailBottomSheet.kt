package com.example.topoclimb.ui.components

import android.webkit.WebView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.material.icons.filled.Lens
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
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
import com.example.topoclimb.utils.GradeUtils
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
    viewModel: RouteDetailViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    var isBookmarked by remember { mutableStateOf(false) }
    var isSucceeded by remember { mutableStateOf(false) }
    
    LaunchedEffect(routeWithMetadata.id) {
        viewModel.loadRouteDetails(routeWithMetadata.id)
    }
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            // Tab Content (takes most of the space)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            ) {
                when (selectedTab) {
                    0 -> OverviewTab(
                        routeWithMetadata = routeWithMetadata,
                        uiState = uiState,
                        isBookmarked = isBookmarked,
                        isSucceeded = isSucceeded,
                        onBookmarkClick = { isBookmarked = !isBookmarked },
                        onSucceededClick = { isSucceeded = !isSucceeded },
                        onFocusToggle = { viewModel.toggleFocusMode() }
                    )
                    1 -> LogsTab(
                        uiState = uiState,
                        routeWithMetadata = routeWithMetadata,
                        gradingSystem = gradingSystem
                    )
                }
            }
            
            // Tabs at the bottom
            SecondaryTabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier.fillMaxWidth(),
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Overview") },
                    selectedContentColor = MaterialTheme.colorScheme.primary,
                    unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Logs") },
                    selectedContentColor = MaterialTheme.colorScheme.primary,
                    unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun OverviewTab(
    routeWithMetadata: RouteWithMetadata,
    uiState: com.example.topoclimb.viewmodel.RouteDetailUiState,
    isBookmarked: Boolean,
    isSucceeded: Boolean,
    onBookmarkClick: () -> Unit,
    onSucceededClick: () -> Unit,
    onFocusToggle: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
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
                            Text(
                                text = "Failed to load image",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
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

                    IconButton(onClick = onSucceededClick) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = if (isSucceeded) "Mark as not succeeded" else "Mark as succeeded",
                            tint = if (isSucceeded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
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
                        routeWithMetadata.grade?.let { grade ->
                            MetadataRow(
                                icon = Icons.Default.Star,
                                label = "Grade",
                                value = grade
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
                                    val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
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
                                    icon = Icons.Default.Info,
                                    label = "Tags",
                                    value = tags.joinToString(", ")
                                )
                            }
                        }
                    }
                }
                
                // Green Infobox
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = true,
                            onCheckedChange = null,
                            enabled = false,
                            colors = CheckboxDefaults.colors(
                                checkedColor = Color(0xFF4CAF50),
                                disabledCheckedColor = Color(0xFF4CAF50)
                            )
                        )
                        Text(
                            text = "There are no plans to dismantle this route at this time.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF2E7D32)
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
    gradingSystem: GradingSystem?
) {
    var showOnlyWithComments by remember { mutableStateOf(false) }
    
    // Filter logs based on the toggle state
    val filteredLogs = remember(uiState.logs, showOnlyWithComments) {
        if (showOnlyWithComments) {
            uiState.logs.filter { !it.comments.isNullOrBlank() }
        } else {
            uiState.logs
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 400.dp, max = 600.dp)
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
        
        // Content
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
            uiState.logsError != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Failed to load logs",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = uiState.logsError,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            filteredLogs.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (showOnlyWithComments) "No logs with comments" else "No logs yet",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
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

@Composable
private fun LogCard(
    log: com.example.topoclimb.data.Log,
    routeGrade: String?,
    gradingSystem: GradingSystem?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // User info row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // User avatar
                    SubcomposeAsyncImage(
                        model = log.userPpUrl,
                        contentDescription = "User avatar",
                        modifier = Modifier
                            .size(40.dp)
                            .clip(MaterialTheme.shapes.medium)
                            .background(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = MaterialTheme.shapes.medium
                            ),
                        contentScale = ContentScale.Crop
                    ) {
                        val state = painter.state
                        when (state) {
                            is AsyncImagePainter.State.Loading -> {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            is AsyncImagePainter.State.Error -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = log.userName.firstOrNull()?.uppercase() ?: "?",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
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
                    
                    Column {
                        Text(
                            text = log.userName,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = formatLogDate(log.createdAt),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // Verified badge
                if (log.isVerified) {
                    Surface(
                        color = Color(0xFF4CAF50).copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Verified",
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Verified",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF2E7D32)
                            )
                        }
                    }
                }
            }
            
            // Badges row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Type badge with icon
                val typeIcon = when (log.type.lowercase()) {
                    "flash" -> Icons.Filled.FlashOn
                    "view" -> Icons.Filled.Visibility
                    "work" -> Icons.Filled.Lens
                    else -> null
                }
                
                LogBadgeWithIcon(
                    text = log.type.capitalize(),
                    icon = typeIcon,
                    containerColor = when (log.type.lowercase()) {
                        "flash" -> Color(0xFFFFB74D)
                        "redpoint" -> Color(0xFF64B5F6)
                        "onsight" -> Color(0xFF81C784)
                        else -> MaterialTheme.colorScheme.secondaryContainer
                    },
                    contentColor = when (log.type.lowercase()) {
                        "flash" -> Color(0xFFE65100)
                        "redpoint" -> Color(0xFF0D47A1)
                        "onsight" -> Color(0xFF1B5E20)
                        else -> MaterialTheme.colorScheme.onSecondaryContainer
                    }
                )
                
                // Way badge with icon (only show if not bouldering)
                if (log.way.lowercase() != "bouldering") {
                    val wayIcon = when (log.way.lowercase()) {
                        "top-rope", "sport" -> Icons.Filled.Lens
                        "lead" -> Icons.Filled.Flag
                        else -> null
                    }
                    
                    LogBadgeWithIcon(
                        text = log.way.capitalize(),
                        icon = wayIcon,
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
                
                // Grade badge with comparison indicator
                // Convert log grade points to grade string
                val logGradeString = GradeUtils.pointsToGrade(log.grade, gradingSystem) ?: log.grade.toString()
                
                val gradeComparison = routeGrade?.let { routeGradeStr ->
                    // Convert route grade string to points for comparison
                    val routeGradePoints = GradeUtils.gradeToPoints(routeGradeStr, gradingSystem)
                    when {
                        log.grade > routeGradePoints -> GradeComparison.HIGHER
                        log.grade < routeGradePoints -> GradeComparison.LOWER
                        else -> GradeComparison.EQUAL
                    }
                }
                
                LogBadgeWithIcon(
                    text = "Grade: $logGradeString",
                    icon = when (gradeComparison) {
                        GradeComparison.HIGHER -> Icons.Default.KeyboardArrowUp
                        GradeComparison.LOWER -> Icons.Default.KeyboardArrowDown
                        GradeComparison.EQUAL -> Icons.Default.Check
                        null -> null
                    },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            
            // Comments if available
            if (!log.comments.isNullOrBlank()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ChatBubble,
                            contentDescription = "Comment",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Comment",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = log.comments,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LogBadge(
    text: String,
    containerColor: Color,
    contentColor: Color
) {
    Surface(
        color = containerColor,
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Medium
            ),
            color = contentColor
        )
    }
}

@Composable
private fun LogBadgeWithIcon(
    text: String,
    icon: ImageVector?,
    containerColor: Color,
    contentColor: Color
) {
    Surface(
        color = containerColor,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = contentColor
            )
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = contentColor
                )
            }
        }
    }
}

private enum class GradeComparison {
    HIGHER, LOWER, EQUAL
}

private fun formatLogDate(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        date?.let { outputFormat.format(it) } ?: dateString
    } catch (e: Exception) {
        dateString
    }
}

private fun String.capitalize(): String {
    return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
}
