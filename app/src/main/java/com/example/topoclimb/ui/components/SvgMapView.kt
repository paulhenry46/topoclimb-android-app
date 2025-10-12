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
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    // Find which path was tapped
                    svgPaths.forEach { pathData ->
                        if (pathData.sectorId != null) {
                            // Simple hit test - check if point is near the path
                            // This is a simplified approach; a proper implementation would use path bounds
                            onPathTapped(pathData.sectorId)
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
                        
                        drawPath(
                            path = pathData.path,
                            color = color,
                            style = Stroke(width = 2f / scale)
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
                    style = Stroke(width = 2f)
                )
            }
        }
    }
}
