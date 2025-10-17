package com.example.topoclimb.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
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
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail image (rounded)
            AsyncImage(
                model = route.thumbnail,
                contentDescription = "Route thumbnail",
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            
            // Grade badge with route color
            route.grade?.let { grade ->
                val gradeColor = parseColor(route.color)
                Box(
                    modifier = Modifier
                        .background(
                            color = gradeColor,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = grade,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        ),
                        color = Color.White
                    )
                }
            }
            
            // Name section (no local ID available in RoutesScreen)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = route.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }
    }
}

// Helper function to parse hex color string
private fun parseColor(colorHex: String?): Color {
    return try {
        if (colorHex != null && colorHex.startsWith("#")) {
            Color(android.graphics.Color.parseColor(colorHex))
        } else {
            Color(0xFF6200EE) // Default Material purple
        }
    } catch (e: Exception) {
        Color(0xFF6200EE) // Default Material purple on error
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
