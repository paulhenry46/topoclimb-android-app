package com.example.topoclimb.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

// Constants for area types
private const val AREA_TYPE_BOULDERING = "bouldering"

/**
 * Step 2: Select climbing way (Lead, Top-Rope, Bouldering)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogRouteStep2Screen(
    routeName: String,
    areaType: String?, // "bouldering" or "traditional"
    onBackClick: () -> Unit,
    onWaySelected: (String) -> Unit
) {
    // If area is bouldering, automatically select bouldering and move to next step
    LaunchedEffect(areaType) {
        if (areaType?.lowercase() == AREA_TYPE_BOULDERING) {
            onWaySelected("bouldering")
        }
    }
    
    // Only show this screen if it's not a bouldering-only area
    if (areaType?.lowercase() == AREA_TYPE_BOULDERING) {
        // This screen will not be shown, navigation will skip to step 3
        return
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(0, 0, 0, 0),
                title = { Text("Log Route - Step 2/3") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "How did you climb this route?",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
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
            
            // Show bouldering only for traditional areas
            if (areaType?.lowercase() != AREA_TYPE_BOULDERING) {
                ClimbingWayCard(
                    icon = Icons.Default.Landscape,
                    way = "Bouldering",
                    description = "Climbing without rope on shorter routes",
                    onClick = { onWaySelected("bouldering") }
                )
            }
        }
    }
}

@Composable
fun ClimbingWayCard(
    icon: ImageVector,
    way: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = way,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = way,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
