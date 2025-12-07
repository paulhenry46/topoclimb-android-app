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

// Default dark color scheme with new background colors
private val DarkColorScheme = darkColorScheme(
    primary = NewPrimary,
    onPrimary = NewOnPrimary,
    primaryContainer = IconBackground,
    onPrimaryContainer = IconBottomBar,
    secondary = Gray80,
    onSecondary = DarkGray,
    secondaryContainer = Gray60,
    onSecondaryContainer = White,
    tertiary = OnSuccessSurface,
    onTertiary = SuccessSurface,
    tertiaryContainer = SuccessSurface,
    onTertiaryContainer = OnSuccessSurface,
    background = NewBackground,
    onBackground = White,
    surface = NewSurface,
    onSurface = White,
    surfaceVariant = NewSurface2,
    onSurfaceVariant = Gray80,
    surfaceContainer = NewSurface,
    surfaceContainerHigh = NewSurface2,
    surfaceContainerHighest = NewSurface2,
    surfaceContainerLow = NewSurface,
    surfaceContainerLowest = NewBackground,
    error = Color(0xFFCF6679),
    onError = Black,
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    outline = Gray60,
    outlineVariant = Gray40
)

// OLED dark color scheme (true black for OLED displays)
private val OledDarkColorScheme = darkColorScheme(
    primary = NewPrimary,
    onPrimary = NewOnPrimary,
    primaryContainer = IconBackground,
    onPrimaryContainer = IconBottomBar,
    secondary = Gray80,
    onSecondary = DarkGray,
    secondaryContainer = Gray60,
    onSecondaryContainer = White,
    tertiary = OnSuccessSurface,
    onTertiary = SuccessSurface,
    tertiaryContainer = SuccessSurface,
    onTertiaryContainer = OnSuccessSurface,
    background = Black,
    onBackground = White,
    surface = NewSurface,
    onSurface = White,
    surfaceVariant = NewSurface2,
    onSurfaceVariant = Gray80,
    surfaceContainer = NewSurface,
    surfaceContainerHigh = NewSurface2,
    surfaceContainerHighest = NewSurface2,
    surfaceContainerLow = NewSurface,
    surfaceContainerLowest = Black,
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
    useOledDark: Boolean = false,
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> if (useOledDark) OledDarkColorScheme else DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}