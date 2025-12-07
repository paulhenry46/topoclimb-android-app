package com.example.topoclimb.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.topoclimb.ui.theme.OnPinkSurface
import com.example.topoclimb.ui.theme.OnSuccessSurface
import com.example.topoclimb.ui.theme.OnVioletSurface
import com.example.topoclimb.ui.theme.PinkSurface
import com.example.topoclimb.ui.theme.SuccessSurface
import com.example.topoclimb.ui.theme.VioletSurface

// Color constants for climbing type cards
private val FLASH_COLOR = PinkSurface
private val FLASH_TEXT_COLOR = OnPinkSurface
private val VIEW_COLOR = VioletSurface
private val VIEW_TEXT_COLOR = OnVioletSurface
private val WORK_COLOR = SuccessSurface
private val WORK_TEXT_COLOR = OnSuccessSurface
private val DEFAULT_TEXT_COLOR = androidx.compose.ui.graphics.Color.White

/**
 * Step 1: Select climbing type (flash, view, work)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogRouteStep1Screen(
    routeName: String,
    onBackClick: () -> Unit,
    onTypeSelected: (String) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(0, 0, 0, 0),
                title = { Text("Log Route - Step 1/3") },
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
            
            // Climbing type cards
            ClimbingTypeCard(
                icon = Icons.Default.FlashOn,
                type = "Flash",
                description = "Climbed on first try without prior knowledge",
                onClick = { onTypeSelected("flash") }
            )
            
            ClimbingTypeCard(
                icon = Icons.Default.Visibility,
                type = "View",
                description = "Climbed after watching others or prior attempts",
                onClick = { onTypeSelected("view") }
            )
            
            ClimbingTypeCard(
                icon = Icons.Default.FitnessCenter,
                type = "Work",
                description = "Working on the route, multiple attempts",
                onClick = { onTypeSelected("work") }
            )
        }
    }
}

@Composable
fun ClimbingTypeCard(
    icon: ImageVector,
    type: String,
    description: String,
    onClick: () -> Unit
) {
    // Define colors based on type
    val (containerColor, contentColor) = when (type) {
        "Flash" -> Pair(FLASH_COLOR, FLASH_TEXT_COLOR)
        "View" -> Pair(VIEW_COLOR, VIEW_TEXT_COLOR)
        "Work" -> Pair(WORK_COLOR, WORK_TEXT_COLOR)
        else -> Pair(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurface)
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
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
                contentDescription = type,
                modifier = Modifier.size(48.dp),
                tint = contentColor
            )
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = type,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = DEFAULT_TEXT_COLOR
                )
                
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = contentColor.copy(alpha = 0.9f)
                )
            }
        }
    }
}
