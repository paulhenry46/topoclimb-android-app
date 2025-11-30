package com.example.topoclimb.ui.screens

import android.app.Activity
import android.net.Uri
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QRCodeScreen(
    userName: String,
    userGender: String?,
    userBirthDate: String?,
    instanceName: String,
    instanceUrl: String,
    qrCodeUrl: String?,
    isLoading: Boolean,
    error: String?,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    
    // Set screen brightness to max when entering QR code screen, restore on exit
    DisposableEffect(Unit) {
        val activity = context as? Activity
        val window = activity?.window
        val layoutParams = window?.attributes
        val originalBrightness = layoutParams?.screenBrightness ?: WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
        
        // Set brightness to maximum
        layoutParams?.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL
        window?.attributes = layoutParams
        
        onDispose {
            // Restore original brightness
            layoutParams?.screenBrightness = originalBrightness
            window?.attributes = layoutParams
        }
    }
    
    // Calculate age and format birthday info
    val age = userBirthDate?.let { calculateAge(it) }
    val formattedBirthday = userBirthDate?.let { formatBirthday(it) }
    
    // Extract domain name from URL
    val domainName = remember(instanceUrl) {
        try {
            val uri = Uri.parse(instanceUrl)
            uri.host ?: instanceUrl
        } catch (e: Exception) {
            instanceUrl
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(0, 0, 0, 0),
                title = { Text("QR Code") },
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top section with user info - avatar circle next to name and info
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circle avatar with first letter of name
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = userName.firstOrNull()?.uppercase() ?: "?",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // Name and info column
                Column {
                    Text(
                        text = userName,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    // Birthday and gender info
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Birthday with age
                        if (formattedBirthday != null && age != null) {
                            Text(
                                text = "$formattedBirthday ($age)",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        // Separator if both exist
                        if (formattedBirthday != null && age != null && userGender?.isNotEmpty() == true) {
                            Text(
                                text = "•",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        // Gender
                        userGender?.let { gender ->
                            if (gender.isNotEmpty()) {
                                Text(
                                    text = gender.replaceFirstChar { char -> 
                                        if (char.isLowerCase()) char.titlecase() else char.toString() 
                                    },
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
            
            // Center section with QR code
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                when {
                    isLoading -> {
                        CircularProgressIndicator()
                    }
                    error != null -> {
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    qrCodeUrl != null -> {
                        // Column for QR code and backend info
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Box with white background and rounded corners for QR code
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.8f)
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color.White)
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                val context = LocalContext.current
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(qrCodeUrl)
                                        .decoderFactory(SvgDecoder.Factory())
                                        .build(),
                                    contentDescription = "User QR Code",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Fit
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Backend name and URL below QR code
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = instanceName,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = domainName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    else -> {
                        Text(
                            text = "QR Code not available",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            
            // Bottom section with back button
            Button(
                onClick = onBackClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                Text("Back to Profile")
            }
        }
    }
}

/**
 * Calculate age from birth date string (YYYY-MM-DD format)
 */
fun calculateAge(birthDate: String): Int? {
    return try {
        if (birthDate.length < 10) return null
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val birthLocalDate = LocalDate.parse(birthDate.substring(0, 10), formatter)
        val now = LocalDate.now()
        Period.between(birthLocalDate, now).years
    } catch (e: Exception) {
        null
    }
}

/**
 * Format birth date string to a readable format (e.g., "Jan 15, 1990")
 */
fun formatBirthday(birthDate: String): String? {
    return try {
        if (birthDate.length < 10) return null
        val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val outputFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")
        val birthLocalDate = LocalDate.parse(birthDate.substring(0, 10), inputFormatter)
        birthLocalDate.format(outputFormatter)
    } catch (e: Exception) {
        null
    }
}
