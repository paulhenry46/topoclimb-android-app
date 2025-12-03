package com.example.topoclimb.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.topoclimb.R
import com.example.topoclimb.data.Contest
import com.example.topoclimb.data.Federated
import com.example.topoclimb.data.GradingSystem
import com.example.topoclimb.data.RouteWithMetadata
import com.example.topoclimb.data.Site
import com.example.topoclimb.ui.components.RouteCard
import com.example.topoclimb.ui.components.RouteDetailBottomSheet
import com.example.topoclimb.utils.GradeUtils
import com.example.topoclimb.viewmodel.CurrentEventWithSite
import com.example.topoclimb.viewmodel.FavoriteRoutesViewModel
import com.example.topoclimb.viewmodel.FriendLogWithDetails
import com.example.topoclimb.viewmodel.FriendsViewModel
import com.example.topoclimb.viewmodel.HomeViewModel
import com.example.topoclimb.viewmodel.SitesViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onSiteClick: (String, Int) -> Unit,
    onAllFavoriteSitesClick: () -> Unit,
    onAllFavoriteRoutesClick: () -> Unit,
    onAllSitesClick: () -> Unit,
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
    
    val greeting = if (homeUiState.userName != null) {
        "Hello ${homeUiState.userName}"
    } else {
        "Hello"
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(0, 0, 0, 0),
                title = { 
                    Text(
                        text = greeting,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    ) 
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
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // NEWS SECTION
                item {
                    Text(
                        text = "News",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                // Current Events (Contests)
                if (homeUiState.currentEvents.isNotEmpty()) {
                    item {
                        Text(
                            text = "Ongoing Contests",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    
                    item {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
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
                    }
                }
                
                // Friend Activity
                if (homeUiState.friendLogs.isNotEmpty()) {
                    item {
                        Text(
                            text = "Friend Activity",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(top = if (homeUiState.currentEvents.isNotEmpty()) 8.dp else 0.dp, bottom = 8.dp)
                        )
                    }
                    
                    items(homeUiState.friendLogs.take(10)) { friendLog ->
                        FriendLogCard(
                            friendLog = friendLog,
                            gradingSystem = friendLog.siteId?.let { gradingSystemMap[it] },
                            onClick = {
                                // Could navigate to user profile or route
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
                
                // CLIMB SECTION
                item {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                }
                
                item {
                    Text(
                        text = "Climb",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                // Favorite Sites (max 3)
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Favorite Sites",
                            style = MaterialTheme.typography.titleMedium
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
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Favorite Routes",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(top = 8.dp)
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
                        Spacer(modifier = Modifier.height(8.dp))
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
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
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
                        Spacer(modifier = Modifier.height(8.dp))
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
    
    // Calculate days remaining
    val daysRemaining = try {
        contest.endDate?.let { endDateStr ->
            val endDate = LocalDate.parse(endDateStr.substring(0, 10))
            val today = LocalDate.now()
            ChronoUnit.DAYS.between(today, endDate).toInt()
        }
    } catch (e: Exception) {
        null
    }
    
    Card(
        modifier = Modifier
            .width(280.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
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
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Status badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Ongoing",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
                
                // Days remaining
                daysRemaining?.let { days ->
                    if (days > 0) {
                        Text(
                            text = "$days days left",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    } else if (days == 0) {
                        Text(
                            text = "Last day!",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FriendLogCard(
    friendLog: FriendLogWithDetails,
    gradingSystem: GradingSystem?,
    onClick: () -> Unit
) {
    val gradeString = friendLog.routeGrade?.let {
        GradeUtils.pointsToGrade(it, gradingSystem)
    }
    
    // Format the time
    val timeAgo = friendLog.logCreatedAt?.let { dateStr ->
        try {
            val createdDate = LocalDate.parse(dateStr.substring(0, 10))
            val today = LocalDate.now()
            val days = ChronoUnit.DAYS.between(createdDate, today).toInt()
            when {
                days == 0 -> "Today"
                days == 1 -> "Yesterday"
                days < 7 -> "$days days ago"
                else -> "${days / 7} weeks ago"
            }
        } catch (e: Exception) {
            null
        }
    }
    
    val logTypeDisplay = when (friendLog.logType?.lowercase()) {
        "flash" -> "flashed"
        "work" -> "climbed"
        "view" -> "tried"
        else -> "logged"
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Friend avatar
            AsyncImage(
                model = friendLog.friendPhotoUrl,
                contentDescription = "Friend photo",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row {
                    Text(
                        text = friendLog.friendName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = " $logTypeDisplay",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Text(
                    text = friendLog.routeName ?: "Route",
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Row {
                    gradeString?.let { grade ->
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = grade,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    
                    timeAgo?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // Route thumbnail
            friendLog.routeThumbnail?.let { thumbnail ->
                AsyncImage(
                    model = thumbnail,
                    contentDescription = "Route thumbnail",
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
    
    Spacer(modifier = Modifier.height(8.dp))
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
    
    Spacer(modifier = Modifier.height(8.dp))
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
