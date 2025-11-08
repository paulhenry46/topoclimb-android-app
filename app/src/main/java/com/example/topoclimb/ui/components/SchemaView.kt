package com.example.topoclimb.ui.components

import android.annotation.SuppressLint
import android.webkit.JavascriptInterface
import android.webkit.WebView
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.topoclimb.data.SectorSchema
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun SchemaView(
    schema: SectorSchema,
    filteredRouteIds: Set<Int>,
    onRouteClick: (Int) -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    hasPrevious: Boolean,
    hasNext: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    // Load background image and SVG paths
    var bgImageData by remember(schema) { mutableStateOf<String?>(null) }
    var svgPathsData by remember(schema) { mutableStateOf<String?>(null) }
    var isLoading by remember(schema) { mutableStateOf(true) }
    var error by remember(schema) { mutableStateOf<String?>(null) }
    var imageHeight by remember(schema) { mutableStateOf(400.dp) }
    
    // Reuse OkHttpClient instance
    val httpClient = remember { OkHttpClient() }
    val density = androidx.compose.ui.platform.LocalDensity.current
    
    LaunchedEffect(schema) {
        isLoading = true
        error = null
        
        try {
            // Load background image as base64
            val bgData = schema.bg?.let { url ->
                withContext(Dispatchers.IO) {
                    try {
                        val request = Request.Builder().url(url).build()
                        httpClient.newCall(request).execute().use { response ->
                            if (response.isSuccessful) {
                                // Determine content type from response or default to image/*
                                val contentType = response.header("Content-Type") ?: "image/jpeg"
                                response.body?.bytes()?.let { bytes ->
                                    "data:$contentType;base64," + android.util.Base64.encodeToString(
                                        bytes,
                                        android.util.Base64.NO_WRAP
                                    )
                                }
                            } else null
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        null
                    }
                }
            }
            
            // Load SVG paths
            val pathsData = schema.paths?.let { url ->
                withContext(Dispatchers.IO) {
                    try {
                        val request = Request.Builder().url(url).build()
                        httpClient.newCall(request).execute().use { response ->
                            if (response.isSuccessful) {
                                response.body?.string()
                            } else null
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        null
                    }
                }
            }
            
            bgImageData = bgData
            svgPathsData = pathsData
            isLoading = false
        } catch (e: Exception) {
            error = e.message
            isLoading = false
        }
    }
    
    Column(modifier = modifier) {
        // Navigation controls and sector name
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Previous button
                IconButton(
                    onClick = onPreviousClick,
                    enabled = hasPrevious
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Previous sector",
                        tint = if (hasPrevious) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    )
                }
                
                // Sector name
                Text(
                    text = schema.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                // Next button
                IconButton(
                    onClick = onNextClick,
                    enabled = hasNext
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Next sector",
                        tint = if (hasNext) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    )
                }
            }
        }
        
        // Schema content
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Error loading schema: $error",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                bgImageData != null && svgPathsData != null -> {
                    AndroidView(
                        factory = { context ->
                            WebView(context).apply {
                                settings.javaScriptEnabled = true
                                settings.loadWithOverviewMode = true
                                settings.useWideViewPort = false
                                settings.builtInZoomControls = false
                                settings.displayZoomControls = false
                                setBackgroundColor(android.graphics.Color.TRANSPARENT)
                                
                                // Add JavaScript interface for route clicks
                                addJavascriptInterface(object {
                                    @JavascriptInterface
                                    fun onRouteSelected(routeIdStr: String) {
                                        // Validate input: should start with "path_" and contain only digits after
                                        if (routeIdStr.startsWith("path_")) {
                                            val routeId = routeIdStr.removePrefix("path_").toIntOrNull()
                                            if (routeId != null && routeId > 0) {
                                                onRouteClick(routeId)
                                            }
                                        }
                                    }
                                    
                                    @JavascriptInterface
                                    fun setImageHeight(heightPx: Int) {
                                        // Convert px to dp and update the state
                                        // Add a small buffer (8dp) to prevent any cropping
                                        val heightDp = with(density) { heightPx.toDp() + 8.dp }
                                        imageHeight = heightDp
                                    }
                                }, "Android")
                            }
                        },
                        update = { webView ->
                            // Sanitize inputs to prevent XSS
                            // Route IDs are integers, so creating "path_N" format is safe
                            val visibleRouteIds = filteredRouteIds.joinToString(",") { id -> "path_$id" }
                            
                            // Escape the SVG content to prevent script injection
                            // Note: We trust the SVG from the server, but we sanitize by removing any script tags
                            val sanitizedSvg = svgPathsData?.replace(
                                Regex("<script[^>]*>.*?</script>", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)),
                                ""
                            ) ?: ""
                            
                            val htmlContent = """
                                <!DOCTYPE html>
                                <html>
                                <head>
                                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                                    <style>
                                        body {
                                            margin: 0;
                                            padding: 0;
                                            background: transparent;
                                            -webkit-tap-highlight-color: transparent;
                                            -webkit-touch-callout: none;
                                            -webkit-user-select: none;
                                            user-select: none;
                                        }
                                        .container {
                                            position: relative;
                                            width: 100%;
                                            display: inline-block;
                                        }
                                        .bg-image {
                                            width: 100%;
                                            height: auto;
                                            display: block;
                                        }
                                        .svg-overlay {
                                            position: absolute;
                                            top: 0;
                                            left: 0;
                                            width: 100%;
                                            height: 100%;
                                            pointer-events: none;
                                        }
                                        svg {
                                            width: 100%;
                                            height: 100%;
                                            position: absolute;
                                            top: 0;
                                            left: 0;
                                        }
                                        svg path {
                                            /* Keep original stroke colors from SVG */
                                            fill: none;
                                            cursor: pointer;
                                            stroke-width: 3;
                                            opacity: 0.8;
                                            pointer-events: auto;
                                        }
                                        svg path.hidden {
                                            display: none;
                                        }
                                        svg path:hover {
                                            stroke-width: 5;
                                            opacity: 1;
                                        }
                                    </style>
                                    <script>
                                        document.addEventListener('DOMContentLoaded', function() {
                                            const visibleIds = new Set('$visibleRouteIds'.split(','));
                                            const paths = document.querySelectorAll('svg path');
                                            
                                            paths.forEach(function(path) {
                                                // Hide paths that are not in the visible set
                                                if (path.id && !visibleIds.has(path.id)) {
                                                    path.classList.add('hidden');
                                                }
                                                
                                                // Add click handler
                                                path.addEventListener('click', function(e) {
                                                    e.preventDefault();
                                                    if (window.Android && window.Android.onRouteSelected && this.id) {
                                                        window.Android.onRouteSelected(this.id);
                                                    }
                                                });
                                            });
                                            
                                            // Measure and report image height
                                            const img = document.querySelector('.bg-image');
                                            if (img) {
                                                function updateHeight() {
                                                    if (window.Android && window.Android.setImageHeight) {
                                                        // Use scrollHeight to get full content height
                                                        const container = document.querySelector('.container');
                                                        const fullHeight = Math.max(
                                                            img.offsetHeight,
                                                            img.scrollHeight,
                                                            container ? container.offsetHeight : 0,
                                                            container ? container.scrollHeight : 0,
                                                            document.body.scrollHeight
                                                        );
                                                        window.Android.setImageHeight(fullHeight);
                                                    }
                                                }
                                                
                                                if (img.complete) {
                                                    updateHeight();
                                                } else {
                                                    img.addEventListener('load', updateHeight);
                                                }
                                            }
                                        });
                                    </script>
                                </head>
                                <body>
                                    <div class="container">
                                        <img class="bg-image" src="$bgImageData" alt="Sector background">
                                        <div class="svg-overlay">
                                            $sanitizedSvg
                                        </div>
                                    </div>
                                </body>
                                </html>
                            """.trimIndent()
                            
                            webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(imageHeight)
                    )
                }
                else -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Schema data not available for this sector",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
