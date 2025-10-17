package com.example.topoclimb.ui.utils

import androidx.compose.ui.graphics.Color
import com.example.topoclimb.ui.theme.Purple40

/**
 * Parse a hex color string and return a Compose Color
 * Falls back to Purple40 (app's main color) for null values, invalid hex format, or parsing errors.
 * 
 * @param colorHex Hex color string (e.g., "#FF5733", "#F00", "#80FF5733")
 * @return Compose Color
 */
fun parseRouteColor(colorHex: String?): Color {
    return try {
        if (colorHex != null && colorHex.startsWith("#")) {
            Color(android.graphics.Color.parseColor(colorHex))
        } else {
            Purple40 // Use app's main color as fallback
        }
    } catch (e: Exception) {
        Purple40 // Use app's main color as fallback on error
    }
}
