package com.example.topoclimb.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = LightBlue80,
    onPrimary = DarkGray,
    primaryContainer = LightBlue60,
    onPrimaryContainer = Black,
    secondary = Gray80,
    onSecondary = DarkGray,
    secondaryContainer = Gray60,
    onSecondaryContainer = White,
    tertiary = LightBlue60,
    onTertiary = DarkGray,
    tertiaryContainer = Gray60,
    onTertiaryContainer = White,
    background = DarkGray,
    onBackground = White,
    surface = Color(0xFF1C1C1C),
    onSurface = White,
    surfaceVariant = Gray40,
    onSurfaceVariant = Gray80,
    error = Color(0xFFCF6679),
    onError = Black,
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    outline = Gray60,
    outlineVariant = Gray40
)

private val LightColorScheme = lightColorScheme(
    primary = LightBlue40,
    onPrimary = White,
    primaryContainer = LightBlue80,
    onPrimaryContainer = DarkGray,
    secondary = Gray60,
    onSecondary = White,
    secondaryContainer = Gray80,
    onSecondaryContainer = DarkGray,
    tertiary = LightBlue60,
    onTertiary = White,
    tertiaryContainer = LightBlue80,
    onTertiaryContainer = DarkGray,
    background = LightGray,
    onBackground = DarkGray,
    surface = White,
    onSurface = DarkGray,
    surfaceVariant = Gray80,
    onSurfaceVariant = Gray40,
    error = Color(0xFFB3261E),
    onError = White,
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B),
    outline = Gray60,
    outlineVariant = Gray80
)

@Composable
fun TopoClimbTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}