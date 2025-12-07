package com.example.topoclimb.ui.theme

import androidx.compose.ui.graphics.Color

// Main theme colors - Gray/Black/White with light blue accent
val LightBlue80 = Color(0xFFB3E5FC) // Light blue for dark theme (old)
val LightBlue40 = Color(0xFF0288D1) // Light blue for light theme (old)
val LightBlue60 = Color(0xFF4FC3F7) // Medium light blue (old)

val Gray80 = Color(0xFFE0E0E0)
val Gray40 = Color(0xFF616161)
val Gray60 = Color(0xFF9E9E9E)

val Black = Color(0xFF000000)
val White = Color(0xFFFFFFFF)
val DarkGray = Color(0xFF212121)
val LightGray = Color(0xFFF5F5F5)

// Updated dark theme background colors (elevation 0)
val NewBackground = Color(0xFF131313) // New default dark background
// Surface colors (elevation 1)
val NewSurface = Color(0xFF1e1f21) // New default surface/container/cards
// Surface2 colors (elevation 2)
val NewSurface2 = Color(0xFF343537) // Secondary surface color for components on top of surface

// Success colors
val SuccessSurface = Color(0xFF1f3a2b) // Success surface color
val OnSuccessSurface = Color(0xFF78dea5) // On success surface color

// Bottom navigation bar and chips colors
val IconBottomBar = Color(0xFFc2e6ff) // Icon color for bottom bar and text on chips
val IconBackground = Color(0xFF004a77) // Icon background and chip background

// New primary colors
val NewPrimary = Color(0xFFa8c8fb) // New primary color
val NewOnPrimary = Color(0xFF062d6e) // New onPrimary color (text on buttons)

// Old colors kept for backwards compatibility
val DarkBlueBackground = Color(0xFF0e1326) // Old default dark background
val DarkBlueSurface = Color(0xFF232a3d) // Old default card/surface color

// Warning/Orange colors for less aggressive notifications
val Orange80 = Color(0xFFFFCC80) // Light orange for dark theme
val Orange40 = Color(0xFFFF9800) // Orange for light theme
val Orange60 = Color(0xFFFFB74D) // Medium orange

// Keep old colors for backwards compatibility if needed elsewhere
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)