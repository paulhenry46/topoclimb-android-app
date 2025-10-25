package com.example.topoclimb.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.topoclimb.data.Area
import com.example.topoclimb.data.Contest
import com.example.topoclimb.viewmodel.SiteDetailViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SiteDetailScreen(
    backendId: String,
    siteId: Int,
    onBackClick: () -> Unit,
    onAreaClick: (String, Int) -> Unit = { _, _ -> },
    viewModel: SiteDetailViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(backendId, siteId) {
        viewModel.loadSiteDetails(backendId, siteId)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.site?.data?.name ?: "Site Details") },
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
                        Button(onClick = { viewModel.loadSiteDetails(backendId, siteId) }) {
                            Text("Retry")
                        }
                    }
                }
            }
            uiState.site != null -> {
                PullToRefreshBox(
                    isRefreshing = uiState.isRefreshing,
                    onRefresh = { viewModel.refreshSiteDetails() },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                    // Site header with banner
                    item {
                        uiState.site?.let { federatedSite ->
                            val site = federatedSite.data
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                            ) {
                                Column {
                                    site.banner?.let { bannerUrl ->
                                        AsyncImage(
                                            model = bannerUrl,
                                            contentDescription = "Site banner",
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(200.dp),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                            text = site.name,
                                            style = MaterialTheme.typography.headlineMedium
                                        )
                                        site.description?.let { description ->
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = description,
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    // Contact Information Card
                    item {
                        uiState.site?.let { federatedSite ->
                            val site = federatedSite.data
                            val hasContactInfo = site.email != null || site.phone != null || 
                                                site.website != null || site.address != null || 
                                                site.coordinates != null
                            
                            if (hasContactInfo) {
                                var isExpanded by remember { mutableStateOf(false) }
                                
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { isExpanded = !isExpanded },
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Contact Information",
                                                style = MaterialTheme.typography.titleLarge
                                            )
                                            Icon(
                                                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                                contentDescription = if (isExpanded) "Collapse" else "Expand",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        
                                        AnimatedVisibility(visible = isExpanded) {
                                            Column(modifier = Modifier.padding(top = 12.dp)) {
                                                site.email?.let { email ->
                                                    ContactInfoRow(
                                                        icon = Icons.Default.Email,
                                                        label = "Email",
                                                        value = email
                                                    )
                                                }
                                                
                                                site.phone?.let { phone ->
                                                    ContactInfoRow(
                                                        icon = Icons.Default.Phone,
                                                        label = "Phone",
                                                        value = phone
                                                    )
                                                }
                                                
                                                site.website?.let { website ->
                                                    ContactInfoRow(
                                                        icon = Icons.Default.Language,
                                                        label = "Website",
                                                        value = website
                                                    )
                                                }
                                                
                                                site.address?.let { address ->
                                                    ContactInfoRow(
                                                        icon = Icons.Default.LocationOn,
                                                        label = "Address",
                                                        value = address
                                                    )
                                                }
                                                
                                                site.coordinates?.let { coordinates ->
                                                    ContactInfoRow(
                                                        icon = Icons.Default.Place,
                                                        label = "Coordinates",
                                                        value = coordinates
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    // Areas section
                    if (uiState.areas.isNotEmpty()) {
                        item {
                            Text(
                                text = "Areas",
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                        items(uiState.areas) { federatedArea ->
                            SiteAreaItem(
                                area = federatedArea.data,
                                onSeeTopoClick = { onAreaClick(federatedArea.backend.backendId, federatedArea.data.id) }
                            )
                        }
                    }
                    
                    // Contests section
                    if (uiState.contests.isNotEmpty()) {
                        item {
                            Text(
                                text = "Contests",
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                        items(uiState.contests) { federatedContest ->
                            ContestItem(federatedContest.data)
                        }
                    }
                    
                    // Empty state
                    if (uiState.areas.isEmpty() && uiState.contests.isEmpty()) {
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
                                        text = "No areas or contests available for this site.",
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
    }
}

@Composable
fun SiteAreaItem(
    area: Area,
    onSeeTopoClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = area.name,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                
                // Area type badge
                area.type?.let { type ->
                    val (badgeText, badgeColor) = when (type.lowercase()) {
                        "bouldering", "boulder" -> "Boulder" to MaterialTheme.colorScheme.primary
                        "traditional", "trad", "sport" -> "Trad" to MaterialTheme.colorScheme.secondary
                        else -> type.replaceFirstChar { it.uppercase() } to MaterialTheme.colorScheme.tertiary
                    }
                    
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = badgeColor,
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text(
                            text = badgeText,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onSeeTopoClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("See Topo")
            }
        }
    }
}

@Composable
fun ContestItem(contest: Contest) {
    // Determine contest state
    val contestState = try {
        val now = LocalDate.now()
        val startDate = contest.startDate?.let { LocalDate.parse(it.substring(0, 10)) }
        val endDate = contest.endDate?.let { LocalDate.parse(it.substring(0, 10)) }
        
        when {
            startDate == null && endDate == null -> ContestState.UNKNOWN
            startDate != null && now.isBefore(startDate) -> ContestState.UPCOMING
            endDate != null && now.isAfter(endDate) -> ContestState.PAST
            else -> ContestState.ONGOING
        }
    } catch (e: Exception) {
        ContestState.UNKNOWN
    }
    
    val (stateColor, stateText, stateIcon) = when (contestState) {
        ContestState.UPCOMING -> Triple(
            MaterialTheme.colorScheme.primary,
            "Upcoming",
            Icons.Default.Schedule
        )
        ContestState.ONGOING -> Triple(
            MaterialTheme.colorScheme.tertiary,
            "Ongoing",
            Icons.Default.PlayArrow
        )
        ContestState.PAST -> Triple(
            MaterialTheme.colorScheme.onSurfaceVariant,
            "Ended",
            Icons.Default.CheckCircle
        )
        ContestState.UNKNOWN -> Triple(
            MaterialTheme.colorScheme.onSurfaceVariant,
            "",
            Icons.Default.Event
        )
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (contestState == ContestState.PAST) 
                MaterialTheme.colorScheme.surfaceVariant 
            else 
                MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = stateColor,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = contest.name,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                    fontWeight = FontWeight.SemiBold
                )
                
                if (stateText.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = stateColor.copy(alpha = 0.15f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = stateIcon,
                                contentDescription = null,
                                tint = stateColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = stateText,
                                style = MaterialTheme.typography.labelSmall,
                                color = stateColor
                            )
                        }
                    }
                }
            }
            
            contest.description?.let { description ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            if (contest.startDate != null || contest.endDate != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    val dateFormatter = try {
                        DateTimeFormatter.ofPattern("MMM dd, yyyy")
                    } catch (e: Exception) {
                        null
                    }
                    
                    val dateText = buildString {
                        contest.startDate?.let { startDateStr ->
                            try {
                                val startDate = LocalDate.parse(startDateStr.substring(0, 10))
                                append(dateFormatter?.format(startDate) ?: startDateStr.substring(0, 10))
                            } catch (e: Exception) {
                                append(startDateStr.substring(0, 10))
                            }
                        }
                        
                        if (contest.startDate != null && contest.endDate != null) {
                            append(" - ")
                        }
                        
                        contest.endDate?.let { endDateStr ->
                            try {
                                val endDate = LocalDate.parse(endDateStr.substring(0, 10))
                                append(dateFormatter?.format(endDate) ?: endDateStr.substring(0, 10))
                            } catch (e: Exception) {
                                append(endDateStr.substring(0, 10))
                            }
                        }
                    }
                    
                    Text(
                        text = dateText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

enum class ContestState {
    UPCOMING, ONGOING, PAST, UNKNOWN
}

@Composable
fun ContactInfoRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
