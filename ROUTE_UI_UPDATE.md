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
)
```

The `Line` and `Sector` data classes now include `localId`:

```kotlin
data class Line(
    // ... existing fields
    val localId: String?           // Local ID of the line (from API)
)

data class Sector(
    // ... existing fields
    val localId: String?           // Local ID of the sector (from API)
)
```

A new `RouteWithMetadata` class enriches routes with line/sector information:

```kotlin
data class RouteWithMetadata(
    val route: Route,
    val lineLocalId: String? = null,      // Populated from Line.localId
    val sectorLocalId: String? = null,    // Populated from Sector.localId
    val lineCount: Int? = null            // Number of lines in the sector
)
```

This approach allows the app to combine data from multiple API calls (sectors → lines → routes) to provide enriched display information.

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

In `AreaDetailScreen`, when a sector is selected, the app fetches lines for that sector and then routes for each line. The `AreaDetailViewModel` enriches each route with metadata:

```kotlin
val localId = if (routeWithMetadata.lineCount == 1) {
    routeWithMetadata.sectorLocalId
} else {
    routeWithMetadata.lineLocalId
}
```

**Data Flow:**
1. User selects a sector → `getLinesBySector(sectorId)` is called
2. For each line → `getRoutesByLine(lineId)` is called
3. Each route is enriched with:
   - `lineLocalId` from the Line object's `localId` field
   - `sectorLocalId` from the Sector object's `localId` field
   - `lineCount` = total number of lines in the sector

**Rationale:** When a sector contains only one line, displaying the sector local ID provides sufficient context since there's no ambiguity about which line is being referenced. When multiple lines exist in a sector, the line local ID is displayed to specifically identify which line the route is on. This approach reduces redundancy while maintaining clarity.

## API Requirements

For the route display to work correctly, the API should return:

**Routes** with these fields:
```json
{
  "id": 1,
  "name": "La Marie-Rose",
  "grade": "7c",
  "thumbnail": "https://example.com/route-thumb.jpg",
  "color": "#FF5722"
}
```

**Lines** (from `/sectors/{id}/lines`) with `local_id`:
```json
{
  "id": 1,
  "name": "Line 3B",
  "sectorId": 1,
  "local_id": "3B"
}
```

**Sectors** (from `/areas/{id}/sectors`) with `local_id`:
```json
{
  "id": 1,
  "name": "Sector Alpha",
  "areaId": 1,
  "local_id": "A"
}
```

The app combines data from these endpoints to enrich routes with line/sector information when displaying filtered routes.

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
