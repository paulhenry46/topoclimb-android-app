package com.example.topoclimb.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.topoclimb.data.Contest
import com.example.topoclimb.data.ContestCategory
import com.example.topoclimb.data.ContestRankEntry
import com.example.topoclimb.data.ContestStep
import com.example.topoclimb.viewmodel.ContestDetailViewModel
import com.example.topoclimb.viewmodel.StepState
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContestDetailScreen(
    backendId: String,
    contestId: Int,
    contest: Contest,
    onBackClick: () -> Unit,
    onStepRoutesClick: (Int, List<Int>) -> Unit = { _, _ -> },
    viewModel: ContestDetailViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(backendId, contestId) {
        viewModel.loadContestDetails(backendId, contestId, contest)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(0, 0, 0, 0),
                title = { Text(contest.name) },
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
                        Button(onClick = { viewModel.loadContestDetails(backendId, contestId, contest) }) {
                            Text("Retry")
                        }
                    }
                }
            }
            else -> {
                PullToRefreshBox(
                    isRefreshing = uiState.isRefreshing,
                    onRefresh = { viewModel.refreshContestDetails() },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Contest description
                        item {
                            contest.description?.let { description ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                            text = description,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                            }
                        }
                        
                        // Categories section
                        if (uiState.categories.isNotEmpty()) {
                            item {
                                Text(
                                    text = "Categories",
                                    style = MaterialTheme.typography.titleLarge,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                            
                            // Category list with registration
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                ) {
                                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                                        uiState.categories.forEach { category ->
                                            CategoryListItem(
                                                category = category,
                                                isRegistered = uiState.userCategoryIds.contains(category.id),
                                                onToggleRegistration = { 
                                                    if (!category.autoAssign) {
                                                        viewModel.toggleCategoryRegistration(category.id)
                                                    }
                                                }
                                            )
                                            if (category != uiState.categories.last()) {
                                                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        
                        // Contest Steps
                        if (uiState.steps.isNotEmpty()) {
                            item {
                                Text(
                                    text = "Steps",
                                    style = MaterialTheme.typography.titleLarge,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                            
                            items(uiState.steps) { step ->
                                ContestStepCard(
                                    step = step,
                                    state = viewModel.getStepState(step),
                                    isSelected = uiState.selectedStepId == step.id,
                                    onStepClick = { 
                                        if (uiState.selectedStepId == step.id) {
                                            viewModel.selectStep(null)
                                        } else {
                                            viewModel.selectStep(step.id)
                                        }
                                    },
                                    onViewRoutesClick = { onStepRoutesClick(step.id, step.routes) }
                                )
                            }
                        }
                        
                        // Show step ranking if a step is selected
                        if (uiState.selectedStepId != null && uiState.selectedStepRanking.isNotEmpty()) {
                            item {
                                val selectedStep = uiState.steps.find { it.id == uiState.selectedStepId }
                                val selectedCategory = uiState.categories.find { it.id == uiState.selectedCategoryId }
                                val categoryText = if (selectedCategory != null) " - ${selectedCategory.name}" else ""
                                Text(
                                    text = "Ranking: ${selectedStep?.name ?: "Step"}$categoryText",
                                    style = MaterialTheme.typography.titleLarge,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                            
                            items(uiState.selectedStepRanking) { entry ->
                                RankingEntryCard(entry = entry)
                            }
                        }
                        
                        // Global Ranking
                        if (uiState.globalRanking.isNotEmpty() && uiState.selectedStepId == null) {
                            item {
                                val selectedCategory = uiState.categories.find { it.id == uiState.selectedCategoryId }
                                val categoryText = if (selectedCategory != null) " - ${selectedCategory.name}" else ""
                                Text(
                                    text = "Global Ranking$categoryText",
                                    style = MaterialTheme.typography.titleLarge,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                            
                            // Category filter chips (if categories exist)
                            if (uiState.categories.isNotEmpty()) {
                                item {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        // "All" chip
                                        FilterChip(
                                            selected = uiState.selectedCategoryId == null,
                                            onClick = { viewModel.selectCategory(null) },
                                            label = { Text("All") },
                                            leadingIcon = if (uiState.selectedCategoryId == null) {
                                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                                            } else null
                                        )
                                        
                                        // Category chips
                                        uiState.categories.forEach { category ->
                                            FilterChip(
                                                selected = uiState.selectedCategoryId == category.id,
                                                onClick = { viewModel.selectCategory(category.id) },
                                                label = { Text(category.name) },
                                                leadingIcon = if (uiState.selectedCategoryId == category.id) {
                                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                                                } else null
                                            )
                                        }
                                    }
                                }
                            }
                            
                            items(uiState.globalRanking) { entry ->
                                RankingEntryCard(entry = entry)
                            }
                        }
                        
                        // Empty state
                        if (uiState.steps.isEmpty() && uiState.globalRanking.isEmpty()) {
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
                                            text = "No data available for this contest.",
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
fun ContestStepCard(
    step: ContestStep,
    state: StepState,
    isSelected: Boolean,
    onStepClick: () -> Unit,
    onViewRoutesClick: () -> Unit
) {
    val (stateColor, stateText, stateIcon) = when (state) {
        StepState.UPCOMING -> Triple(
            MaterialTheme.colorScheme.primary,
            "Upcoming",
            Icons.Default.Schedule
        )
        StepState.ACTIVE -> Triple(
            MaterialTheme.colorScheme.tertiary,
            "Active",
            Icons.Default.PlayArrow
        )
        StepState.ENDED -> Triple(
            MaterialTheme.colorScheme.onSurfaceVariant,
            "Ended",
            Icons.Default.CheckCircle
        )
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onStepClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (state == StepState.ENDED)
                MaterialTheme.colorScheme.surfaceVariant
            else 
                MaterialTheme.colorScheme.surface
        ),
        border = if (isSelected)
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        else
            null
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = stateColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = step.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                
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
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Time information
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
                
                val dateText = remember(step.startTime, step.endTime) {
                    try {
                        val formatter = DateTimeFormatter.ofPattern("MMM dd, HH:mm")
                            .withZone(ZoneId.systemDefault())
                        val startTime = Instant.parse(step.startTime)
                        val endTime = Instant.parse(step.endTime)
                        "${formatter.format(startTime)} - ${formatter.format(endTime)}"
                    } catch (e: Exception) {
                        "Time unavailable"
                    }
                }
                
                Text(
                    text = dateText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Routes count
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Route,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${step.routes.size} routes",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun RankingEntryCard(entry: ContestRankEntry) {
    val medalIcon = when (entry.rank) {
        1 -> Icons.Default.EmojiEvents to MaterialTheme.colorScheme.primary
        2 -> Icons.Default.EmojiEvents to MaterialTheme.colorScheme.tertiary
        3 -> Icons.Default.EmojiEvents to MaterialTheme.colorScheme.secondary
        else -> null
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = if (medalIcon != null) medalIcon.second.copy(alpha = 0.15f) 
                        else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (medalIcon != null) {
                    Icon(
                        imageVector = medalIcon.first,
                        contentDescription = null,
                        tint = medalIcon.second,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text(
                        text = entry.rank.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // User info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.userName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${entry.routesCount} routes",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${entry.totalPoints} pts",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryListItem(
    category: ContestCategory,
    isRegistered: Boolean,
    onToggleRegistration: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !category.autoAssign) { onToggleRegistration() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                if (category.autoAssign) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "Auto",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            
            if (!category.criteria.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = category.criteria,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Show gender or age restrictions
            val restrictions = mutableListOf<String>()
            category.gender?.takeIf { it.isNotBlank() }?.let { restrictions.add(it.replaceFirstChar { c -> c.uppercase() }) }
            if (category.minAge != null || category.maxAge != null) {
                val ageRange = when {
                    category.minAge != null && category.maxAge != null -> "${category.minAge}-${category.maxAge} years"
                    category.minAge != null -> "${category.minAge}+ years"
                    category.maxAge != null -> "Up to ${category.maxAge} years"
                    else -> null
                }
                ageRange?.let { restrictions.add(it) }
            }
            
            if (restrictions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = restrictions.joinToString(" • "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        if (isRegistered) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Registered",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        } else if (!category.autoAssign) {
            Icon(
                imageVector = Icons.Default.Circle,
                contentDescription = "Not registered",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
