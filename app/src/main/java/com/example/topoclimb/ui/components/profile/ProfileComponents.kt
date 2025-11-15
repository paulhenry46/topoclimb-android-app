package com.example.topoclimb.ui.components.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/**
 * Info row component for displaying labeled information with an icon
 */
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

/**
 * Simple stat item displaying a value and label
 */
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

/**
 * Stats item with icon, label, and value
 */
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

/**
 * Chart component for displaying routes by grade
 */
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
