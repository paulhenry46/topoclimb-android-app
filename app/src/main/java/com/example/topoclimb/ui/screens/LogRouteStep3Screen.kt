package com.example.topoclimb.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.topoclimb.data.GradingSystem
import com.example.topoclimb.utils.GradeUtils

/**
 * Step 3: Enter grade, comment, and optional video URL
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogRouteStep3Screen(
    routeName: String,
    routeGrade: Int?,
    gradingSystem: GradingSystem?,
    onBackClick: () -> Unit,
    onSubmit: (grade: Int, comment: String?, videoUrl: String?) -> Unit,
    isLoading: Boolean = false,
    error: String? = null
) {
    var selectedGrade by remember { 
        mutableStateOf(
            routeGrade?.let { GradeUtils.pointsToGrade(it, gradingSystem) } ?: "error"
        ) 
    }
    var comment by remember { mutableStateOf("") }
    var videoUrl by remember { mutableStateOf("") }
    
    // Update selectedGrade when gradingSystem becomes available
    LaunchedEffect(gradingSystem, routeGrade) {
        routeGrade?.let { grade ->
            GradeUtils.pointsToGrade(grade, gradingSystem)?.let { gradeStr ->
                selectedGrade = gradeStr
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                windowInsets = TopAppBarDefaults.windowInsets,
                title = { Text("Log Route - Step 3/3") },
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
            // Route name
            Text(
                text = "Logging: $routeName",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            
            Text(
                text = "Final details",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Error message
            if (error != null) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = error,
                        modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            // Grade input
            OutlinedTextField(
                value = selectedGrade,
                onValueChange = { selectedGrade = it },
                label = { Text("Your felt grade") },
                placeholder = { Text("e.g., 6a, 7b+") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isLoading
            )
            
            // Comment input
            OutlinedTextField(
                value = comment,
                onValueChange = { if (it.length <= 1000) comment = it },
                label = { Text("Comment (optional)") },
                placeholder = { Text("Share your experience...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                maxLines = 6,
                supportingText = {
                    Text("${comment.length}/1000")
                },
                enabled = !isLoading
            )
            
            // Video URL input
            OutlinedTextField(
                value = videoUrl,
                onValueChange = { if (it.length <= 255) videoUrl = it },
                label = { Text("Video URL (optional)") },
                placeholder = { Text("https://...") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                supportingText = {
                    Text("${videoUrl.length}/255")
                },
                enabled = !isLoading
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Validate grade
            val gradePoints = GradeUtils.gradeToPoints(selectedGrade, gradingSystem)
            val isValidGrade = gradePoints in GradeUtils.minGradePoints(gradingSystem)..GradeUtils.maxGradePoints(gradingSystem)
            val canSubmit = !isLoading && selectedGrade.isNotBlank() && isValidGrade
            
            // Show validation error for invalid grade
            if (selectedGrade.isNotBlank() && !isValidGrade) {
                Text(
                    text = "Invalid grade. Please enter a valid climbing grade.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            
            // Submit button
            Button(
                onClick = {
                    if (isValidGrade) {
                        onSubmit(
                            gradePoints,
                            comment.takeIf { it.isNotBlank() },
                            videoUrl.takeIf { it.isNotBlank() }
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = canSubmit
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(
                        text = "Submit Log",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}
