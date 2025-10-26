package com.example.topoclimb.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

data class Particle(
    val startX: Float,
    val startY: Float,
    val velocityX: Float,
    val velocityY: Float,
    val color: Color,
    val life: Float = 1f
)

@Composable
fun HeroMomentScreen(
    onDismiss: () -> Unit,
    routeName: String = "Route"
) {
    var particles by remember { mutableStateOf<List<Particle>>(emptyList()) }
    val infiniteTransition = rememberInfiniteTransition(label = "particles")
    
    // Scale animation for the success icon
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    // Initialize particles
    LaunchedEffect(Unit) {
        val colors = listOf(
            Color(0xFFFFD700), // Gold
            Color(0xFFFF6B6B), // Red
            Color(0xFF4ECDC4), // Turquoise
            Color(0xFFFFE66D), // Yellow
            Color(0xFF95E1D3), // Mint
            Color(0xFFF38181), // Pink
            Color(0xFFAA96DA), // Purple
            Color(0xFFFCBF49)  // Orange
        )
        
        particles = List(50) {
            val angle = Random.nextFloat() * 2 * Math.PI
            val velocity = Random.nextFloat() * 3 + 2
            Particle(
                startX = 0.5f,
                startY = 0.5f,
                velocityX = (cos(angle) * velocity).toFloat(),
                velocityY = (sin(angle) * velocity).toFloat(),
                color = colors.random(),
                life = 1f
            )
        }
    }
    
    // Auto dismiss after 3 seconds
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(3000)
        onDismiss()
    }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.95f),
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            // Fireworks animation
            FireworksAnimation(particles = particles)
            
            // Content
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier.padding(32.dp)
            ) {
                // Success icon with animation
                Surface(
                    modifier = Modifier.size(120.dp * scale),
                    shape = MaterialTheme.shapes.extraLarge,
                    color = MaterialTheme.colorScheme.primary,
                    shadowElevation = 8.dp
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Success",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
                
                // Success text
                Text(
                    text = "Amazing!",
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                
                Text(
                    text = "You logged $routeName!",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                
                // Dismiss button
                FilledTonalButton(
                    onClick = onDismiss,
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text("Continue")
                }
            }
        }
    }
}

@Composable
private fun FireworksAnimation(particles: List<Particle>) {
    val animationProgress by rememberInfiniteTransition(label = "fireworks")
        .animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "progress"
        )
    
    Canvas(
        modifier = Modifier.fillMaxSize()
    ) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        
        particles.forEach { particle ->
            val progress = animationProgress
            val life = 1f - progress
            
            if (life > 0) {
                val x = centerX + particle.velocityX * progress * 200
                val y = centerY + particle.velocityY * progress * 200 + progress * progress * 100 // Gravity effect
                
                val alpha = life * 0.8f
                val radius = 8f * (1f - progress * 0.5f)
                
                drawCircle(
                    color = particle.color.copy(alpha = alpha),
                    radius = radius,
                    center = Offset(x, y)
                )
                
                // Trail effect
                if (progress > 0.1f) {
                    val prevX = centerX + particle.velocityX * (progress - 0.1f) * 200
                    val prevY = centerY + particle.velocityY * (progress - 0.1f) * 200 + (progress - 0.1f) * (progress - 0.1f) * 100
                    
                    drawLine(
                        color = particle.color.copy(alpha = alpha * 0.3f),
                        start = Offset(prevX, prevY),
                        end = Offset(x, y),
                        strokeWidth = 2f
                    )
                }
            }
        }
    }
}
