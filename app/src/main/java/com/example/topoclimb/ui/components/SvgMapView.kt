package com.example.topoclimb.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.topoclimb.utils.SvgDimensions
import com.example.topoclimb.utils.SvgPathData

@Composable
fun SvgMapView(
    svgPaths: List<SvgPathData>,
    svgDimensions: SvgDimensions?,
    selectedSectorId: Int?,
    onPathTapped: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    // Pre-compute path bounds for hit testing
    val pathBoundsMap = remember(svgPaths) {
        svgPaths.mapNotNull { pathData ->
            pathData.sectorId?.let { sectorId ->
                val bounds = pathData.path.getBounds()
                sectorId to bounds
            }
        }.toMap()
    }
    
    // Calculate aspect ratio from SVG dimensions
    val aspectRatioModifier = svgDimensions?.let { dims ->
        Modifier.aspectRatio(dims.viewBoxWidth / dims.viewBoxHeight)
    } ?: Modifier.height(300.dp)  // Fallback height when no dimensions available
    
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .then(aspectRatioModifier)
            .pointerInput(svgPaths, svgDimensions) {
                detectTapGestures { tapOffset ->
                    val canvasWidth = size.width.toFloat()
                    val canvasHeight = size.height.toFloat()
                    
                    svgDimensions?.let { dims ->
                        // Calculate the same scale and translation used for drawing
                        // Use scaleX to fill full width
                        val scaleX = canvasWidth / dims.viewBoxWidth
                        val scale = scaleX  // Use full width scaling
                        
                        val translateX = -dims.viewBoxX * scale
                        val translateY = -dims.viewBoxY * scale
                        
                        // Transform tap offset to SVG coordinates
                        val svgX = (tapOffset.x - translateX) / scale
                        val svgY = (tapOffset.y - translateY) / scale
                        val svgPoint = Offset(svgX, svgY)
                        
                        // Find which path was tapped using bounds checking
                        pathBoundsMap.forEach { (sectorId, bounds) ->
                            // Expand bounds for easier tapping (20 units tolerance, increased from 10)
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
                        // Fallback for no dimensions
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
        val canvasWidth = size.width
        val canvasHeight = size.height
        
        svgDimensions?.let { dims ->
            // Calculate scale to fit the SVG width to the canvas width
            val scaleX = canvasWidth / dims.viewBoxWidth
            val scale = scaleX  // Use full width scaling
            
            // Align to top-left (no centering)
            val translateX = -dims.viewBoxX * scale
            val translateY = -dims.viewBoxY * scale
            
            translate(translateX, translateY) {
                scale(scale, scale) {
                    svgPaths.forEach { pathData ->
                        val color = if (pathData.sectorId == selectedSectorId) {
                            Color.Red
                        } else {
                            Color.Black
                        }
                        
                        // Increased stroke width for easier tapping
                        val strokeWidth = if (pathData.sectorId == selectedSectorId) {
                            6f / scale  // Increased from 3f
                        } else {
                            4f / scale  // Increased from 2f
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
                
                // Increased stroke width for easier tapping
                val strokeWidth = if (pathData.sectorId == selectedSectorId) {
                    6f  // Increased from 3f
                } else {
                    4f  // Increased from 2f
                }
                
                drawPath(
                    path = pathData.path,
                    color = color,
                    style = Stroke(width = strokeWidth)
                )
            }
        }
    }
}
