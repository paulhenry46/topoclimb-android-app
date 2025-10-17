package com.example.topoclimb.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.topoclimb.ui.utils.parseRouteColor

/**
 * Shared RouteCard component that displays route information
 * 
 * @param thumbnail URL of the route thumbnail image
 * @param grade The grade/difficulty of the route
 * @param color Hex color code for the route (e.g., "#FF5733")
 * @param name Name of the route
 * @param localId Optional local ID to display
 * @param isClimbed Whether the route has been climbed (shows check icon)
 * @param onClick Callback when the card is clicked
 */
@Composable
fun RouteCard(
    thumbnail: String?,
    grade: String?,
    color: String?,
    name: String,
    localId: String? = null,
    isClimbed: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val routeColor = parseRouteColor(color)
    
    // Shape for thumbnail: rounded on left, square on right for continuity with grade
    val thumbnailShape = RoundedCornerShape(
        topStart = 8.dp,
        topEnd = 0.dp,
        bottomEnd = 0.dp,
        bottomStart = 8.dp
    )
    
    // Shape for grade badge: square on left, rounded on right for continuity with thumbnail
    val gradeBadgeShape = RoundedCornerShape(
        topStart = 0.dp,
        topEnd = 8.dp,
        bottomEnd = 8.dp,
        bottomStart = 0.dp
    )
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            ),
        horizontalArrangement = Arrangement.spacedBy(0.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
            // Thumbnail image with route color border
            AsyncImage(
                model = thumbnail,
                contentDescription = "Route thumbnail",
                modifier = Modifier
                    .size(60.dp)
                    .border(
                        width = 3.dp,
                        color = routeColor,
                        shape = thumbnailShape
                    )
                    .clip(thumbnailShape),
                contentScale = ContentScale.Crop
            )
            
            // Grade badge with route color (square-shaped, max height)
            grade?.let {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(
                            color = routeColor,
                            shape = gradeBadgeShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        ),
                        color = Color.White
                    )
                }
            }
            
            // Spacer to push content
            Spacer(modifier = Modifier.width(12.dp))
            
            // Name and local ID section
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
                
                localId?.let { id ->
                    Text(
                        text = id,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Check icon for climbed routes (right-aligned)
            if (isClimbed) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Climbed",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
