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
import com.example.topoclimb.data.CachedSectorSchema

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun SchemaView(
    schema: CachedSectorSchema,
    filteredRouteIds: Set<Int>,
    onRouteClick: (Int) -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    hasPrevious: Boolean,
    hasNext: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    // State for image height
    var imageHeight by remember(schema.id) { mutableStateOf(400.dp) }
    
    val density = androidx.compose.ui.platform.LocalDensity.current
    
    // Get cached data directly from the schema
    val bgImageData = schema.bgContent
    val svgPathsData = schema.pathsContent
    
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
                                        // Validate input
                                        if (heightPx > 0) {
                                            // Convert px to dp and update the state
                                            val heightDp = with(density) { heightPx.toDp() }
                                            // Always update to the reported height (it's already the natural size)
                                            imageHeight = heightDp
                                        }
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
                                        html, body {
                                            margin: 0;
                                            padding: 0;
                                            background: transparent;
                                            -webkit-tap-highlight-color: transparent;
                                            -webkit-touch-callout: none;
                                            -webkit-user-select: none;
                                            user-select: none;
                                            overflow: visible;
                                        }
                                        .container {
                                            position: relative;
                                            width: 100%;
                                            display: block;
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
                                            stroke-width: 10;
                                            opacity: 0.7;
                                            pointer-events: auto;
                                        }
                                        svg path.hidden {
                                            display: none;
                                        }
                                        svg path:hover {
                                            stroke-width: 15;
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
                                                        // Use multiple attempts to ensure we get the correct height
                                                        const attempts = [50, 150, 300];
                                                        attempts.forEach(function(delay) {
                                                            setTimeout(function() {
                                                                // Get the natural height of the image
                                                                const naturalHeight = img.naturalHeight;
                                                                
                                                                if (naturalHeight > 0) {
                                                                    // Report the actual image height in pixels
                                                                    window.Android.setImageHeight(naturalHeight);
                                                                }
                                                            }, delay);
                                                        });
                                                    }
                                                }
                                                
                                                if (img.complete && img.naturalHeight > 0) {
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
                            .heightIn(min = imageHeight)
                            .wrapContentHeight()
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
