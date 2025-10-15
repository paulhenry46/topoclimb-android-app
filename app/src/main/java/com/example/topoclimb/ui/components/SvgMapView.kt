package com.example.topoclimb.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.pointerInput
import com.example.topoclimb.utils.SvgDimensions
import com.example.topoclimb.utils.SvgPathData

/**
 * Renders an SVG map with interactive sectors, pan and zoom gestures.
 * 
 * This component allows users to:
 * - Pan (scroll) the map in all directions
 * - Zoom in/out with pinch gestures
 * - Tap on sectors to select them
 * 
 * The map is initially aligned with its bottom-right corner at the bottom-right of the container.
 */
@Composable
fun SvgMapView(
    svgPaths: List<SvgPathData>,
    svgDimensions: SvgDimensions?,
    selectedSectorId: Int?,
    onPathTapped: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    // Pre-compute path bounds for hit testing (in SVG coordinate space)
    val pathBoundsMap = remember(svgPaths) {
        svgPaths.mapNotNull { pathData ->
            pathData.sectorId?.let { sectorId ->
                val bounds = pathData.path.getBounds()
                sectorId to bounds
            }
        }.toMap()
    }
    
    // State for pan and zoom
    var scale by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    
    // Track if initial offset has been set
    var initialOffsetSet by remember { mutableStateOf(false) }
    
    // Transformable state for pan and zoom gestures
    val state = rememberTransformableState { zoomChange, panChange, _ ->
        scale = (scale * zoomChange).coerceIn(0.5f, 5f)
        offsetX += panChange.x
        offsetY += panChange.y
    }
    
    // Calculate aspect ratio from SVG dimensions
    val aspectRatioModifier = svgDimensions?.let { dims ->
        Modifier.aspectRatio(dims.viewBoxWidth / dims.viewBoxHeight)
    } ?: Modifier
    
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .then(aspectRatioModifier)
            .transformable(state = state)
            .pointerInput(svgPaths, svgDimensions, scale, offsetX, offsetY) {
                detectTapGestures { tapOffset ->
                    svgDimensions?.let { dims ->
                        // Calculate base scale factor to fit viewBox to canvas width
                        // Must match the calculation in the draw scope
                        val baseScale = size.width / scale / dims.viewBoxWidth
                        
                        // Transform tap position from screen to SVG coordinates
                        // First, undo user pan/zoom transformations
                        val canvasX = (tapOffset.x - offsetX) / scale
                        val canvasY = (tapOffset.y - offsetY) / scale
                        
                        // Then convert from canvas space to SVG space
                        val svgX = (canvasX / baseScale) + dims.viewBoxX
                        val svgY = (canvasY / baseScale) + dims.viewBoxY
                        val svgPoint = Offset(svgX, svgY)
                        
                        // Find which path was tapped using bounds checking in SVG space
                        pathBoundsMap.forEach { (sectorId, bounds) ->
                            // Expand bounds for easier tapping
                            val expandedBounds = Rect(
                                left = bounds.left - 20f,
                                top = bounds.top - 20f,
                                right = bounds.right + 20f,
                                bottom = bounds.bottom + 20f
                            )
                            
                            if (expandedBounds.contains(svgPoint)) {
                                onPathTapped(sectorId)
                                return@detectTapGestures
                            }
                        }
                    } ?: run {
                        // Fallback for no dimensions - check in canvas space
                        pathBoundsMap.forEach { (sectorId, bounds) ->
                            val expandedBounds = Rect(
                                left = bounds.left - 20f,
                                top = bounds.top - 20f,
                                right = bounds.right + 20f,
                                bottom = bounds.bottom + 20f
                            )
                            
                            if (expandedBounds.contains(tapOffset)) {
                                onPathTapped(sectorId)
                                return@detectTapGestures
                            }
                        }
                    }
                }
            }
    ) {
        // Apply user pan/zoom transformations manually in the draw scope
        translate(offsetX, offsetY) {
            scale(scale, scale) {
                svgDimensions?.let { dims ->
                    // Calculate base scale to fit the viewBox width to the canvas width
                    val baseScale = size.width / scale / dims.viewBoxWidth
                    
                    // Set initial offset to align bottom-right on first draw
                    if (!initialOffsetSet && size.width > 0 && size.height > 0) {
                        // Calculate the SVG content size in canvas space
                        val svgCanvasWidth = dims.viewBoxWidth * baseScale
                        val svgCanvasHeight = dims.viewBoxHeight * baseScale
                        
                        // Align bottom-right: offset so that bottom-right of SVG aligns with bottom-right of canvas
                        offsetX = size.width - svgCanvasWidth * scale
                        offsetY = size.height - svgCanvasHeight * scale
                        initialOffsetSet = true
                    }
                    
                    // Apply transformation to scale the SVG (top-left aligned within transformed space)
                    translate(-dims.viewBoxX * baseScale, -dims.viewBoxY * baseScale) {
                        scale(baseScale) {
                            // Now we're in SVG coordinate space, scaled and aligned to top-left
                            svgPaths.forEach { pathData ->
                                val color = if (pathData.sectorId == selectedSectorId) {
                                    Color.Red
                                } else {
                                    Color.Black
                                }
                                
                                // Increased stroke width for better visibility
                                val strokeWidth = if (pathData.sectorId == selectedSectorId) {
                                    10f
                                } else {
                                    8f
                                }
                                
                                drawPath(
                                    path = pathData.path,
                                    color = color,
                                    style = Stroke(width = strokeWidth)
                                )
                            }
                        }
                    }
                } ?: run {
                    // If no dimensions, just draw the paths as-is
                    svgPaths.forEach { pathData ->
                        val color = if (pathData.sectorId == selectedSectorId) {
                            Color.Red
                        } else {
                            Color.Black
                        }
                        
                        drawPath(
                            path = pathData.path,
                            color = color,
                            style = Stroke(width = if (pathData.sectorId == selectedSectorId) 10f else 8f)
                        )
                    }
                }
            }
        }
    }
}
