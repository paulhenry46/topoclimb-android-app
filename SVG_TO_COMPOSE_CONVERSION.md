# SVG Map to Compose Component Conversion

## Overview
This document describes the conversion of the area map from WebView-based SVG rendering to native Compose components with interactive sector selection.

## Problem Statement
The original implementation used a WebView to display SVG maps, which had the following limitations:
- No direct interaction with map elements
- No ability to highlight specific sectors
- Couldn't filter routes based on selected map areas

## Solution Implemented

### 1. Data Models
Created new data models to support the hierarchical structure of climbing areas:

**Sector** (`app/src/main/java/com/example/topoclimb/data/Sector.kt`)
```kotlin
data class Sector(
    val id: Int,
    val name: String,
    val description: String?,
    val areaId: Int
)
```

**Line** (`app/src/main/java/com/example/topoclimb/data/Line.kt`)
```kotlin
data class Line(
    val id: Int,
    val name: String,
    val description: String?,
    val sectorId: Int
)
```

### 2. API Integration
Added new API endpoints to support the hierarchical data structure:

- `GET /areas/{areaId}/sectors` - Get sectors for an area
- `GET /sectors/{sectorId}/lines` - Get lines for a sector
- `GET /lines/{lineId}/routes` - Get routes for a line

### 3. SVG Parser
Created a utility to parse SVG content and extract interactive paths:

**SvgParser** (`app/src/main/java/com/example/topoclimb/utils/SvgParser.kt`)
- Parses SVG XML content
- Extracts `<path>` elements with their `d` attributes
- Identifies sector IDs from path element IDs (e.g., `id="sector_123"`)
- Converts SVG path data to Compose Path objects
- Extracts viewBox dimensions for proper scaling

### 4. Compose Map Component
Created a native Compose component to render the SVG map:

**SvgMapView** (`app/src/main/java/com/example/topoclimb/ui/components/SvgMapView.kt`)

Features:
- Renders SVG paths using Compose Canvas
- Handles tap gestures with accurate hit detection
- Highlights selected sectors in red with thicker strokes
- Scales and centers the SVG to fit the available space
- Supports toggling sector selection

### 5. ViewModel Updates
Enhanced the AreaDetailViewModel to manage sector selection and route filtering:

**New State Fields:**
- `svgPaths: List<SvgPathData>` - Parsed SVG paths with sector IDs
- `svgDimensions: SvgDimensions?` - SVG viewBox dimensions
- `selectedSectorId: Int?` - Currently selected sector
- `sectors: List<Sector>` - Available sectors for the area

**New Methods:**
- `onSectorTapped(sectorId: Int)` - Handles sector selection/deselection
  - When a sector is tapped, it fetches the lines for that sector
  - Then fetches routes for each line
  - Updates the route list to show only routes in the selected sector
  - If the same sector is tapped again, it deselects and shows all routes

### 6. UI Updates
Updated the AreaDetailScreen to use the new Compose component:

**Changes:**
- Removed WebView and AndroidView usage
- Added SvgMapView component with interactive capabilities
- Added visual feedback showing which sector is selected
- Updated route list title to indicate if filtered by sector

## User Flow

1. User navigates to an area detail screen
2. The SVG map is fetched and parsed automatically
3. Paths with `sector_id` attributes become interactive
4. User taps on a sector path:
   - The path turns red and becomes thicker
   - A message appears: "Sector X selected - Tap to deselect"
   - The route list updates to show only routes in that sector
   - Route list title shows: "Routes in Sector X (count)"
5. User taps the same sector again:
   - The selection is cleared
   - All routes are shown again

## Technical Details

### Path Hit Detection
The implementation uses bounding box hit detection:
1. Pre-compute bounds for each path with a sector ID
2. When user taps, transform the tap coordinates to SVG space
3. Check if the tap falls within any path's expanded bounds (10 unit tolerance)
4. Trigger the callback for the first matching path

### Coordinate Transformation
The component handles coordinate systems properly:
- Canvas coordinates → SVG viewBox coordinates
- Applies the same scale and translation used for rendering
- Ensures tap detection matches visual rendering

### Visual Feedback
- Selected paths: Red color, 3px stroke width
- Unselected paths: Black color, 2px stroke width
- Stroke widths scale inversely with zoom to maintain consistent appearance

## Files Changed

### New Files
1. `app/src/main/java/com/example/topoclimb/data/Sector.kt`
2. `app/src/main/java/com/example/topoclimb/data/Line.kt`
3. `app/src/main/java/com/example/topoclimb/data/SectorsResponse.kt`
4. `app/src/main/java/com/example/topoclimb/data/LinesResponse.kt`
5. `app/src/main/java/com/example/topoclimb/utils/SvgParser.kt`
6. `app/src/main/java/com/example/topoclimb/ui/components/SvgMapView.kt`

### Modified Files
1. `app/src/main/java/com/example/topoclimb/data/Route.kt` - Added optional `lineId` field
2. `app/src/main/java/com/example/topoclimb/network/TopoClimbApiService.kt` - Added new endpoints
3. `app/src/main/java/com/example/topoclimb/repository/TopoClimbRepository.kt` - Added repository methods
4. `app/src/main/java/com/example/topoclimb/viewmodel/AreaDetailViewModel.kt` - Added sector selection logic
5. `app/src/main/java/com/example/topoclimb/ui/screens/AreaDetailScreen.kt` - Replaced WebView with SvgMapView

## Benefits

1. **Native Performance**: No WebView overhead
2. **Better UX**: Direct interaction with map elements
3. **Context-Aware Filtering**: Routes filtered by selected sector
4. **Clearer Visual Feedback**: Selected sectors highlighted
5. **Maintainable Code**: Pure Kotlin/Compose implementation

## Requirements for SVG Files

For the interactive features to work, SVG files must:
- Include a `viewBox` attribute on the root `<svg>` element
- Have `<path>` elements with `id` attributes in the format `sector_123` or `sector-123`
- Use valid SVG path data in the `d` attribute

## Future Enhancements

Potential improvements:
1. Add sector name labels on the map
2. Support pinch-to-zoom and pan gestures
3. Cache parsed SVG data to improve performance
4. Add smooth animations when selecting/deselecting sectors
5. Show sector boundaries more clearly
6. Support multi-sector selection
