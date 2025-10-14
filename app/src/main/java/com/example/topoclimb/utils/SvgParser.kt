package com.example.topoclimb.utils

import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.PathParser
import org.w3c.dom.Element
import org.w3c.dom.NodeList
import java.io.StringReader
import javax.xml.parsers.DocumentBuilderFactory

data class SvgPathData(
    val path: Path,
    val sectorId: Int?,
    val originalPathString: String
)

data class SvgDimensions(
    val viewBoxX: Float,
    val viewBoxY: Float,
    val viewBoxWidth: Float,
    val viewBoxHeight: Float,
    val width: Float?,
    val height: Float?
)

object SvgParser {
    
    fun parseSvg(svgContent: String): Pair<SvgDimensions?, List<SvgPathData>> {
        try {
            val factory = DocumentBuilderFactory.newInstance()
            val builder = factory.newDocumentBuilder()
            val doc = builder.parse(StringReader(svgContent).let {
                org.xml.sax.InputSource(it)
            })
            
            val svgElement = doc.documentElement
            val dimensions = extractDimensions(svgElement)
            
            val pathElements = doc.getElementsByTagName("path")
            val paths = mutableListOf<SvgPathData>()
            
            for (i in 0 until pathElements.length) {
                val pathElement = pathElements.item(i) as? Element ?: continue
                val pathString = pathElement.getAttribute("d") ?: continue
                
                // Extract sector_id from id attribute (e.g., id="sector_123")
                val idAttr = pathElement.getAttribute("id")
                val sectorId = extractSectorId(idAttr)
                
                try {
                    val composePath = PathParser().parsePathString(pathString).toPath()
                    paths.add(SvgPathData(composePath, sectorId, pathString))
                } catch (e: Exception) {
                    // Skip paths that can't be parsed
                    e.printStackTrace()
                }
            }
            
            return Pair(dimensions, paths)
        } catch (e: Exception) {
            e.printStackTrace()
            return Pair(null, emptyList())
        }
    }
    
    private fun extractDimensions(svgElement: Element): SvgDimensions? {
        return try {
            val viewBox = svgElement.getAttribute("viewBox")
            val parts = viewBox.trim().split(Regex("\\s+")).map { it.toFloat() }
            
            val widthStr = svgElement.getAttribute("width")
            val heightStr = svgElement.getAttribute("height")
            
            val width = widthStr.takeIf { it.isNotEmpty() }?.replace("px", "")?.toFloatOrNull()
            val height = heightStr.takeIf { it.isNotEmpty() }?.replace("px", "")?.toFloatOrNull()
            
            SvgDimensions(
                viewBoxX = parts[0],
                viewBoxY = parts[1],
                viewBoxWidth = parts[2],
                viewBoxHeight = parts[3],
                width = width,
                height = height
            )
        } catch (e: Exception) {
            null
        }
    }
    
    private fun extractSectorId(idAttribute: String?): Int? {
        if (idAttribute.isNullOrEmpty()) return null
        
        // Extract sector ID from attributes like "sector_123" or "sector-123"
        val regex = Regex("sector[_-]?(\\d+)", RegexOption.IGNORE_CASE)
        val match = regex.find(idAttribute)
        return match?.groupValues?.getOrNull(1)?.toIntOrNull()
    }
}
