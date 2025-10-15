package com.example.topoclimb.ui.components

import android.annotation.SuppressLint
import android.webkit.JavascriptInterface
import android.webkit.WebView
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.example.topoclimb.utils.SvgDimensions

/**
 * WebView-based SVG map viewer with interactive sector selection.
 * 
 * Features:
 * - Tap on sectors to select them
 * - Visual feedback (red color, thicker stroke) for selected sectors
 * - Native-like appearance and behavior
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun SvgWebMapView(
    svgContent: String?,
    svgDimensions: SvgDimensions?,
    selectedSectorId: Int?,
    onSectorTapped: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    if (svgContent == null) return
    
    // Calculate aspect ratio from SVG dimensions
    val aspectRatioModifier = svgDimensions?.let { dims ->
        Modifier.aspectRatio(dims.viewBoxWidth / dims.viewBoxHeight)
    } ?: Modifier
    
    // Create HTML content with embedded SVG and JavaScript for interaction
    val htmlContent = remember(svgContent, selectedSectorId) {
        createInteractiveHtml(svgContent, selectedSectorId)
    }
    
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                settings.apply {
                    javaScriptEnabled = true
                    builtInZoomControls = false
                    displayZoomControls = false
                    setSupportZoom(false)
                    useWideViewPort = false
                    loadWithOverviewMode = false
                }
                
                // Add JavaScript interface for communication
                addJavascriptInterface(
                    SectorJavaScriptInterface { sectorId ->
                        onSectorTapped(sectorId)
                    },
                    "Android"
                )
                
                // Set background to transparent for native look
                setBackgroundColor(0x00000000)
                
                loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
            }
        },
        update = { webView ->
            webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
        },
        modifier = modifier
            .fillMaxWidth()
            .then(aspectRatioModifier)
    )
}

/**
 * JavaScript interface for communication between WebView and Kotlin
 */
private class SectorJavaScriptInterface(
    private val onSectorTapped: (Int) -> Unit
) {
    @JavascriptInterface
    fun onSectorClick(sectorId: Int) {
        onSectorTapped(sectorId)
    }
}

/**
 * Creates HTML content with interactive SVG map
 */
private fun createInteractiveHtml(svgContent: String, selectedSectorId: Int?): String {
    return """
<!DOCTYPE html>
<html>
<head>
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }
        
        html, body {
            width: 100%;
            height: 100%;
            overflow: hidden;
            background: transparent;
            -webkit-tap-highlight-color: transparent;
        }
        
        svg {
            width: 100%;
            height: 100%;
            display: block;
            touch-action: none;
        }
        
        /* Default path style */
        path[id^="sector"] {
            fill: none;
            stroke: #000000;
            stroke-width: 8;
            cursor: pointer;
            transition: stroke 0.15s ease, stroke-width 0.15s ease;
        }
        
        /* Selected path style */
        path.selected {
            stroke: #FF0000 !important;
            stroke-width: 10 !important;
        }
        
        /* Hover effect for better UX */
        path[id^="sector"]:hover {
            stroke-width: 9;
        }
    </style>
</head>
<body>
    <div id="svg-container">
        ${svgContent}
    </div>
    
    <script>
        // Prevent default touch behaviors
        document.addEventListener('touchmove', function(e) {
            e.preventDefault();
        }, { passive: false });
        
        // Get all sector paths
        const paths = document.querySelectorAll('path[id^="sector"]');
        
        // Apply selected state if needed
        const selectedSectorId = ${selectedSectorId ?: "null"};
        if (selectedSectorId !== null) {
            paths.forEach(path => {
                const pathId = path.getAttribute('id');
                const match = pathId.match(/sector[_-]?(\\d+)/i);
                if (match && parseInt(match[1]) === selectedSectorId) {
                    path.classList.add('selected');
                } else {
                    path.classList.remove('selected');
                }
            });
        }
        
        // Add click/tap handlers to all sector paths
        paths.forEach(path => {
            path.addEventListener('click', function(e) {
                e.preventDefault();
                e.stopPropagation();
                
                const pathId = this.getAttribute('id');
                // Extract sector ID from id attribute (e.g., "sector_123" or "sector-123")
                const match = pathId.match(/sector[_-]?(\\d+)/i);
                
                if (match && match[1]) {
                    const sectorId = parseInt(match[1]);
                    
                    // Call Android interface
                    if (window.Android && window.Android.onSectorClick) {
                        window.Android.onSectorClick(sectorId);
                    }
                }
            });
            
            // Also handle touch events for better mobile support
            path.addEventListener('touchend', function(e) {
                e.preventDefault();
                e.stopPropagation();
                
                const pathId = this.getAttribute('id');
                const match = pathId.match(/sector[_-]?(\\d+)/i);
                
                if (match && match[1]) {
                    const sectorId = parseInt(match[1]);
                    
                    if (window.Android && window.Android.onSectorClick) {
                        window.Android.onSectorClick(sectorId);
                    }
                }
            }, { passive: false });
        });
    </script>
</body>
</html>
    """.trimIndent()
}
