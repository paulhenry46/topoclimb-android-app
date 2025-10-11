package com.example.topoclimb.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.topoclimb.data.Route
import com.example.topoclimb.viewmodel.RoutesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutesScreen(
    siteId: Int? = null,
    onRouteClick: (Int) -> Unit,
    viewModel: RoutesViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showFilterDialog by remember { mutableStateOf(false) }
    
    LaunchedEffect(siteId) {
        if (siteId != null) {
            viewModel.loadRoutes(siteId)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Routes") },
                actions = {
                    IconButton(onClick = { showFilterDialog = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Filter")
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
                        Button(onClick = { viewModel.loadRoutes(siteId) }) {
                            Text("Retry")
                        }
                    }
                }
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    // Show active filters
                    if (uiState.selectedGrade != null || uiState.selectedType != null) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Filters active: ${uiState.selectedGrade ?: ""} ${uiState.selectedType ?: ""}")
                                TextButton(onClick = { viewModel.clearFilters() }) {
                                    Text("Clear")
                                }
                            }
                        }
                    }
                    
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.filteredRoutes) { route ->
                            RouteItem(
                                route = route,
                                onClick = { onRouteClick(route.id) }
                            )
                        }
                    }
                }
            }
        }
    }
    
    if (showFilterDialog) {
        FilterDialog(
            currentGrade = uiState.selectedGrade,
            currentType = uiState.selectedType,
            onGradeSelected = { viewModel.filterByGrade(it) },
            onTypeSelected = { viewModel.filterByType(it) },
            onDismiss = { showFilterDialog = false }
        )
    }
}

@Composable
fun RouteItem(
    route: Route,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = route.name,
                    style = MaterialTheme.typography.titleMedium
                )
                route.grade?.let { grade ->
                    Text(
                        text = grade,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                route.type?.let { type ->
                    AssistChip(
                        onClick = { },
                        label = { Text(type) }
                    )
                }
                route.height?.let { height ->
                    AssistChip(
                        onClick = { },
                        label = { Text("${height}m") }
                    )
                }
            }
            
            route.description?.let { description ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterDialog(
    currentGrade: String?,
    currentType: String?,
    onGradeSelected: (String?) -> Unit,
    onTypeSelected: (String?) -> Unit,
    onDismiss: () -> Unit
) {
    val grades = listOf("5a", "5b", "5c", "6a", "6b", "6c", "7a", "7b", "7c", "8a", "8b", "8c")
    val types = listOf("sport", "trad", "boulder", "multi-pitch")
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filter Routes") },
        text = {
            Column {
                Text("Grade", style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    grades.take(4).forEach { grade ->
                        FilterChip(
                            selected = currentGrade == grade,
                            onClick = { 
                                onGradeSelected(if (currentGrade == grade) null else grade)
                            },
                            label = { Text(grade) }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                Text("Type", style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    types.forEach { type ->
                        FilterChip(
                            selected = currentType == type,
                            onClick = { 
                                onTypeSelected(if (currentType == type) null else type)
                            },
                            label = { Text(type) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}
