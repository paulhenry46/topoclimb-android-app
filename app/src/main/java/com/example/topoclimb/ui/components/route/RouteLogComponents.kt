package com.example.topoclimb.ui.components.route

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import com.example.topoclimb.data.GradingSystem
import com.example.topoclimb.data.Log
import com.example.topoclimb.ui.theme.OnPinkSurface
import com.example.topoclimb.ui.theme.OnSuccessSurface
import com.example.topoclimb.ui.theme.OnVioletSurface
import com.example.topoclimb.ui.theme.PinkSurface
import com.example.topoclimb.ui.theme.SuccessSurface
import com.example.topoclimb.ui.theme.VioletSurface
import com.example.topoclimb.utils.GradeUtils

/**
 * Enum representing grade comparison states
 */
private enum class GradeComparison {
    HIGHER, LOWER, EQUAL
}

/**
 * Card component displaying a single log entry with expandable details
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LogCard(
    log: Log,
    routeGrade: Int?,
    gradingSystem: GradingSystem?,
    modifier: Modifier = Modifier,
    onLongPressUser: ((userId: Int, userName: String) -> Unit)? = null,
    onUserClick: ((userId: Int) -> Unit)? = null
) {
    var isExpanded by remember { mutableStateOf(false) }
    var showAddFriendDialog by remember { mutableStateOf(false) }
    
    // Calculate badge data
    val typeIcon = when (log.type?.lowercase()) {
        "flash" -> Icons.Filled.FlashOn
        "view" -> Icons.Filled.Visibility
        "work" -> Icons.Filled.Lens
        else -> null
    }
    
    val wayIcon = when (log.way?.lowercase()) {
        "top-rope", "sport" -> Icons.Filled.Lens
        "lead" -> Icons.Filled.Flag
        else -> null
    }
    
    val logGradeString = GradeUtils.pointsToGrade(log.grade, gradingSystem) ?: log.grade.toString()
    
    val gradeComparison = routeGrade?.let { routeGradeInt ->
        when {
            log.grade > routeGradeInt -> GradeComparison.HIGHER
            log.grade < routeGradeInt -> GradeComparison.LOWER
            else -> GradeComparison.EQUAL
        }
    }
    
    // Dialog for confirming add friend
    if (showAddFriendDialog) {
        AlertDialog(
            onDismissRequest = { showAddFriendDialog = false },
            title = { Text("Add Friend") },
            text = { Text("Add ${log.userName} as a friend?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onLongPressUser?.invoke(log.user.id, log.userName)
                        showAddFriendDialog = false
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddFriendDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { isExpanded = !isExpanded },
                onLongClick = {
                    onLongPressUser?.let { showAddFriendDialog = true }
                }
            )
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // User info row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f)
                        .then(
                            if (onUserClick != null) {
                                Modifier.clickable { onUserClick(log.user.id) }
                            } else {
                                Modifier
                            }
                        )
                ) {
                    // User avatar
                    UserAvatar(
                        userPpUrl = log.userPpUrl,
                        userName = log.userName
                    )
                    
                    Column {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = log.userName,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            
                            // Compact badges next to username (shown when collapsed)
                            AnimatedVisibility(
                                visible = !isExpanded,
                                enter = fadeIn() + expandHorizontally(),
                                exit = fadeOut() + shrinkHorizontally()
                            ) {
                                CompactBadgesRow(
                                    typeIcon = typeIcon,
                                    wayIcon = wayIcon,
                                    logType = log.type,
                                    logWay = log.way,
                                    logGradeString = logGradeString,
                                    gradeComparison = gradeComparison
                                )
                            }
                        }
                        Text(
                            text = formatLogDate(log.createdAt ?: ""),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // Verified badge
                if (log.isVerified) {
                    VerifiedBadge()
                }
            }
            
            // Full badges row (shown when expanded)
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                FullBadgesRow(
                    typeIcon = typeIcon,
                    wayIcon = wayIcon,
                    logType = log.type,
                    logWay = log.way,
                    logGradeString = logGradeString,
                    gradeComparison = gradeComparison
                )
            }
            
            // Comments if available
            if (!log.comments.isNullOrBlank()) {
                CommentCard(comment = log.comments)
            }
        }
        
        // Divider between logs
        HorizontalDivider(
            modifier = Modifier.padding(top = 8.dp),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    }
}

@Composable
private fun UserAvatar(
    userPpUrl: String?,
    userName: String
) {
    SubcomposeAsyncImage(
        model = userPpUrl,
        contentDescription = "User avatar",
        modifier = Modifier
            .size(40.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = MaterialTheme.shapes.medium
            ),
        contentScale = ContentScale.Crop
    ) {
        val state = painter.state
        when (state) {
            is AsyncImagePainter.State.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            is AsyncImagePainter.State.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = userName.firstOrNull()?.uppercase() ?: "?",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            else -> {
                Image(
                    painter = painter,
                    contentDescription = contentDescription,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = contentScale
                )
            }
        }
    }
}

@Composable
private fun CompactBadgesRow(
    typeIcon: ImageVector?,
    wayIcon: ImageVector?,
    logType: String?,
    logWay: String?,
    logGradeString: String,
    gradeComparison: GradeComparison?
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Type icon only
        typeIcon?.let { icon ->
            CompactBadge(
                icon = icon,
                containerColor = getTypeColor(logType),
                contentColor = getTypeContentColor(logType)
            )
        }
        
        // Way icon only (only show if not bouldering)
        if (logWay?.lowercase() != "bouldering" && wayIcon != null) {
            CompactBadge(
                icon = wayIcon,
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer
            )
        }
        
        // Grade value only (without "Grade:" text)
        CompactBadgeWithText(
            text = logGradeString,
            icon = when (gradeComparison) {
                GradeComparison.HIGHER -> Icons.Default.KeyboardArrowUp
                GradeComparison.LOWER -> Icons.Default.KeyboardArrowDown
                GradeComparison.EQUAL -> Icons.Default.Check
                null -> null
            },
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
private fun FullBadgesRow(
    typeIcon: ImageVector?,
    wayIcon: ImageVector?,
    logType: String?,
    logWay: String?,
    logGradeString: String,
    gradeComparison: GradeComparison?
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Type badge with icon
        if (logType != null) {
            LogBadgeWithIcon(
                text = logType.capitalize(),
                icon = typeIcon,
                containerColor = getTypeColor(logType),
                contentColor = getTypeContentColor(logType)
            )
        }
        
        // Way badge with icon (only show if not bouldering)
        if (logWay?.lowercase() != "bouldering" && logWay != null) {
            LogBadgeWithIcon(
                text = logWay.capitalize(),
                icon = wayIcon,
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer
            )
        }
        
        // Grade badge with comparison indicator
        LogBadgeWithIcon(
            text = "Grade: $logGradeString",
            icon = when (gradeComparison) {
                GradeComparison.HIGHER -> Icons.Default.KeyboardArrowUp
                GradeComparison.LOWER -> Icons.Default.KeyboardArrowDown
                GradeComparison.EQUAL -> Icons.Default.Check
                null -> null
            },
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
private fun VerifiedBadge() {
    Surface(
        color = SuccessSurface.copy(alpha = 0.1f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Verified",
                tint = OnSuccessSurface,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "Verified",
                style = MaterialTheme.typography.labelSmall,
                color = OnSuccessSurface
            )
        }
    }
}

@Composable
private fun CommentCard(comment: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Filled.ChatBubble,
                contentDescription = "Comment",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Comment",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = comment,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

/**
 * Badge component for displaying log information
 */
@Composable
fun LogBadge(
    text: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = containerColor,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Medium
            ),
            color = contentColor
        )
    }
}

/**
 * Badge component with icon for displaying log information
 */
@Composable
fun LogBadgeWithIcon(
    text: String,
    icon: ImageVector?,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = containerColor,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = contentColor
            )
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = contentColor
                )
            }
        }
    }
}

/**
 * Compact badge component with icon only
 */
@Composable
fun CompactBadge(
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = containerColor,
        shape = CircleShape,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier.padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = contentColor
            )
        }
    }
}

/**
 * Compact badge component with text and optional icon
 */
@Composable
fun CompactBadgeWithText(
    text: String,
    icon: ImageVector?,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = containerColor,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.sp
                ),
                color = contentColor
            )
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint = contentColor
                )
            }
        }
    }
}

// Helper functions

@Composable
private fun getTypeColor(type: String?): Color {
    return when (type?.lowercase()) {
        "flash" -> PinkSurface
        "view" -> VioletSurface
        else -> MaterialTheme.colorScheme.secondaryContainer
    }
}

@Composable
private fun getTypeContentColor(type: String?): Color {
    return when (type?.lowercase()) {
        "flash" -> OnPinkSurface
        "view" -> OnVioletSurface
        else -> MaterialTheme.colorScheme.onSecondaryContainer
    }
}

@Composable
private fun Modifier.clickableWithoutRipple(onClick: () -> Unit): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    return this.clickable(
        interactionSource = interactionSource,
        indication = null,
        onClick = onClick
    )
}

private fun formatLogDate(dateString: String): String {
    return try {
        val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", java.util.Locale.getDefault())
        val outputFormat = java.text.SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", java.util.Locale.getDefault())
        val date = inputFormat.parse(dateString)
        date?.let { outputFormat.format(it) } ?: dateString
    } catch (e: Exception) {
        dateString
    }
}

private fun String.capitalize(): String {
    return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase(java.util.Locale.getDefault()) else it.toString() }
}
