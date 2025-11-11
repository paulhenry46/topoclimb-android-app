package com.example.topoclimb.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.topoclimb.ui.theme.Orange40
import com.example.topoclimb.ui.theme.Orange80
import com.example.topoclimb.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onManageBackendsClick: () -> Unit = {},
    onNavigateToQRCode: (String) -> Unit = {},
    viewModel: ProfileViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val user = uiState.user
    
    var isEditMode by remember { mutableStateOf(false) }
    var editedName by remember { mutableStateOf("") }
    var editedBirthDate by remember { mutableStateOf("") }
    var editedGender by remember { mutableStateOf("") }
    var showGenderDropdown by remember { mutableStateOf(false) }
    var showStatsHelpDialog by remember { mutableStateOf(false) }
    
    // Update edit fields when user changes
    LaunchedEffect(user) {
        if (user != null) {
            editedName = user.name
            editedBirthDate = user.birthDate?.take(10) ?: ""
            editedGender = user.gender ?: ""
        }
    }
    
    // Refresh profile when the screen is displayed
    LaunchedEffect(Unit) {
        viewModel.refresh()
    }
    
    // Show success snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.updateSuccess) {
        if (uiState.updateSuccess) {
            snackbarHostState.showSnackbar("Profile updated successfully!")
            viewModel.clearUpdateStatus()
            isEditMode = false
        }
    }
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
        if (uiState.isAuthenticated && user != null) {
                // Profile picture
                AsyncImage(
                    model = user.profilePhotoUrl,
                    contentDescription = "Profile picture",
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop,
                    placeholder = rememberVectorPainter(Icons.Default.Settings),
                    error = rememberVectorPainter(Icons.Default.Face)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // User name
                Text(
                    text = user.name,
                    style = MaterialTheme.typography.headlineMedium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Email
                Text(
                    text = user.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Instance name
                if (uiState.instanceName != null) {
                    Text(
                        text = "from ${uiState.instanceName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // User stats card
                uiState.userStats?.let { stats ->
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
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Climbing Stats",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                IconButton(
                                    onClick = { showStatsHelpDialog = true }
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                                        contentDescription = "Stats help",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            
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
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                // Stats help dialog
                if (showStatsHelpDialog) {
                    AlertDialog(
                        onDismissRequest = { showStatsHelpDialog = false },
                        title = { Text("How Stats are Calculated") },
                        text = {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "Total Climbed",
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    text = "This takes into account all routes climbed, whether traditional or bouldering, including those that have been dismantled since you completed them. A route climbed with lead and top rope is only counted once.",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Text(
                                    text = "Bouldering Level and Trad Level",
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    text = "We take the 3 hardest routes you've ever done, and then we take the lowest rating from those three. So, to have a level of 7a, you'll need to climb 3 routes with a minimum level of 7a.",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = { showStatsHelpDialog = false }) {
                                Text("OK")
                            }
                        }
                    )
                }
                
                
                if (uiState.userStats == null && uiState.isLoadingStats) {
                    // Loading stats indicator
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // User info card with edit mode
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Account Information",
                                style = MaterialTheme.typography.titleMedium
                            )
                            
                            if (!isEditMode) {
                                IconButton(onClick = { isEditMode = true }) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit profile"
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        if (isEditMode) {
                            // Edit mode
                            OutlinedTextField(
                                value = editedName,
                                onValueChange = { if (it.length <= 255) editedName = it },
                                label = { Text("Name") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            OutlinedTextField(
                                value = editedBirthDate,
                                onValueChange = { editedBirthDate = it },
                                label = { Text("Birth Date (YYYY-MM-DD)") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                placeholder = { Text("YYYY-MM-DD") }
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Gender dropdown
                            ExposedDropdownMenuBox(
                                expanded = showGenderDropdown,
                                onExpandedChange = { showGenderDropdown = it }
                            ) {
                                OutlinedTextField(
                                    value = when (editedGender.lowercase()) {
                                        "male" -> "Male"
                                        "female" -> "Female"
                                        "other" -> "Other"
                                        else -> "Not specified"
                                    },
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Gender") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showGenderDropdown) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor()
                                )
                                ExposedDropdownMenu(
                                    expanded = showGenderDropdown,
                                    onDismissRequest = { showGenderDropdown = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Male") },
                                        onClick = {
                                            editedGender = "male"
                                            showGenderDropdown = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Female") },
                                        onClick = {
                                            editedGender = "female"
                                            showGenderDropdown = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Other") },
                                        onClick = {
                                            editedGender = "other"
                                            showGenderDropdown = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Not specified") },
                                        onClick = {
                                            editedGender = ""
                                            showGenderDropdown = false
                                        }
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Error message
                            val currentUpdateError = uiState.updateError
                            if (currentUpdateError != null) {
                                Text(
                                    text = currentUpdateError,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                            
                            // Save/Cancel buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        isEditMode = false
                                        editedName = user.name
                                        editedBirthDate = user.birthDate?.take(10) ?: ""
                                        editedGender = user.gender ?: ""
                                        viewModel.clearUpdateStatus()
                                    },
                                    modifier = Modifier.weight(1f),
                                    enabled = !uiState.isUpdating
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Cancel,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Cancel")
                                }
                                
                                Button(
                                    onClick = {
                                        viewModel.updateUserInfo(
                                            name = editedName.takeIf { it.isNotBlank() },
                                            birthDate = editedBirthDate.takeIf { it.isNotBlank() },
                                            gender = editedGender.takeIf { it.isNotBlank() }
                                        )
                                    },
                                    modifier = Modifier.weight(1f),
                                    enabled = !uiState.isUpdating
                                ) {
                                    if (uiState.isUpdating) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(18.dp),
                                            strokeWidth = 2.dp,
                                            color = MaterialTheme.colorScheme.onPrimary
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.Save,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Save")
                                    }
                                }
                            }
                        } else {
                            // View mode
                            user.birthDate?.let { birthDate ->
                                if (birthDate.length >= 10) {
                                    InfoRow("Birth Date", birthDate.substring(0, 10), Icons.Default.Cake)
                                }
                            }
                            user.gender?.let { gender ->
                                if (gender.isNotEmpty()) {
                                    InfoRow("Gender", gender.replaceFirstChar { char -> 
                                        if (char.isLowerCase()) char.titlecase() else char.toString() 
                                    }, Icons.Default.Person)
                                }
                            }
                            if (user.createdAt.length >= 10) {
                                InfoRow("Member Since", user.createdAt.substring(0, 10), Icons.Default.CalendarMonth)
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            } else {
                // Not authenticated state with improved UI
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Orange80
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Face,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = androidx.compose.ui.graphics.Color.Black
                        )
                        Text(
                            text = "Not Logged In",
                            style = MaterialTheme.typography.titleLarge,
                            color = androidx.compose.ui.graphics.Color.Black
                        )
                        Text(
                            text = "Login to a TopoClimb instance to see your profile",
                            style = MaterialTheme.typography.bodyMedium,
                            color = androidx.compose.ui.graphics.Color.Black
                        )
                        Button(
                            onClick = onManageBackendsClick,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Orange40,
                                contentColor = androidx.compose.ui.graphics.Color.White
                            )
                        ) {
                            Icon(
                                Icons.Default.Settings,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Go to Instance Manager")
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // QR Code section for authenticated instances
            if (uiState.authenticatedBackends.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "QR Codes",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "View your QR code for each authenticated instance",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        uiState.authenticatedBackends.forEach { backend ->
                            OutlinedButton(
                                onClick = { onNavigateToQRCode(backend.id) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Text(backend.name)
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Settings card
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Button(
                        onClick = onManageBackendsClick,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Manage TopoClimb Instances")
                    }
                }
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String, icon: ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
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

@Composable
fun StatItem(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.secondary
        )
    }
}

@Composable
fun StatsItem(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(32.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

/**
 * Parse routes by grade from API response
 * Handles both [] (empty array) and {"6a":2,"5c":2} (object) formats
 */
fun parseRoutesbyGrade(routesByGrade: Any?): Map<String, Int> {
    if (routesByGrade == null) return emptyMap()
    
    return try {
        when (routesByGrade) {
            is Map<*, *> -> {
                // Convert map to Map<String, Int>
                routesByGrade.entries
                    .mapNotNull { entry ->
                        val key = entry.key as? String
                        val value = when (val v = entry.value) {
                            is Number -> v.toInt()
                            is String -> v.toIntOrNull()
                            else -> null
                        }
                        if (key != null && value != null) key to value else null
                    }
                    .toMap()
            }
            is List<*> -> {
                // Empty list case
                if (routesByGrade.isEmpty()) emptyMap() else emptyMap()
            }
            else -> emptyMap()
        }
    } catch (e: Exception) {
        emptyMap()
    }
}

@Composable
fun RoutesByGradeChart(gradeMap: Map<String, Int>) {
    if (gradeMap.isEmpty()) return
    
    // Sort grades by natural order
    val sortedGrades = gradeMap.entries.sortedBy { it.key }
    val maxValue = sortedGrades.maxOfOrNull { it.value } ?: 1
    
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            sortedGrades.forEach { (grade, count) ->
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    // Count label above bar
                    Text(
                        text = count.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(((count.toFloat() / maxValue) * 100).dp)
                            .clip(MaterialTheme.shapes.small)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Grade label
                    Text(
                        text = grade,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}
