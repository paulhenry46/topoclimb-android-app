# Sector Selection and Route Filtering Improvements

## Summary

This document describes the improvements made to the Area Detail screen to enhance sector selection on the SVG map and implement automatic route filtering based on the selected sector using the correct API structure.

## Problem Statement

The following issues were addressed:

1. **No route filtering on sector selection**: When selecting a sector on the map, routes were not automatically filtered
2. **Difficult sector selection**: SVG paths were close to the border of the container, making them hard to select
3. **Need for better hit tolerance**: Sectors needed better tolerance around them to make selection easier
4. **Blue rectangle on tap**: A blue rectangle appeared when tapping sectors (default WebView tap highlight)
5. **Blank space at bottom**: Fixed height of 400dp caused blank space at the bottom of the map

## Solution Overview

The solution involves:

1. **Data Model Enhancement**: Added Sector and Line data models to support the API structure: Area → Sectors → Lines → Routes
2. **API Integration**: Added new endpoints for fetching sectors, lines, and routes following the hierarchical structure
3. **ViewModel Updates**: Added logic to fetch lines and routes when a sector is selected
4. **JavaScript Bridge**: Implemented bi-directional communication between WebView JavaScript and Kotlin code
5. **CSS Improvements**: Enhanced hit tolerance and removed tap highlights
6. **Adaptive Height**: Changed from fixed height to adaptive height for the map container

## API Structure

The app follows the hierarchical structure:
```
Area → Sectors → Lines → Routes
```

When a sector is selected:
1. Request to `/sectors/{id}/lines` to get lines of the sector
2. For each line, request to `/lines/{id}/routes` to get routes
3. All responses are wrapped with `data:{}` prefix

## Changes Made

### 1. New Data Models

#### Sector Model (`Sector.kt`)

```kotlin
data class Sector(
    val id: Int,
    val name: String,
    val description: String?,
    val areaId: Int
)
```

The sector's `id` field directly corresponds to the SVG path element's `id` attribute.

#### Line Model (`Line.kt`)

```kotlin
data class Line(
    val id: Int,
    val name: String,
    val description: String?,
    val sectorId: Int
)
```

#### Response Wrappers

All API responses are wrapped with `data:{}`:
- `SectorsResponse` - wraps list of sectors
- `LinesResponse` - wraps list of lines
- `RoutesResponse` - already exists, wraps list of routes

### 2. API Service Updates

Added new endpoints in `TopoClimbApiService.kt`:

```kotlin
@GET("sectors/{id}/lines")
suspend fun getLinesBySector(@Path("id") sectorId: Int): LinesResponse

@GET("lines/{id}/routes")
suspend fun getRoutesByLine(@Path("id") lineId: Int): RoutesResponse

@GET("areas/{areaId}/sectors")
suspend fun getSectorsByArea(@Path("areaId") areaId: Int): SectorsResponse
```

### 3. Repository Updates

Added corresponding repository methods:

```kotlin
suspend fun getSectorsByArea(areaId: Int): Result<List<Sector>>
suspend fun getLinesBySector(sectorId: Int): Result<List<Line>>
suspend fun getRoutesByLine(lineId: Int): Result<List<Route>>
```

### 4. AreaDetailViewModel

#### Updated UI State

Modified `AreaDetailUiState`:

```kotlin
val routes: List<Route> = emptyList(),       // Currently displayed routes
val sectors: List<Sector> = emptyList(),     // Sectors for the area
val selectedSectorId: Int? = null,           // Selected sector ID (Int, not String)
```

#### New Filtering Function

Updated `filterRoutesBySector()` to use the API:

```kotlin
fun filterRoutesBySector(sectorId: Int?) {
    viewModelScope.launch {
        if (sectorId == null) {
            // Reload all routes for the area
            val routesResult = repository.getRoutesByArea(area.id)
            // Update state with all routes
        } else {
            // Fetch lines for this sector
            val linesResult = repository.getLinesBySector(sectorId)
            val allRoutes = mutableListOf<Route>()
            
            // Fetch routes for each line
            for (line in lines) {
                val routesResult = repository.getRoutesByLine(line.id)
                allRoutes.addAll(routes)
            }
            
            // Update state with filtered routes
        }
    }
}
```

### 5. AreaDetailScreen Updates

#### JavaScript Interface

Updated to directly use the SVG path's `id` attribute as the sector ID:

```kotlin
addJavascriptInterface(object {
    @JavascriptInterface
    fun onSectorSelected(sectorIdStr: String) {
        // The SVG path's id is the sector ID
        if (sectorIdStr.isEmpty()) {
            viewModel.filterRoutesBySector(null)
        } else {
            // Parse the sector ID from the path's id attribute
            val sectorId = sectorIdStr.toIntOrNull()
            viewModel.filterRoutesBySector(sectorId)
        }
    }
}, "Android")
```

#### CSS Improvements (Unchanged)

**Removed tap highlight:**
```css
body {
    -webkit-tap-highlight-color: transparent;
    -webkit-touch-callout: none;
    -webkit-user-select: none;
    user-select: none;
}
```

**Increased hit tolerance:**
```css
svg path {
    stroke: black;
    fill: none;
    cursor: pointer;
    stroke-width: 3;  /* Increased from default 1 to 3 */
    paint-order: stroke;
}

svg path.selected {
    stroke: red;
    stroke-width: 4;  /* Even thicker when selected */
}
```

**Added hover feedback:**
```css
svg path:hover {
    stroke: #666;
}
```

#### Adaptive Height (Unchanged)

```kotlin
// Changed from:
.height(400.dp)

// To:
.wrapContentHeight()
```

### 6. Route Model

**No changes to Route model** - the `pathId` field was removed as it's not part of the actual API structure.

## User Experience Flow

1. **User opens Area Detail screen**: 
   - All routes for the area are displayed
   - Sectors are loaded from the API
2. **User taps a sector on the map**: 
   - JavaScript identifies the clicked path's ID (which is the sector ID)
   - The sector ID is parsed and passed to the ViewModel
   - App fetches lines for that sector
   - For each line, app fetches routes
   - All routes are combined and displayed
   - Sector is highlighted in red with thicker stroke
   - Routes header updates to indicate filtering is active
3. **User taps the same sector again**: 
   - The sector is deselected (returns to black)
   - App reloads all routes for the area
   - Routes header returns to normal
4. **User taps a different sector**: 
   - Previous sector is deselected
   - New sector is highlighted
   - Routes are fetched and filtered for the new sector

## API Requirements

### Expected Endpoints

1. **Get sectors for an area**:
   ```
   GET /areas/{areaId}/sectors
   Response: { "data": [{ "id": 1, "name": "Sector A", "areaId": 1 }] }
   ```

2. **Get lines for a sector**:
   ```
   GET /sectors/{sectorId}/lines
   Response: { "data": [{ "id": 1, "name": "Line 1", "sectorId": 1 }] }
   ```

3. **Get routes for a line**:
   ```
   GET /lines/{lineId}/routes
   Response: { "data": [{ "id": 1, "name": "Route Name", "grade": "7c", ... }] }
   ```

### SVG Map Requirements

SVG paths should have `id` attributes that are the sector IDs (as integers):

```svg
<svg>
  <path id="1" d="M 10 10 L 100 100 ..." />
  <path id="2" d="M 20 20 L 120 120 ..." />
</svg>
```

The `id` attribute of each SVG path should match the `id` field of the corresponding sector in the API response.

## Technical Details

### Data Flow

```
User taps SVG path
  ↓
JavaScript captures click, gets path.id (sector ID)
  ↓
window.Android.onSectorSelected(sectorId)
  ↓
Parse sector ID as integer
  ↓
ViewModel.filterRoutesBySector(sectorId)
  ↓
API: GET /sectors/{id}/lines → List<Line>
  ↓
For each line: GET /lines/{id}/routes → List<Route>
  ↓
Combine all routes
  ↓
State update with filtered routes
  ↓
UI recomposes showing filtered list
```

### Thread Safety

All API calls are made within `viewModelScope.launch`, ensuring proper coroutine handling and lifecycle awareness.

### Performance

- Route filtering requires multiple API calls (1 for lines + N for routes where N = number of lines)
- Results are cached in the UI state until sector selection changes
- Initial load fetches all routes for the area for quick display

### Compatibility

- Minimum Android SDK: 24 (Android 7.0)
- Requires JavaScript enabled in WebView
- Uses standard CSS and JavaScript (no external dependencies)

## Testing

All changes have been tested:

- ✅ Unit tests pass (`./gradlew test`)
- ✅ Debug build successful (`./gradlew assembleDebug`)
- ✅ No new dependencies added
- ✅ Minimal code changes (7 files: 2 new models, 3 updated, 1 reverted, 1 documentation)

## Files Modified/Created

1. `app/src/main/java/com/example/topoclimb/data/Sector.kt` - **NEW** Sector data model
2. `app/src/main/java/com/example/topoclimb/data/Line.kt` - **NEW** Line data model
3. `app/src/main/java/com/example/topoclimb/data/Route.kt` - **REVERTED** Removed pathId field
4. `app/src/main/java/com/example/topoclimb/network/TopoClimbApiService.kt` - Added new endpoints
5. `app/src/main/java/com/example/topoclimb/repository/TopoClimbRepository.kt` - Added repository methods
6. `app/src/main/java/com/example/topoclimb/viewmodel/AreaDetailViewModel.kt` - Updated filtering logic
7. `app/src/main/java/com/example/topoclimb/ui/screens/AreaDetailScreen.kt` - Updated JavaScript interface

## Backward Compatibility

These changes maintain backward compatibility:

- Existing routes endpoint still works
- New endpoints are additive
- SVG paths without IDs still work (auto-generated)
- Areas without sectors can still display routes

## Future Enhancements

Potential improvements for future versions:

1. **Caching**: Cache sectors and lines to reduce API calls
2. **Parallel Loading**: Fetch routes for multiple lines in parallel
3. **Loading Indicators**: Show loading state while fetching sector routes
4. **Error Handling**: Better error messages for failed sector/line fetches
5. **Offline Support**: Cache sector structure for offline use

## Conclusion

These improvements implement the correct API structure (Area → Sectors → Lines → Routes) while maintaining all the UX improvements:

- Making sectors much easier to select (3x thicker stroke)
- Providing immediate visual feedback (no blue rectangle, red highlight on selection)
- Automatically filtering routes to relevant ones for the selected sector
- Eliminating blank space below the map
- Supporting toggle behavior for better control

The implementation follows the actual API architecture and provides a clean, performant solution.
