package com.example.topoclimb.utils

import org.junit.Test
import org.junit.Assert.*

class SvgParserTest {
    
    // Note: These tests focus on viewBox dimension parsing, which is the core fix.
    // Path parsing tests are limited because PathParser requires Android runtime components
    // that are not available in unit tests (requires instrumented tests instead).
    
    @Test
    fun parseSvg_withNormalSpacing_parsesViewBoxCorrectly() {
        val svgContent = """
            <?xml version="1.0" encoding="UTF-8"?>
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 200">
                <path d="M 10 10 L 20 20" id="sector_1"/>
            </svg>
        """.trimIndent()
        
        val (dimensions, _) = SvgParser.parseSvg(svgContent)
        
        assertNotNull(dimensions)
        assertEquals(0f, dimensions!!.viewBoxX, 0.001f)
        assertEquals(0f, dimensions.viewBoxY, 0.001f)
        assertEquals(100f, dimensions.viewBoxWidth, 0.001f)
        assertEquals(200f, dimensions.viewBoxHeight, 0.001f)
    }
    
    @Test
    fun parseSvg_withMultipleSpaces_parsesViewBoxCorrectly() {
        // This is the key test for the bug fix!
        // Multiple spaces in viewBox would cause parsing to fail before the fix
        val svgContent = """
            <?xml version="1.0" encoding="UTF-8"?>
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0  0  100  200">
                <path d="M 10 10 L 20 20" id="sector_1"/>
            </svg>
        """.trimIndent()
        
        val (dimensions, _) = SvgParser.parseSvg(svgContent)
        
        assertNotNull(dimensions)
        assertEquals(0f, dimensions!!.viewBoxX, 0.001f)
        assertEquals(0f, dimensions.viewBoxY, 0.001f)
        assertEquals(100f, dimensions.viewBoxWidth, 0.001f)
        assertEquals(200f, dimensions.viewBoxHeight, 0.001f)
    }
    
    @Test
    fun parseSvg_withLeadingTrailingSpaces_parsesViewBoxCorrectly() {
        val svgContent = """
            <?xml version="1.0" encoding="UTF-8"?>
            <svg xmlns="http://www.w3.org/2000/svg" viewBox=" 0 0 100 200 ">
                <path d="M 10 10 L 20 20" id="sector_1"/>
            </svg>
        """.trimIndent()
        
        val (dimensions, _) = SvgParser.parseSvg(svgContent)
        
        assertNotNull(dimensions)
        assertEquals(0f, dimensions!!.viewBoxX, 0.001f)
        assertEquals(0f, dimensions.viewBoxY, 0.001f)
        assertEquals(100f, dimensions.viewBoxWidth, 0.001f)
        assertEquals(200f, dimensions.viewBoxHeight, 0.001f)
    }
    
    @Test
    fun parseSvg_withNonZeroViewBoxOffset_parsesCorrectly() {
        val svgContent = """
            <?xml version="1.0" encoding="UTF-8"?>
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="10 20 100 200">
                <path d="M 10 10 L 20 20" id="sector_1"/>
            </svg>
        """.trimIndent()
        
        val (dimensions, _) = SvgParser.parseSvg(svgContent)
        
        assertNotNull(dimensions)
        assertEquals(10f, dimensions!!.viewBoxX, 0.001f)
        assertEquals(20f, dimensions.viewBoxY, 0.001f)
        assertEquals(100f, dimensions.viewBoxWidth, 0.001f)
        assertEquals(200f, dimensions.viewBoxHeight, 0.001f)
    }
    
    @Test
    fun parseSvg_withDecimalViewBoxValues_parsesCorrectly() {
        val svgContent = """
            <?xml version="1.0" encoding="UTF-8"?>
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 786.15863 583.85938">
                <path d="M 10 10 L 20 20" id="sector_1"/>
            </svg>
        """.trimIndent()
        
        val (dimensions, _) = SvgParser.parseSvg(svgContent)
        
        assertNotNull(dimensions)
        assertEquals(0f, dimensions!!.viewBoxX, 0.001f)
        assertEquals(0f, dimensions.viewBoxY, 0.001f)
        assertEquals(786.15863f, dimensions.viewBoxWidth, 0.001f)
        assertEquals(583.85938f, dimensions.viewBoxHeight, 0.001f)
    }
    
    @Test
    fun parseSvg_withNoViewBox_returnsNullDimensions() {
        val svgContent = """
            <?xml version="1.0" encoding="UTF-8"?>
            <svg xmlns="http://www.w3.org/2000/svg">
                <path d="M 10 10 L 20 20" id="sector_1"/>
            </svg>
        """.trimIndent()
        
        val (dimensions, _) = SvgParser.parseSvg(svgContent)
        
        assertNull(dimensions)
    }
    
    @Test
    fun parseSvg_withInvalidXml_returnsEmptyResult() {
        val svgContent = "This is not valid XML"
        
        val (dimensions, paths) = SvgParser.parseSvg(svgContent)
        
        assertNull(dimensions)
        assertTrue(paths.isEmpty())
    }
    
    @Test
    fun parseSvg_withWidthAndHeight_extractsDimensions() {
        val svgContent = """
            <?xml version="1.0" encoding="UTF-8"?>
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 200" width="500" height="1000">
                <path d="M 10 10 L 20 20" id="sector_1"/>
            </svg>
        """.trimIndent()
        
        val (dimensions, _) = SvgParser.parseSvg(svgContent)
        
        assertNotNull(dimensions)
        assertEquals(500f, dimensions!!.width!!, 0.001f)
        assertEquals(1000f, dimensions.height!!, 0.001f)
    }
    
    @Test
    fun parseSvg_withWidthAndHeightPx_extractsDimensions() {
        val svgContent = """
            <?xml version="1.0" encoding="UTF-8"?>
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 200" width="500px" height="1000px">
                <path d="M 10 10 L 20 20" id="sector_1"/>
            </svg>
        """.trimIndent()
        
        val (dimensions, _) = SvgParser.parseSvg(svgContent)
        
        assertNotNull(dimensions)
        assertEquals(500f, dimensions!!.width!!, 0.001f)
        assertEquals(1000f, dimensions.height!!, 0.001f)
    }
    
    @Test
    fun parseSvg_realWorldExample_parsesViewBoxCorrectly() {
        // This is based on the SVG from the problem statement
        val svgContent = """
            <?xml version="1.0" encoding="UTF-8" standalone="no"?>
            <svg xmlns="http://www.w3.org/2000/svg" version="1.1" viewBox="0 0 786.15863 583.85938">
              <g id="layer1" stroke="#000000" stroke-width="10">
                <path d="M 670.35026,578.359382 H 781.15864 V 449.082932 L 744.22249,282.870362" id="sector_1"/>
                <path d="M 744.22249,282.870362 633.41411,227.466172 522.60577,42.785531" id="sector_2"/>
              </g>
            </svg>
        """.trimIndent()
        
        val (dimensions, _) = SvgParser.parseSvg(svgContent)
        
        assertNotNull(dimensions)
        assertEquals(0f, dimensions!!.viewBoxX, 0.001f)
        assertEquals(0f, dimensions.viewBoxY, 0.001f)
        assertEquals(786.15863f, dimensions.viewBoxWidth, 0.001f)
        assertEquals(583.85938f, dimensions.viewBoxHeight, 0.001f)
    }
}
