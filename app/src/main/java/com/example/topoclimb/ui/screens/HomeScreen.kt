package com.example.topoclimb.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.topoclimb.R
import com.example.topoclimb.data.Federated
import com.example.topoclimb.data.GradingSystem
import com.example.topoclimb.data.RouteWithMetadata
import com.example.topoclimb.data.Site
import com.example.topoclimb.ui.components.RouteCard
import com.example.topoclimb.ui.components.RouteDetailBottomSheet
import com.example.topoclimb.ui.utils.parseRouteColor
import com.example.topoclimb.utils.GradeUtils
import com.example.topoclimb.viewmodel.CurrentEventWithSite
import com.example.topoclimb.viewmodel.FavoriteRoutesViewModel
import com.example.topoclimb.viewmodel.FriendLogWithDetails
import com.example.topoclimb.viewmodel.FriendsViewModel
import com.example.topoclimb.viewmodel.HomeViewModel
import com.example.topoclimb.viewmodel.SitesViewModel
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onSiteClick: (String, Int) -> Unit,
    onAllFavoriteSitesClick: () -> Unit,
    onAllFavoriteRoutesClick: () -> Unit,
    onAllSitesClick: () -> Unit,
    onFriendsClick: () -> Unit = {},
    onUserClick: (Int, String) -> Unit = { _, _ -> },
    homeViewModel: HomeViewModel = viewModel(),
    sitesViewModel: SitesViewModel = viewModel(),
    favoriteRoutesViewModel: FavoriteRoutesViewModel = viewModel(),
    friendsViewModel: FriendsViewModel = viewModel()
) {
    val homeUiState by homeViewModel.uiState.collectAsState()
    val sitesUiState by sitesViewModel.uiState.collectAsState()
    val favoriteRoutesUiState by favoriteRoutesViewModel.uiState.collectAsState()
    
    // Derive favorite sites from sitesUiState
    val favoriteSites = remember(sitesUiState.favoriteSiteId, sitesUiState.sites) {
        sitesUiState.sites.filter { it.data.id == sitesUiState.favoriteSiteId }
    }
    
    // Update favorite sites in home view model only when they change
    LaunchedEffect(favoriteSites) {
        homeViewModel.updateFavoriteSites(favoriteSites)
    }
    
    LaunchedEffect(favoriteRoutesUiState.favoriteRoutes) {
        homeViewModel.updateFavoriteRoutes(favoriteRoutesUiState.favoriteRoutes)
    }
    
    // Build grading system map for routes
    val gradingSystemMap = remember(sitesUiState.sites) {
        sitesUiState.sites.associate { it.data.id to it.data.gradingSystem }
    }
    
    // State for route bottom sheet
    var showRouteBottomSheet by remember { mutableStateOf(false) }
    var selectedRoute by remember { mutableStateOf<RouteWithMetadata?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(0, 0, 0, 0),
                title = { 
                    Row {
                        Text(
                            text = "Hello ",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        homeUiState.userName?.let { name ->
                            Text(
                                text = name,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = homeUiState.isRefreshing,
            onRefresh = { homeViewModel.refresh() },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Current Events (Contests)
                if (homeUiState.currentEvents.isNotEmpty()) {
                    item {
                        Text(
                            text = "Ongoing Contests",
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                    
                    items(homeUiState.currentEvents) { eventWithSite ->
                        CurrentEventCard(
                            event = eventWithSite,
                            onClick = {
                                eventWithSite.siteId?.let { siteId ->
                                    onSiteClick(eventWithSite.backendId, siteId)
                                }
                            }
                        )
                    }
                }
                
                // Friend Activity
                if (homeUiState.friendLogs.isNotEmpty()) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = if (homeUiState.currentEvents.isNotEmpty()) 12.dp else 0.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Friend Activity",
                                style = MaterialTheme.typography.titleLarge
                            )
                            TextButton(onClick = onFriendsClick) {
                                Text("Your friends")
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                    
                    items(homeUiState.friendLogs.take(10)) { friendLog ->
                        FriendLogCard(
                            friendLog = friendLog,
                            gradingSystem = friendLog.siteId?.let { gradingSystemMap[it] },
                            onClick = {
                                onUserClick(friendLog.friendId, friendLog.backendId)
                            }
                        )
                    }
                } else if (homeUiState.isLoadingFriendLogs) {
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
                } else if (homeUiState.currentEvents.isEmpty()) {
                    item {
                        EmptyNewsCard()
                    }
                }
                
                // Favorite Sites (max 3)
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Favorite Sites",
                            style = MaterialTheme.typography.titleLarge
                        )
                        TextButton(onClick = onAllSitesClick) {
                            Text("See all")
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
                
                if (homeUiState.favoriteSites.isNotEmpty()) {
                    items(homeUiState.favoriteSites.take(3)) { federatedSite ->
                        CompactSiteCard(
                            site = federatedSite.data,
                            backendName = federatedSite.backend.backendName,
                            onClick = { onSiteClick(federatedSite.backend.backendId, federatedSite.data.id) }
                        )
                    }
                    
                    if (homeUiState.favoriteSites.size > 3) {
                        item {
                            TextButton(
                                onClick = onAllFavoriteSitesClick,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("See all favorite sites")
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = null
                                )
                            }
                        }
                    }
                } else {
                    item {
                        EmptyFavoriteSitesCard(onAllSitesClick = onAllSitesClick)
                    }
                }
                
                // Favorite Routes (max 3)
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Favorite Routes",
                            style = MaterialTheme.typography.titleLarge
                        )
                        if (homeUiState.favoriteRoutes.isNotEmpty()) {
                            TextButton(onClick = onAllFavoriteRoutesClick) {
                                Text("More")
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
                
                if (homeUiState.favoriteRoutes.isNotEmpty()) {
                    items(homeUiState.favoriteRoutes.take(3)) { routeWithMetadata ->
                        val gradingSystem = gradingSystemMap[routeWithMetadata.siteId]
                        val gradeString = routeWithMetadata.grade?.let {
                            GradeUtils.pointsToGrade(it, gradingSystem)
                        }
                        
                        RouteCard(
                            thumbnail = routeWithMetadata.thumbnail,
                            grade = gradeString,
                            color = routeWithMetadata.color,
                            name = routeWithMetadata.name,
                            localId = routeWithMetadata.siteName,
                            numberLogs = routeWithMetadata.numberLogs,
                            numberComments = routeWithMetadata.numberComments,
                            onClick = {
                                selectedRoute = routeWithMetadata
                                showRouteBottomSheet = true
                            }
                        )
                    }
                } else {
                    item {
                        EmptyFavoriteRoutesCard()
                    }
                }
                
                // New Routes section
                if (homeUiState.newRoutes.isNotEmpty()) {
                    item {
                        Text(
                            text = "New Routes",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(top = 12.dp)
                        )
                    }
                    
                    items(homeUiState.newRoutes.take(5)) { routeWithMetadata ->
                        val gradingSystem = gradingSystemMap[routeWithMetadata.siteId]
                        val gradeString = routeWithMetadata.grade?.let {
                            GradeUtils.pointsToGrade(it, gradingSystem)
                        }
                        
                        RouteCard(
                            thumbnail = routeWithMetadata.thumbnail,
                            grade = gradeString,
                            color = routeWithMetadata.color,
                            name = routeWithMetadata.name,
                            localId = routeWithMetadata.siteName ?: "New",
                            numberLogs = routeWithMetadata.numberLogs,
                            numberComments = routeWithMetadata.numberComments,
                            onClick = {
                                selectedRoute = routeWithMetadata
                                showRouteBottomSheet = true
                            }
                        )
                    }
                }
                
                // Add some bottom spacing
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
    
    // Bottom Sheet for Route Details
    if (showRouteBottomSheet && selectedRoute != null) {
        val gradingSystem = gradingSystemMap[selectedRoute!!.siteId]
        RouteDetailBottomSheet(
            routeWithMetadata = selectedRoute!!,
            onDismiss = { showRouteBottomSheet = false },
            gradingSystem = gradingSystem,
            onStartLogging = null,
            favoriteRoutesViewModel = favoriteRoutesViewModel,
            friendsViewModel = friendsViewModel
        )
    }
}

@Composable
private fun CurrentEventCard(
    event: CurrentEventWithSite,
    onClick: () -> Unit
) {
    val contest = event.contest
    
    // Pulsing animation for the ongoing indicator
    val infiniteTransition = rememberInfiniteTransition(label = "ongoing_pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 2.dp,
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Animated indicator dot
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = alpha),
                        shape = CircleShape
                    )
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Contest info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = contest.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                event.siteName?.let { siteName ->
                    Text(
                        text = siteName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            
            // Chevron to access site
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Go to site",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun FriendLogCard(
    friendLog: FriendLogWithDetails,
    gradingSystem: GradingSystem?,
    onClick: () -> Unit
) {
    val routeColor = parseRouteColor(friendLog.routeColor)
    
    // Parse date - handles various timestamp formats from API
    val formattedDate = friendLog.logCreatedAt?.let { dateString ->
        try {
            // Handle different timestamp formats by removing microseconds
            val cleanedDate = dateString
                .replace(Regex("\\.\\d+Z?$"), "") // Remove microseconds and optional trailing Z
                .trimEnd('Z') // Remove any remaining trailing Z
            
            val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
            val outputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            val dateTime = LocalDateTime.parse(cleanedDate, inputFormatter)
            dateTime.format(outputFormatter)
        } catch (e: Exception) {
            // Fallback: extract date portion (first 10 characters: YYYY-MM-DD)
            if (dateString.length >= 10) dateString.substring(0, 10) else dateString
        }
    } ?: ""
    
    // Shape for type icon container - same rounded corners as grade badge
    val typeIconShape = RoundedCornerShape(
        topStart = 8.dp,
        topEnd = 0.dp,
        bottomEnd = 0.dp,
        bottomStart = 8.dp
    )
    
    // Shape for grade badge: rounded only on right side
    val gradeBadgeShape = RoundedCornerShape(
        topStart = 0.dp,
        topEnd = 8.dp,
        bottomEnd = 8.dp,
        bottomStart = 0.dp
    )
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Type icon - same height as picture+grade section (50.dp) with neutral background
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(typeIconShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = getTypeIcon(friendLog.logType),
                contentDescription = friendLog.logType,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(28.dp)
            )
        }
        
        // Date and Friend name column
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
            
            // Friend name - neutral color instead of primary
            Text(
                text = friendLog.friendName,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Route name column (right aligned)
        Column(
            modifier = Modifier.width(100.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = friendLog.routeName ?: "Route",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            // Show way (climbing style) - like "bouldering", "traditional", etc.
            friendLog.logWay?.let { way ->
                Text(
                    text = way.replaceFirstChar { char -> 
                        if (char.isLowerCase()) char.titlecase() else char.toString() 
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // Route thumbnail and grade glued together
        Row(
            horizontalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // Route thumbnail (with border, no radius)
            AsyncImage(
                model = friendLog.routeThumbnail,
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
            friendLog.routeGrade?.let { routeGrade ->
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

@Composable
private fun CompactSiteCard(
    site: Site,
    backendName: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Banner background
            site.banner?.let { bannerUrl ->
                AsyncImage(
                    model = bannerUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                
                // Gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.4f)
                                )
                            )
                        )
                )
            }
            
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Logo
                site.profilePicture?.let { logoUrl ->
                    AsyncImage(
                        model = logoUrl,
                        contentDescription = "Site logo",
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = site.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = backendName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun EmptyNewsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Newspaper,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "No News Yet",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Add friends to see their climbing activity",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EmptyFavoriteSitesCard(onAllSitesClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onAllSitesClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.routes_empty_state),
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "No Favorite Site",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Tap the star on a site to add it to favorites",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun EmptyFavoriteRoutesCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "No Favorite Routes",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Tap the star on a route to add it here",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
