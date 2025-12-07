package com.example.topoclimb.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.topoclimb.data.GradingSystem
import com.example.topoclimb.utils.GradeUtils
import kotlinx.coroutines.launch

/**
 * ViewPager-based workflow screen for logging routes
 * Combines all 3 steps into a single screen with dot indicators
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogRouteWorkflowScreen(
    backendId: String,
    siteId: Int,
    areaId: Int,
    routeId: Int,
    routeName: String,
    routeGrade: Int?,
    areaType: String?,
    gradingSystem: GradingSystem?,
    onClose: () -> Unit,
    onSubmit: (grade: Int, climbingType: String, climbingWay: String, comment: String?, videoUrl: String?) -> Unit,
    isLoading: Boolean = false,
    error: String? = null
) {
    // State for selected values
    var selectedClimbingType by remember { mutableStateOf<String?>(null) }
    var selectedClimbingWay by remember { mutableStateOf<String?>(null) }
    
    // Determine number of pages (skip step 2 for bouldering areas)
    val isBoulderingArea = areaType?.lowercase() == "bouldering"
    val pageCount = if (isBoulderingArea) 2 else 3
    
    val pagerState = rememberPagerState(pageCount = { pageCount })
    val coroutineScope = rememberCoroutineScope()
    
    // Auto-select bouldering if it's a bouldering area
    LaunchedEffect(isBoulderingArea) {
        if (isBoulderingArea) {
            selectedClimbingWay = "bouldering"
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(0, 0, 0, 0),
                title = { Text("Log Route $routeName") },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
            )
        },
        bottomBar = {
            // Dots indicator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(pageCount) { index ->
                    val isSelected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(if (isSelected) 12.dp else 8.dp)
                            .background(
                                color = if (isSelected) 
                                    MaterialTheme.colorScheme.primary 
                                else 
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                shape = CircleShape
                            )
                            .clickable(
                                enabled = !isLoading,
                                onClick = {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(index)
                                    }
                                }
                            )
                    )
                }
            }
        }
    ) { padding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            userScrollEnabled = false // Disable swipe, only navigate via dots or buttons
        ) { page ->
            when (page) {
                0 -> {
                    // Step 1: Select climbing type
                    LogRouteStep1Content(
                        routeName = routeName,
                        onTypeSelected = { climbingType ->
                            selectedClimbingType = climbingType
                            coroutineScope.launch {
                                if (isBoulderingArea) {
                                    // Skip to step 3 (page 1 in 2-page setup)
                                    pagerState.animateScrollToPage(1)
                                } else {
                                    // Go to step 2 (page 1 in 3-page setup)
                                    pagerState.animateScrollToPage(1)
                                }
                            }
                        }
                    )
                }
                1 -> {
                    if (isBoulderingArea) {
                        // Step 3: Enter details (for bouldering areas)
                        LogRouteStep3Content(
                            routeName = routeName,
                            routeGrade = routeGrade,
                            gradingSystem = gradingSystem,
                            isLoading = isLoading,
                            error = error,
                            showSendIcon = true,
                            onSubmit = { grade, comment, videoUrl ->
                                selectedClimbingType?.let { climbingType ->
                                    selectedClimbingWay?.let { climbingWay ->
                                        onSubmit(grade, climbingType, climbingWay, comment, videoUrl)
                                    }
                                }
                            }
                        )
                    } else {
                        // Step 2: Select climbing way (for non-bouldering areas)
                        LogRouteStep2Content(
                            routeName = routeName,
                            areaType = areaType,
                            onWaySelected = { climbingWay ->
                                selectedClimbingWay = climbingWay
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(2)
                                }
                            }
                        )
                    }
                }
                2 -> {
                    // Step 3: Enter details (for non-bouldering areas)
                    LogRouteStep3Content(
                        routeName = routeName,
                        routeGrade = routeGrade,
                        gradingSystem = gradingSystem,
                        isLoading = isLoading,
                        error = error,
                        showSendIcon = true,
                        onSubmit = { grade, comment, videoUrl ->
                            selectedClimbingType?.let { climbingType ->
                                selectedClimbingWay?.let { climbingWay ->
                                    onSubmit(grade, climbingType, climbingWay, comment, videoUrl)
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

/**
 * Content for Step 1 without the scaffold (to be used in ViewPager)
 */
@Composable
fun LogRouteStep1Content(
    routeName: String,
    onTypeSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Route name
        Text(
            text = "Logging: $routeName",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.fillMaxWidth()
        )
        
        Text(
            text = "How did you climb this route?",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Climbing type cards
        ClimbingTypeCard(
            icon = Icons.Default.FlashOn,
            type = "Flash",
            description = "Climbed on first try without prior knowledge",
            onClick = { onTypeSelected("flash") }
        )
        
        ClimbingTypeCard(
            icon = Icons.Default.Visibility,
            type = "View",
            description = "Climbed after watching others or prior attempts",
            onClick = { onTypeSelected("view") }
        )
        
        ClimbingTypeCard(
            icon = Icons.Default.FitnessCenter,
            type = "Work",
            description = "Working on the route, multiple attempts",
            onClick = { onTypeSelected("work") }
        )
    }
}

/**
 * Content for Step 2 without the scaffold (to be used in ViewPager)
 */
@Composable
fun LogRouteStep2Content(
    routeName: String,
    areaType: String?,
    onWaySelected: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Route name

        
        Text(
            text = "How did you climb this route?",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Climbing way cards
        ClimbingWayCard(
            icon = Icons.Default.Flag,
            way = "Lead",
            description = "Lead climbing with rope and quickdraws",
            onClick = { onWaySelected("lead") }
        )
        
        ClimbingWayCard(
            icon = Icons.Default.ArrowUpward,
            way = "Top-Rope",
            description = "Top-rope climbing with pre-placed anchor",
            onClick = { onWaySelected("top-rope") }
        )
        
        ClimbingWayCard(
            icon = Icons.Default.Landscape,
            way = "Bouldering",
            description = "Climbing without rope on shorter routes",
            onClick = { onWaySelected("bouldering") }
        )
    }
}

/**
 * Content for Step 3 without the scaffold (to be used in ViewPager)
 */
@Composable
fun LogRouteStep3Content(
    routeName: String,
    routeGrade: Int?,
    gradingSystem: GradingSystem?,
    isLoading: Boolean = false,
    error: String? = null,
    showSendIcon: Boolean = false,
    onSubmit: (grade: Int, comment: String?, videoUrl: String?) -> Unit
) {
    var selectedGrade by remember { 
        mutableStateOf(
            routeGrade?.let { GradeUtils.pointsToGrade(it, gradingSystem) } ?: ""
        ) 
    }
    var comment by remember { mutableStateOf("") }
    var videoUrl by remember { mutableStateOf("") }
    
    // Update selectedGrade when gradingSystem becomes available
    LaunchedEffect(gradingSystem, routeGrade) {
        routeGrade?.let { grade ->
            GradeUtils.pointsToGrade(grade, gradingSystem)?.let { gradeStr ->
                selectedGrade = gradeStr
            }
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        
        Text(
            text = "Final details",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Error message
        if (error != null) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = error,
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
            label = { Text("Your felt grade") },
            placeholder = { Text("e.g., 6a, 7b+") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !isLoading
        )
        
        // Comment input
        OutlinedTextField(
            value = comment,
            onValueChange = { if (it.length <= 1000) comment = it },
            label = { Text("Comment (optional)") },
            placeholder = { Text("Share your experience...") },
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            maxLines = 6,
            supportingText = {
                Text("${comment.length}/1000")
            },
            enabled = !isLoading
        )
        
        // Video URL input
        OutlinedTextField(
            value = videoUrl,
            onValueChange = { if (it.length <= 255) videoUrl = it },
            label = { Text("Video URL (optional)") },
            placeholder = { Text("https://...") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            supportingText = {
                Text("${videoUrl.length}/255")
            },
            enabled = !isLoading
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Validate grade
        val gradePoints = GradeUtils.gradeToPoints(selectedGrade, gradingSystem)
        val isValidGrade = gradePoints in GradeUtils.minGradePoints(gradingSystem)..GradeUtils.maxGradePoints(gradingSystem)
        val canSubmit = !isLoading && selectedGrade.isNotBlank() && isValidGrade
        
        // Show validation error for invalid grade
        if (selectedGrade.isNotBlank() && !isValidGrade) {
            Text(
                text = "Invalid grade. Please enter a valid climbing grade.",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        
        // Submit button
        Button(
            onClick = {
                if (isValidGrade) {
                    onSubmit(
                        gradePoints,
                        comment.takeIf { it.isNotBlank() },
                        videoUrl.takeIf { it.isNotBlank() }
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = canSubmit
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                if (showSendIcon) {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = "Submit Log",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}
