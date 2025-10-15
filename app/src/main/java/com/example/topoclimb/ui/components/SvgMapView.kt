package com.example.topoclimb.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
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

/**
 * Renders an SVG map with interactive sectors.
 * 
 * This component uses the viewBox to properly position and scale SVG paths.
 * The transformation logic:
 * 1. Calculate scale factor to fit viewBox content to canvas
 * 2. Apply translation to account for viewBox origin offset
 * 3. Apply scale to fit content to canvas size
 * 
 * All coordinates in SVG paths are in viewBox coordinate space.
 * We transform them to canvas coordinate space using: canvas = (svg - viewBoxOrigin) * scale
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
    
    // Calculate aspect ratio from SVG dimensions
    val aspectRatioModifier = svgDimensions?.let { dims ->
        Modifier.aspectRatio(dims.viewBoxWidth / dims.viewBoxHeight)
    } ?: Modifier
    
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .then(aspectRatioModifier)
            .pointerInput(svgPaths, svgDimensions) {
                detectTapGestures { tapOffset ->
                    svgDimensions?.let { dims ->
                        // Calculate scale factor to fit viewBox to canvas
                        val scaleX = size.width / dims.viewBoxWidth
                        val scaleY = size.height / dims.viewBoxHeight
                        val scale = minOf(scaleX, scaleY)
                        
                        // Transform tap position from canvas to SVG coordinates
                        // Reverse the transformation: canvas = (svg - viewBoxOrigin) * scale
                        // So: svg = (canvas / scale) + viewBoxOrigin
                        val svgX = (tapOffset.x / scale) + dims.viewBoxX
                        val svgY = (tapOffset.y / scale) + dims.viewBoxY
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
        svgDimensions?.let { dims ->
            // Calculate scale to fit the viewBox content to the canvas
            // Use uniform scaling to maintain aspect ratio
            val scaleX = size.width / dims.viewBoxWidth
            val scaleY = size.height / dims.viewBoxHeight
            val scale = minOf(scaleX, scaleY)
            
            // Apply transformation: 
            // 1. Translate to move viewBox origin to (0,0) in SVG space
            // 2. Scale to fit canvas size
            // The transformation is: canvas = (svg - viewBoxOrigin) * scale
            // Which we achieve by: translate(-viewBoxOrigin * scale) then scale(scale)
            translate(-dims.viewBoxX * scale, -dims.viewBoxY * scale) {
                scale(scale) {
                    // Now we're in SVG coordinate space, scaled to canvas
                    svgPaths.forEach { pathData ->
                        val color = if (pathData.sectorId == selectedSectorId) {
                            Color.Red
                        } else {
                            Color.Black
                        }
                        
                        val strokeWidth = if (pathData.sectorId == selectedSectorId) {
                            6f / scale
                        } else {
                            4f / scale
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
                    style = Stroke(width = if (pathData.sectorId == selectedSectorId) 6f else 4f)
                )
            }
        }
    }
}
