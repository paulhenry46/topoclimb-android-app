package com.example.topoclimb.ui.utils

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils

/**
 * Generates a Material3 ColorScheme from a hex color string.
 * The primary color is derived from the input, and other colors are computed
 * to ensure good contrast and visual harmony.
 * 
 * @param colorHex Hex color string (e.g., "#FF5733")
 * @param isDark Whether to generate a dark color scheme
 * @return A Material3 ColorScheme based on the input color
 */
fun generateColorSchemeFromHex(colorHex: String?, isDark: Boolean = false): ColorScheme {
    val primaryColor = parseRouteColor(colorHex)
    return generateColorSchemeFromColor(primaryColor, isDark)
}

/**
 * Generates a Material3 ColorScheme from a Color.
 * The primary color is the input, and other colors are computed
 * to ensure good contrast and visual harmony.
 * 
 * @param primaryColor The primary color to base the scheme on
 * @param isDark Whether to generate a dark color scheme
 * @return A Material3 ColorScheme based on the input color
 */
fun generateColorSchemeFromColor(primaryColor: Color, isDark: Boolean = false): ColorScheme {
    val primaryArgb = primaryColor.toArgb()
    
    // Calculate luminance to determine if the color is light or dark
    val isLightPrimary = primaryColor.luminance() > 0.5f
    
    // Generate harmonious colors
    val onPrimary = if (isLightPrimary) Color.Black else Color.White
    
    // Create a lighter/darker version for containers
    val primaryContainer = if (isDark) {
        adjustBrightness(primaryColor, 0.3f)
    } else {
        adjustBrightness(primaryColor, 1.5f)
    }
    
    val onPrimaryContainer = if (primaryContainer.luminance() > 0.5f) {
        Color.Black
    } else {
        Color.White
    }
    
    // Generate secondary color (complementary hue shift)
    val secondaryArgb = ColorUtils.blendARGB(primaryArgb, 0xFF888888.toInt(), 0.5f)
    val secondary = Color(secondaryArgb)
    
    val onSecondary = if (secondary.luminance() > 0.5f) Color.Black else Color.White
    
    val secondaryContainer = if (isDark) {
        adjustBrightness(secondary, 0.3f)
    } else {
        adjustBrightness(secondary, 1.5f)
    }
    
    val onSecondaryContainer = if (secondaryContainer.luminance() > 0.5f) {
        Color.Black
    } else {
        Color.White
    }
    
    // Generate tertiary color (analogous hue shift)
    val tertiaryArgb = rotateHue(primaryArgb, 30f)
    val tertiary = Color(tertiaryArgb)
    
    val onTertiary = if (tertiary.luminance() > 0.5f) Color.Black else Color.White
    
    val tertiaryContainer = if (isDark) {
        adjustBrightness(tertiary, 0.3f)
    } else {
        adjustBrightness(tertiary, 1.5f)
    }
    
    val onTertiaryContainer = if (tertiaryContainer.luminance() > 0.5f) {
        Color.Black
    } else {
        Color.White
    }
    
    // Background and surface colors
    val background = if (isDark) Color(0xFF1C1B1F) else Color(0xFFFFFBFE)
    val onBackground = if (isDark) Color(0xFFE6E1E5) else Color(0xFF1C1B1F)
    
    val surface = if (isDark) Color(0xFF1C1B1F) else Color(0xFFFFFBFE)
    val onSurface = if (isDark) Color(0xFFE6E1E5) else Color(0xFF1C1B1F)
    
    val surfaceVariant = if (isDark) Color(0xFF49454F) else Color(0xFFE7E0EC)
    val onSurfaceVariant = if (isDark) Color(0xFFCAC4D0) else Color(0xFF49454F)
    
    // Error colors (standard Material3)
    val error = if (isDark) Color(0xFFF2B8B5) else Color(0xFFB3261E)
    val onError = if (isDark) Color(0xFF601410) else Color(0xFFFFFFFF)
    val errorContainer = if (isDark) Color(0xFF8C1D18) else Color(0xFFF9DEDC)
    val onErrorContainer = if (isDark) Color(0xFFF2B8B5) else Color(0xFF410E0B)
    
    // Outline colors
    val outline = if (isDark) Color(0xFF938F99) else Color(0xFF79747E)
    val outlineVariant = if (isDark) Color(0xFF49454F) else Color(0xFFCAC4D0)
    
    return if (isDark) {
        darkColorScheme(
            primary = primaryColor,
            onPrimary = onPrimary,
            primaryContainer = primaryContainer,
            onPrimaryContainer = onPrimaryContainer,
            secondary = secondary,
            onSecondary = onSecondary,
            secondaryContainer = secondaryContainer,
            onSecondaryContainer = onSecondaryContainer,
            tertiary = tertiary,
            onTertiary = onTertiary,
            tertiaryContainer = tertiaryContainer,
            onTertiaryContainer = onTertiaryContainer,
            background = background,
            onBackground = onBackground,
            surface = surface,
            onSurface = onSurface,
            surfaceVariant = surfaceVariant,
            onSurfaceVariant = onSurfaceVariant,
            error = error,
            onError = onError,
            errorContainer = errorContainer,
            onErrorContainer = onErrorContainer,
            outline = outline,
            outlineVariant = outlineVariant
        )
    } else {
        lightColorScheme(
            primary = primaryColor,
            onPrimary = onPrimary,
            primaryContainer = primaryContainer,
            onPrimaryContainer = onPrimaryContainer,
            secondary = secondary,
            onSecondary = onSecondary,
            secondaryContainer = secondaryContainer,
            onSecondaryContainer = onSecondaryContainer,
            tertiary = tertiary,
            onTertiary = onTertiary,
            tertiaryContainer = tertiaryContainer,
            onTertiaryContainer = onTertiaryContainer,
            background = background,
            onBackground = onBackground,
            surface = surface,
            onSurface = onSurface,
            surfaceVariant = surfaceVariant,
            onSurfaceVariant = onSurfaceVariant,
            error = error,
            onError = onError,
            errorContainer = errorContainer,
            onErrorContainer = onErrorContainer,
            outline = outline,
            outlineVariant = outlineVariant
        )
    }
}

/**
 * Adjusts the brightness of a color by a factor.
 * 
 * @param color The input color
 * @param factor Brightness factor (> 1 brightens, < 1 darkens)
 * @return Adjusted color
 */
private fun adjustBrightness(color: Color, factor: Float): Color {
    val argb = color.toArgb()
    val hsv = FloatArray(3)
    android.graphics.Color.colorToHSV(argb, hsv)
    hsv[2] = (hsv[2] * factor).coerceIn(0f, 1f)
    return Color(android.graphics.Color.HSVToColor(hsv))
}

/**
 * Rotates the hue of a color by a specified amount.
 * 
 * @param argb The input color in ARGB format
 * @param degrees Degrees to rotate the hue (0-360)
 * @return Rotated color in ARGB format
 */
private fun rotateHue(argb: Int, degrees: Float): Int {
    val hsv = FloatArray(3)
    android.graphics.Color.colorToHSV(argb, hsv)
    hsv[0] = (hsv[0] + degrees) % 360f
    return android.graphics.Color.HSVToColor(hsv)
}
