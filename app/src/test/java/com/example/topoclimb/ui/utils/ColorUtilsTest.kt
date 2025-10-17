package com.example.topoclimb.ui.utils

import androidx.compose.ui.graphics.toArgb
import com.example.topoclimb.ui.theme.Purple40
import org.junit.Test
import org.junit.Assert.*

/**
 * Tests for ColorUtils
 * 
 * Note: These tests only cover the fallback scenarios since android.graphics.Color
 * is not available in standard unit tests without Robolectric.
 * Valid hex color parsing is verified through manual testing and will be covered
 * by instrumented tests in the future.
 */
class ColorUtilsTest {
    
    @Test
    fun parseRouteColor_nullColor_returnsFallback() {
        // Test with null color
        val result = parseRouteColor(null)
        
        // Should return the app's main color (Purple40)
        assertEquals(Purple40.toArgb(), result.toArgb())
    }
    
    @Test
    fun parseRouteColor_emptyString_returnsFallback() {
        // Test with empty string
        val result = parseRouteColor("")
        
        // Should return the app's main color (Purple40)
        assertEquals(Purple40.toArgb(), result.toArgb())
    }
    
    @Test
    fun parseRouteColor_invalidFormat_returnsFallback() {
        // Test with color not starting with #
        val result = parseRouteColor("FF5733")
        
        // Should return the app's main color (Purple40)
        assertEquals(Purple40.toArgb(), result.toArgb())
    }
    
    @Test
    fun parseRouteColor_invalidHexValue_returnsFallback() {
        // Test with invalid hex characters
        val result = parseRouteColor("#GGGGGG")
        
        // Should return the app's main color (Purple40) on error
        assertEquals(Purple40.toArgb(), result.toArgb())
    }
    
    @Test
    fun parseRouteColor_randomString_returnsFallback() {
        // Test with random string
        val result = parseRouteColor("not a color")
        
        // Should return the app's main color (Purple40)
        assertEquals(Purple40.toArgb(), result.toArgb())
    }
}
