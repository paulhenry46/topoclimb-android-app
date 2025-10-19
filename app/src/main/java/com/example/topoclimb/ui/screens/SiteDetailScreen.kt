package com.example.topoclimb.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.topoclimb.data.Area
import com.example.topoclimb.data.Contest
import com.example.topoclimb.data.Federated
import com.example.topoclimb.viewmodel.SiteDetailViewModel

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
                },
                actions = {
                    IconButton(onClick = { viewModel.refreshSiteDetails() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh"
                        )
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
                Box(
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
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "Source: ${federatedSite.backend.backendName}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
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
                                backendName = federatedArea.backend.backendName,
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
                    
                    // Show loading indicator when refreshing
                    if (uiState.isRefreshing) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SiteAreaItem(
    area: Area,
    backendName: String,
    onSeeTopoClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = area.name,
                style = MaterialTheme.typography.titleMedium
            )
            area.description?.let { description ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            if (area.latitude != null && area.longitude != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Location: ${area.latitude}, ${area.longitude}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Source: $backendName",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = contest.name,
                style = MaterialTheme.typography.titleMedium
            )
            contest.description?.let { description ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            if (contest.startDate != null || contest.endDate != null) {
                Spacer(modifier = Modifier.height(4.dp))
                val dateInfo = buildString {
                    contest.startDate?.let { append("Start: $it") }
                    if (contest.startDate != null && contest.endDate != null) {
                        append(" • ")
                    }
                    contest.endDate?.let { append("End: $it") }
                }
                Text(
                    text = dateInfo,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}
