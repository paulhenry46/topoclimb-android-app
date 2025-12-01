package com.example.topoclimb.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import com.example.topoclimb.data.GradingSystem
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
                                    gradingSystem = null // TODO: Pass grading system if available
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
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            SubcomposeAsyncImage(
                model = profilePhotoUrl,
                contentDescription = "Profile picture",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                error = {
                    Icon(
                        imageVector = Icons.Default.Face,
                        contentDescription = "Default profile",
                        modifier = Modifier.size(60.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // User name
        Text(
            text = name,
            style = MaterialTheme.typography.headlineMedium
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Add/Remove friend button (only show if authenticated and not own profile)
        if (isAuthenticated && !isOwnProfile) {
            when {
                isCheckingFriendship -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                }
                isFriend -> {
                    Button(
                        onClick = onRemoveFriend,
                        enabled = !isRemovingFriend,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    ) {
                        if (isRemovingFriend) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.PersonRemove,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Remove Friend")
                    }
                }
                else -> {
                    Button(
                        onClick = onAddFriend,
                        enabled = !isAddingFriend
                    ) {
                        if (isAddingFriend) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.PersonAdd,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Friend")
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
    gradingSystem: GradingSystem? = null
) {
    val log = routeLogWithDetails.log
    val route = routeLogWithDetails.route
    
    val routeColor = parseRouteColor(route?.color)
    
    // Parse date
    val formattedDate = log.createdAt?.let { dateString ->
        try {
            val inputFormatter = DateTimeFormatter.ISO_DATE_TIME
            val outputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            val dateTime = LocalDateTime.parse(dateString.replace(".000000Z", ""))
            dateTime.format(outputFormatter)
        } catch (e: Exception) {
            dateString.take(10)
        }
    } ?: ""
    
    // Convert grade to string
    val gradeString = GradeUtils.pointsToGrade(log.grade, gradingSystem) ?: log.grade.toString()
    
    // Shape for type icon container
    val typeIconShape = RoundedCornerShape(8.dp)
    
    // Shape for thumbnail: rounded on left, square on right for continuity with grade
    val thumbnailShape = RoundedCornerShape(
        topStart = 8.dp,
        topEnd = 0.dp,
        bottomEnd = 0.dp,
        bottomStart = 8.dp
    )
    
    // Shape for grade badge: square on left, rounded on right for continuity with thumbnail
    val gradeBadgeShape = RoundedCornerShape(
        topStart = 0.dp,
        topEnd = 8.dp,
        bottomEnd = 8.dp,
        bottomStart = 0.dp
    )
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Type icon column with date and felt grade
        Column(
            modifier = Modifier.width(60.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
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
            
            // Date
            Text(
                text = formattedDate,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            // Felt grade (cotation felt)
            Text(
                text = gradeString,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        // Route thumbnail
        AsyncImage(
            model = route?.thumbnail,
            contentDescription = "Route thumbnail",
            modifier = Modifier
                .size(60.dp)
                .border(
                    width = 3.dp,
                    color = routeColor,
                    shape = thumbnailShape
                )
                .clip(thumbnailShape),
            contentScale = ContentScale.Crop
        )
        
        // Route grade badge
        route?.grade?.let { routeGrade ->
            val routeGradeString = GradeUtils.pointsToGrade(routeGrade, gradingSystem) ?: routeGrade.toString()
            Box(
                modifier = Modifier
                    .size(60.dp)
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
                        fontSize = 16.sp
                    ),
                    color = Color.White
                )
            }
        }
        
        Spacer(modifier = Modifier.width(4.dp))
        
        // Route name
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = route?.name ?: "Unknown Route",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                )
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
    }
}

/**
 * Get icon for climb type
 */
@Composable
private fun getTypeIcon(type: String?) = when (type?.lowercase()) {
    "flash" -> Icons.Default.FlashOn
    "work" -> Icons.Default.Work
    "view" -> Icons.Default.Visibility
    else -> Icons.Default.Work
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
