package com.example.topoclimb.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
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
    
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(svgPaths, svgDimensions) {
                detectTapGestures { tapOffset ->
                    val canvasWidth = size.width.toFloat()
                    val canvasHeight = size.height.toFloat()
                    
                    svgDimensions?.let { dims ->
                        // Calculate the same scale and translation used for drawing
                        val scaleX = canvasWidth / dims.viewBoxWidth
                        val scaleY = canvasHeight / dims.viewBoxHeight
                        val scale = minOf(scaleX, scaleY)
                        
                        val translateX = (canvasWidth - dims.viewBoxWidth * scale) / 2 - dims.viewBoxX * scale
                        val translateY = (canvasHeight - dims.viewBoxHeight * scale) / 2 - dims.viewBoxY * scale
                        
                        // Transform tap offset to SVG coordinates
                        val svgX = (tapOffset.x - translateX) / scale
                        val svgY = (tapOffset.y - translateY) / scale
                        val svgPoint = Offset(svgX, svgY)
                        
                        // Find which path was tapped using bounds checking
                        pathBoundsMap.forEach { (sectorId, bounds) ->
                            // Expand bounds slightly for easier tapping (10 units tolerance)
                            val expandedBounds = Rect(
                                left = bounds.left - 10f,
                                top = bounds.top - 10f,
                                right = bounds.right + 10f,
                                bottom = bounds.bottom + 10f
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
                                left = bounds.left - 10f,
                                top = bounds.top - 10f,
                                right = bounds.right + 10f,
                                bottom = bounds.bottom + 10f
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
            // Calculate scale to fit the SVG into the canvas
            val scaleX = canvasWidth / dims.viewBoxWidth
            val scaleY = canvasHeight / dims.viewBoxHeight
            val scale = minOf(scaleX, scaleY)
            
            // Center the SVG
            val translateX = (canvasWidth - dims.viewBoxWidth * scale) / 2 - dims.viewBoxX * scale
            val translateY = (canvasHeight - dims.viewBoxHeight * scale) / 2 - dims.viewBoxY * scale
            
            translate(translateX, translateY) {
                scale(scale, scale) {
                    svgPaths.forEach { pathData ->
                        val color = if (pathData.sectorId == selectedSectorId) {
                            Color.Red
                        } else {
                            Color.Black
                        }
                        
                        val strokeWidth = if (pathData.sectorId == selectedSectorId) {
                            3f / scale  // Thicker stroke for selected path
                        } else {
                            2f / scale
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
                
                val strokeWidth = if (pathData.sectorId == selectedSectorId) {
                    3f  // Thicker stroke for selected path
                } else {
                    2f
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
