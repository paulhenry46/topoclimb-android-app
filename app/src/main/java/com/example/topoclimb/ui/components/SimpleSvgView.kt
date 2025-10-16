package com.example.topoclimb.ui.components

import android.annotation.SuppressLint
import android.webkit.WebView
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.example.topoclimb.utils.SvgDimensions

/**
 * Simple WebView-based SVG viewer without interaction.
 * Displays the SVG map as a static image in a collapsible card.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun SimpleSvgView(
    svgContent: String?,
    svgDimensions: SvgDimensions?,
    modifier: Modifier = Modifier
) {
    if (svgContent == null) return
    
    // Calculate aspect ratio from SVG dimensions
    val aspectRatioModifier = svgDimensions?.let { dims ->
        Modifier.aspectRatio(dims.viewBoxWidth / dims.viewBoxHeight)
    } ?: Modifier.aspectRatio(1f)
    
    // Create HTML content with embedded SVG
    val htmlContent = remember(svgContent) {
        createSimpleHtml(svgContent)
    }
    
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                settings.apply {
                    javaScriptEnabled = false
                    builtInZoomControls = false
                    displayZoomControls = false
                    setSupportZoom(false)
                    useWideViewPort = false
                    loadWithOverviewMode = false
                }
                
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
 * Creates HTML content with static SVG display
 */
private fun createSimpleHtml(svgContent: String): String {
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
        }
        
        svg {
            width: 100%;
            height: 100%;
            display: block;
        }
        
        /* Style all paths with consistent appearance */
        path {
            fill: none;
            stroke: #000000;
            stroke-width: 8;
        }
    </style>
</head>
<body>
    <div id="svg-container">
        ${svgContent}
    </div>
</body>
</html>
    """.trimIndent()
}
