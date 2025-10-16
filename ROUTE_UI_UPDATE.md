# Route Display Component Update

## Overview

The route display component has been updated to provide a more beautiful and modern UI using Jetpack Compose. Each route is displayed on a single line with improved visual hierarchy and better use of space.

## Changes Made

### Data Model Updates

The `Route` data class has been extended to include new properties from the API:

```kotlin
data class Route(
    // ... existing fields
    val thumbnail: String?,        // URL of the route thumbnail
    val color: String?,            // Hex color code for the route (e.g., "#FF5722")
    val lineLocalId: String?,      // Local ID of the line
    val sectorLocalId: String?,    // Local ID of the sector
    val lineCount: Int?            // Number of lines in the sector
)
```

### UI Component Updates

The `RouteItem` composable has been completely redesigned with a horizontal layout:

## Before vs After

### Route Card Layout - BEFORE

```
╔══════════════════════════════════════════════════════╗
║  Route Name                         7a               ║
║  sport  15m                                          ║
║  Classic Font problem...                             ║
╚══════════════════════════════════════════════════════╝
```

### Route Card Layout - AFTER

```
╔══════════════════════════════════════════════════════╗
║  [Image]  ┃ 7a ┃  Route Name                         ║
║   60px    ┃    ┃  Line 3B                            ║
║  (round)  ┃    ┃                                     ║
╚══════════════════════════════════════════════════════╝
```

## Visual Design Details

### 1. Route Thumbnail (Left)
- **Size**: 60x60 dp
- **Shape**: Circular (fully rounded)
- **Position**: Left-most element
- **Content**: Route thumbnail image from API
- **Loading**: Uses Coil library for efficient image loading
- **Fallback**: Shows placeholder if no thumbnail available

### 2. Grade Badge (Center-Left)
- **Background**: Colored box using the route's `color` property (hex format)
- **Shape**: Rounded corners (8dp radius)
- **Padding**: 12dp horizontal, 8dp vertical
- **Text**: Grade (e.g., "7a", "6b+")
- **Text Style**: Bold, 16sp, white color
- **Fallback Color**: Material Purple (#6200EE) if no color provided

### 3. Route Information (Right)
- **Layout**: Vertical column, takes remaining space
- **Route Name**: 
  - Style: SemiBold typography
  - Color: Primary text color
  - Size: titleMedium
- **Local ID** (below name):
  - Shows `line_local_id` if sector has multiple lines (lineCount > 1)
  - Shows `sector_local_id` if sector has only one line (lineCount == 1)
  - Style: Small text (12sp)
  - Color: onSurfaceVariant (muted)

## Layout Specifications

```
Row Layout:
├─ AsyncImage (60dp circular)
├─ Spacer (12dp)
├─ Grade Badge (auto-width, colored background)
├─ Spacer (12dp)
└─ Column (weight = 1f, fills remaining space)
   ├─ Route Name (titleMedium, SemiBold)
   └─ Local ID (bodySmall, 12sp, muted color)

Card Properties:
- Elevation: 2dp
- Corner Radius: 12dp
- Padding: 12dp
- Full width
```

## Implementation Details

### Color Parsing
A helper function `parseColor()` safely parses hex color strings:
- Validates hex format (must start with "#")
- Handles parsing errors gracefully
- Falls back to Material Purple (#6200EE) on error

### Local ID Logic
```kotlin
val localId = if (route.lineCount == 1) {
    route.sectorLocalId
} else {
    route.lineLocalId
}
```

This ensures that when a sector has only one line, the sector local ID is shown instead of the line local ID, providing clearer context to users.

## API Requirements

For the route display to work correctly, the API should return routes with these fields:

```json
{
  "id": 1,
  "name": "La Marie-Rose",
  "grade": "7c",
  "thumbnail": "https://example.com/route-thumb.jpg",
  "color": "#FF5722",
  "line_local_id": "3B",
  "sector_local_id": "Sector A",
  "line_count": 5
}
```

## Benefits of the New Design

1. **Visual Hierarchy**: Thumbnail immediately identifies the route visually
2. **Color Coding**: Grade badge uses route color for quick difficulty recognition
3. **Space Efficiency**: Single-line layout shows more routes per screen
4. **Better Scanning**: Horizontal layout is easier to scan quickly
5. **Modern Aesthetic**: Rounded elements and proper spacing follow Material Design 3 principles
6. **Clear Context**: Local IDs help users navigate to specific routes on-site

## Dependencies

The updated implementation uses:
- **Coil**: For efficient image loading and caching
- **Material 3**: For modern UI components and theming
- **Jetpack Compose**: For declarative UI building

## Testing

The changes have been validated with:
- ✅ Gradle build successful
- ✅ Unit tests passing
- ✅ Null safety for all new optional fields
- ✅ Error handling for color parsing
- ✅ Fallback behavior for missing data
