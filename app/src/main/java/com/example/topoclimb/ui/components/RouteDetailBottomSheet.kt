package com.example.topoclimb.ui.components

import android.webkit.WebView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.topoclimb.data.RouteWithMetadata
import com.example.topoclimb.viewmodel.RouteDetailViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteDetailBottomSheet(
    routeWithMetadata: RouteWithMetadata,
    onDismiss: () -> Unit,
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
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            // Secondary Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Overview") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Logs") }
                )
            }
            
            // Tab Content
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
                1 -> LogsTab()
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
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Photo with Circle SVG Overlay
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        ) {
            // Route photo
            val pictureUrl = uiState.route?.picture ?: routeWithMetadata.thumbnail
            AsyncImage(
                model = pictureUrl,
                contentDescription = "Route photo",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            
            // Circle SVG overlay
            uiState.circleSvgContent?.let { svgContent ->
                AndroidView(
                    factory = { context ->
                        WebView(context).apply {
                            settings.javaScriptEnabled = false
                            settings.loadWithOverviewMode = true
                            settings.useWideViewPort = true
                            setBackgroundColor(android.graphics.Color.TRANSPARENT)
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
                                        margin: 0;
                                        padding: 0;
                                        background: transparent;
                                    }
                                    svg {
                                        width: 100%;
                                        height: 100%;
                                        position: absolute;
                                        top: 0;
                                        left: 0;
                                    }
                                </style>
                            </head>
                            <body>
                                $svgContent
                            </body>
                            </html>
                        """.trimIndent()
                        webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
            
            // Focus toggle at bottom-left
            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                        shape = RoundedCornerShape(16.dp)
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
                    )
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
                // Opener(s)
                uiState.route?.openers?.let { openers ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Openers",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Column {
                            Text(
                                text = "Opener(s)",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = openers,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }
                }
                
                // Grade
                routeWithMetadata.grade?.let { grade ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Grade",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Column {
                            Text(
                                text = "Grade",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = grade,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
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
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Date of creation",
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Column {
                                Text(
                                    text = "Date of Creation",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = dateString,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }
                        }
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
                    text = "There are no plans to dismantle this track at this time.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF2E7D32)
                )
            }
        }
    }
}

@Composable
private fun LogsTab() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Logs feature coming soon...",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
