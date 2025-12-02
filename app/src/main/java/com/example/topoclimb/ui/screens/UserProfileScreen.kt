package com.example.topoclimb.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import com.example.topoclimb.data.GradingSystem
import com.example.topoclimb.data.Route
import com.example.topoclimb.data.RouteWithMetadata
import com.example.topoclimb.ui.components.profile.RoutesByGradeChart
import com.example.topoclimb.ui.components.profile.StatsItem
import com.example.topoclimb.ui.components.profile.parseRoutesbyGrade
import com.example.topoclimb.ui.utils.parseRouteColor
import com.example.topoclimb.utils.GradeUtils
import com.example.topoclimb.viewmodel.RouteLogWithDetails
import com.example.topoclimb.viewmodel.UserProfileViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    userId: Int,
    backendId: String,
    onBackClick: () -> Unit = {},
    viewModel: UserProfileViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // State for route bottom sheet
    var showRouteBottomSheet by remember { mutableStateOf(false) }
    var selectedRouteWithMetadata by remember { mutableStateOf<RouteWithMetadata?>(null) }
    var selectedGradingSystem by remember { mutableStateOf<GradingSystem?>(null) }
    
    // Load user profile when screen is displayed
    LaunchedEffect(userId, backendId) {
        viewModel.loadUserProfile(userId, backendId)
    }
    
    // Show success/error messages
    LaunchedEffect(uiState.friendActionMessage) {
        uiState.friendActionMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearMessages()
        }
    }
    
    LaunchedEffect(uiState.friendActionError) {
        uiState.friendActionError?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearMessages()
        }
    }
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(uiState.userProfile?.name ?: "User Profile") },
                windowInsets = WindowInsets(0, 0, 0, 0),
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = uiState.error ?: "Unknown error",
                            color = MaterialTheme.colorScheme.error
                        )
                        Button(onClick = { viewModel.refresh() }) {
                            Text("Retry")
                        }
                    }
                }
            }
            uiState.userProfile != null -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Profile header
                    item {
                        ProfileHeader(
                            name = uiState.userProfile!!.name,
                            profilePhotoUrl = uiState.userProfile!!.profilePhotoUrl,
                            isFriend = uiState.isFriend,
                            isCheckingFriendship = uiState.isCheckingFriendship,
                            isAddingFriend = uiState.isAddingFriend,
                            isRemovingFriend = uiState.isRemovingFriend,
                            isAuthenticated = viewModel.isAuthenticated(backendId),
                            isOwnProfile = viewModel.isOwnProfile(backendId),
                            onAddFriend = { viewModel.addFriend() },
                            onRemoveFriend = { viewModel.removeFriend() }
                        )
                    }
                    
                    // Stats section
                    uiState.userProfile!!.stats?.let { stats ->
                        item {
                            UserStatsCard(stats = stats)
                        }
                    }
                    
                    // Routes section header
                    item {
                        Text(
                            text = "Recent Climbs",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                    
                    // Routes list
                    when {
                        uiState.isLoadingRoutes -> {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(100.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                        }
                        uiState.routesError != null -> {
                            item {
                                Text(
                                    text = uiState.routesError ?: "Failed to load routes",
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                        uiState.routeLogs.isEmpty() -> {
                            item {
                                Text(
                                    text = "No routes climbed yet",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        else -> {
                            items(uiState.routeLogs) { routeLogWithDetails ->
                                UserRouteLogCard(
                                    routeLogWithDetails = routeLogWithDetails,
                                    gradingSystem = routeLogWithDetails.gradingSystem,
                                    onClick = {
                                        // Create RouteWithMetadata from Route for bottom sheet
                                        routeLogWithDetails.route?.let { route ->
                                            selectedRouteWithMetadata = RouteWithMetadata(route = route)
                                            selectedGradingSystem = routeLogWithDetails.gradingSystem
                                            showRouteBottomSheet = true
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Route detail bottom sheet
    if (showRouteBottomSheet && selectedRouteWithMetadata != null) {
        com.example.topoclimb.ui.components.RouteDetailBottomSheet(
            routeWithMetadata = selectedRouteWithMetadata!!,
            onDismiss = { showRouteBottomSheet = false },
            gradingSystem = selectedGradingSystem,
            onStartLogging = null,
            backendId = backendId
        )
    }
}

@Composable
private fun ProfileHeader(
    name: String,
    profilePhotoUrl: String?,
    isFriend: Boolean,
    isCheckingFriendship: Boolean,
    isAddingFriend: Boolean,
    isRemovingFriend: Boolean,
    isAuthenticated: Boolean,
    isOwnProfile: Boolean,
    onAddFriend: () -> Unit,
    onRemoveFriend: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Profile picture
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            SubcomposeAsyncImage(
                model = profilePhotoUrl,
                contentDescription = "Profile picture",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                error = {
                    // Show first letter of name on primary background - centered
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = name.firstOrNull()?.uppercase() ?: "?",
                            style = MaterialTheme.typography.displayMedium,
                            color = MaterialTheme.colorScheme.onPrimary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // User name with friend icon next to it
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.headlineMedium
            )
            
            // Add/Remove friend icon (only show if authenticated and not own profile)
            if (isAuthenticated && !isOwnProfile) {
                Spacer(modifier = Modifier.width(8.dp))
                
                when {
                    isCheckingFriendship -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    }
                    isFriend -> {
                        IconButton(
                            onClick = onRemoveFriend,
                            enabled = !isRemovingFriend,
                            modifier = Modifier.size(32.dp)
                        ) {
                            if (isRemovingFriend) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.PersonRemove,
                                    contentDescription = "Remove friend",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                    else -> {
                        IconButton(
                            onClick = onAddFriend,
                            enabled = !isAddingFriend,
                            modifier = Modifier.size(32.dp)
                        ) {
                            if (isAddingFriend) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.PersonAdd,
                                    contentDescription = "Add friend",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UserStatsCard(
    stats: com.example.topoclimb.data.UserStats
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Climbing Stats",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatsItem(
                    icon = Icons.Default.Terrain,
                    label = "Trad Level",
                    value = stats.tradLevel,
                    modifier = Modifier.weight(1f)
                )
                
                StatsItem(
                    icon = Icons.Default.Landscape,
                    label = "Bouldering",
                    value = stats.boulderingLevel,
                    modifier = Modifier.weight(1f)
                )
                
                StatsItem(
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    label = "Total Climbed",
                    value = "${stats.totalClimbed}",
                    modifier = Modifier.weight(1f)
                )
            }
            
            // Routes by grade chart
            stats.routesByGrade?.let { routesByGrade ->
                val gradeMap = parseRoutesbyGrade(routesByGrade)
                if (gradeMap.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text(
                        text = "Routes by Grade",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    RoutesByGradeChart(gradeMap)
                }
            }
        }
    }
}

@Composable
fun UserRouteLogCard(
    routeLogWithDetails: RouteLogWithDetails,
    gradingSystem: GradingSystem? = null,
    onClick: () -> Unit = {}
) {
    val log = routeLogWithDetails.log
    val route = routeLogWithDetails.route
    
    val routeColor = parseRouteColor(route?.color)
    
    // Parse date - handles various timestamp formats from API
    val formattedDate = log.createdAt?.let { dateString ->
        try {
            // Handle different timestamp formats
            val cleanedDate = dateString
                .replace(Regex("\\.\\d+Z?$"), "") // Remove microseconds and Z
                .replace("Z", "") // Remove trailing Z if present
            
            val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
            val outputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            val dateTime = LocalDateTime.parse(cleanedDate, inputFormatter)
            dateTime.format(outputFormatter)
        } catch (e: Exception) {
            // Fallback to just taking the date portion
            dateString.take(10)
        }
    } ?: ""
    
    // Shape for type icon container
    val typeIconShape = RoundedCornerShape(8.dp)
    
    // Shape for grade badge: rounded only on right side
    val gradeBadgeShape = RoundedCornerShape(
        topStart = 0.dp,
        topEnd = 8.dp,
        bottomEnd = 8.dp,
        bottomStart = 0.dp
    )
    
    // Get type display name
    val typeDisplayName = when (log.type?.lowercase()) {
        "flash" -> "Flash"
        "work" -> "After-work"
        "view" -> "View"
        else -> log.type?.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() } ?: ""
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Type icon
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(typeIconShape)
                .background(getTypeColor(log.type)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = getTypeIcon(log.type),
                contentDescription = log.type,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
        
        // Date and Type name column (almost glued to icon)
        Column(
            modifier = Modifier.width(80.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            // Date
            Text(
                text = formattedDate,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            // Type name
            Text(
                text = typeDisplayName,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Route name and climbing style column (right aligned)
        Column(
            modifier = Modifier.width(100.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = route?.name ?: "Unknown Route",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                maxLines = 1
            )
            
            // Show way (climbing style)
            log.way?.let { way ->
                Text(
                    text = way.replaceFirstChar { char -> 
                        if (char.isLowerCase()) char.titlecase() else char.toString() 
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // Route thumbnail and grade glued together (no space, no radius for picture)
        Row(
            horizontalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // Route thumbnail (with border, no radius)
            AsyncImage(
                model = route?.thumbnail,
                contentDescription = "Route thumbnail",
                modifier = Modifier
                    .size(50.dp)
                    .border(
                        width = 2.dp,
                        color = routeColor,
                        shape = RectangleShape
                    ),
                contentScale = ContentScale.Crop
            )
            
            // Route grade badge (glued to thumbnail)
            route?.grade?.let { routeGrade ->
                val routeGradeString = GradeUtils.pointsToGrade(routeGrade, gradingSystem) ?: routeGrade.toString()
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .background(
                            color = routeColor,
                            shape = gradeBadgeShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = routeGradeString,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        ),
                        color = Color.White
                    )
                }
            }
        }
    }
}

/**
 * Get icon for climb type
 */
@Composable
private fun getTypeIcon(type: String?) = when (type?.lowercase()) {
    "flash" -> Icons.Default.FlashOn
    "work" -> Icons.Default.Check
    "view" -> Icons.Default.Visibility
    else -> Icons.Default.Check
}

/**
 * Get color for climb type
 */
@Composable
private fun getTypeColor(type: String?): Color = when (type?.lowercase()) {
    "flash" -> MaterialTheme.colorScheme.primary
    "work" -> MaterialTheme.colorScheme.secondary
    "view" -> MaterialTheme.colorScheme.tertiary
    else -> MaterialTheme.colorScheme.secondary
}
