# Sector Selection and Route Filtering Improvements

## Summary

This document describes the improvements made to the Area Detail screen to enhance sector selection on the SVG map and implement automatic route filtering based on the selected sector.

## Problem Statement

The following issues were addressed:

1. **No route filtering on sector selection**: When selecting a sector on the map, routes were not automatically filtered
2. **Difficult sector selection**: SVG paths were close to the border of the container, making them hard to select
3. **Need for better hit tolerance**: Sectors needed better tolerance around them to make selection easier
4. **Blue rectangle on tap**: A blue rectangle appeared when tapping sectors (default WebView tap highlight)
5. **Blank space at bottom**: Fixed height of 400dp caused blank space at the bottom of the map

## Solution Overview

The solution involves:

1. **Data Model Enhancement**: Added `pathId` field to the Route model to link routes to specific sectors
2. **ViewModel Updates**: Added filtering logic to filter routes based on selected sector
3. **JavaScript Bridge**: Implemented bi-directional communication between WebView JavaScript and Kotlin code
4. **CSS Improvements**: Enhanced hit tolerance and removed tap highlights
5. **Adaptive Height**: Changed from fixed height to adaptive height for the map container

## Changes Made

### 1. Route Data Model (`Route.kt`)

Added a new field to link routes to sectors/paths:

```kotlin
@SerializedName("path_id") val pathId: String? = null
```

This field maps to the `path_id` field in the API response and contains the ID of the SVG path (sector) that the route belongs to.

### 2. AreaDetailViewModel

#### Updated UI State

Added two new fields to `AreaDetailUiState`:

```kotlin
val allRoutes: List<Route> = emptyList(),  // Stores all routes
val selectedSectorId: String? = null,       // Tracks selected sector
```

#### New Filtering Function

Added `filterRoutesBySector()` method:

```kotlin
fun filterRoutesBySector(sectorId: String?) {
    val currentState = _uiState.value
    _uiState.value = currentState.copy(
        selectedSectorId = sectorId,
        routes = if (sectorId.isNullOrEmpty()) {
            currentState.allRoutes  // Show all routes when no sector selected
        } else {
            currentState.allRoutes.filter { route ->
                route.pathId == sectorId  // Filter by sector ID
            }
        }
    )
}
```

### 3. AreaDetailScreen Updates

#### JavaScript Interface

Added a JavaScript interface to enable communication from WebView to Kotlin:

```kotlin
addJavascriptInterface(object {
    @JavascriptInterface
    fun onSectorSelected(sectorId: String) {
        viewModel.filterRoutesBySector(sectorId)
    }
}, "Android")
```

#### CSS Improvements

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

#### JavaScript Click Handling

Enhanced the click handler to:
- Auto-assign IDs to paths that don't have them
- Support toggle behavior (click to select, click again to deselect)
- Notify the Android app via the JavaScript interface

```javascript
path.addEventListener('click', function(e) {
    e.preventDefault();
    
    const wasSelected = this.classList.contains('selected');
    
    // Remove selected class from all paths
    paths.forEach(function(p) {
        p.classList.remove('selected');
    });
    
    if (!wasSelected) {
        // Select this path
        this.classList.add('selected');
        window.Android.onSectorSelected(this.id);
    } else {
        // Deselect - show all routes
        window.Android.onSectorSelected('');
    }
});
```

#### Adaptive Height

Changed the map container from fixed height to adaptive:

```kotlin
// Before:
.height(400.dp)

// After:
.wrapContentHeight()
```

Also updated the body CSS:

```css
/* Before: */
align-items: center;
min-height: 100vh;

/* After: */
align-items: flex-start;
/* removed min-height */
```

#### UI Feedback

Updated the routes section header to show filtering status:

```kotlin
text = if (uiState.selectedSectorId.isNullOrEmpty()) {
    "Routes (${uiState.routes.size})"
} else {
    "Routes for selected sector (${uiState.routes.size})"
}
```

Added separate empty states for filtered vs. unfiltered views:

- "No routes available for the selected sector." (when sector selected but no routes match)
- "No routes available for this area." (when area has no routes at all)

## User Experience Flow

1. **User opens Area Detail screen**: All routes are displayed
2. **User taps a sector on the map**: 
   - The sector is highlighted in red with thicker stroke
   - Routes are filtered to show only those belonging to that sector
   - Routes header updates to indicate filtering is active
3. **User taps the same sector again**: 
   - The sector is deselected (returns to black)
   - All routes are shown again
   - Routes header returns to normal
4. **User taps a different sector**: 
   - Previous sector is deselected
   - New sector is highlighted
   - Routes are filtered for the new sector

## API Requirements

For the route filtering to work, the backend API must include the `path_id` field in route responses:

```json
{
  "id": 1,
  "name": "La Marie-Rose",
  "grade": "7c",
  "type": "boulder",
  "height": 5,
  "siteId": 1,
  "siteName": "Fontainebleau",
  "path_id": "sector-3"
}
```

The `path_id` should match the `id` attribute of the corresponding `<path>` element in the SVG map.

## SVG Map Requirements

For optimal compatibility, SVG paths should have `id` attributes that match the `path_id` values in route data:

```svg
<svg>
  <path id="sector-1" d="M 10 10 L 100 100 ..." />
  <path id="sector-2" d="M 20 20 L 120 120 ..." />
  <path id="sector-3" d="M 30 30 L 130 130 ..." />
</svg>
```

If paths don't have IDs, the JavaScript will auto-assign them as `sector-0`, `sector-1`, etc.

## Technical Details

### Thread Safety

The JavaScript interface calls `filterRoutesBySector()` from the WebView thread. The ViewModel uses `MutableStateFlow`, which is thread-safe for concurrent updates.

### Performance

- Route filtering is done in-memory on the already-loaded route list
- No additional API calls are made when selecting sectors
- The filter operation is O(n) where n is the number of routes in the area

### Compatibility

- Minimum Android SDK: 24 (Android 7.0)
- Requires JavaScript enabled in WebView
- Uses standard CSS and JavaScript (no external dependencies)

## Testing

All changes have been tested:

- ✅ Unit tests pass (`./gradlew test`)
- ✅ Debug build successful (`./gradlew assembleDebug`)
- ✅ No new dependencies added
- ✅ Minimal code changes (3 files modified)

## Files Modified

1. `app/src/main/java/com/example/topoclimb/data/Route.kt` - Added pathId field
2. `app/src/main/java/com/example/topoclimb/viewmodel/AreaDetailViewModel.kt` - Added filtering logic
3. `app/src/main/java/com/example/topoclimb/ui/screens/AreaDetailScreen.kt` - Enhanced WebView with JavaScript bridge and improved CSS

## Backward Compatibility

These changes are backward compatible:

- `pathId` field is optional (nullable with default null value)
- If routes don't have `path_id`, the filtering won't break
- If SVG paths don't have IDs, auto-generated IDs are used
- Existing functionality is preserved

## Future Enhancements

Potential improvements for future versions:

1. **Multi-select**: Allow selecting multiple sectors at once
2. **Sector Info**: Show sector name/info when hovering or selecting
3. **Deep Linking**: Support URL parameters to pre-select a sector
4. **Accessibility**: Add ARIA labels for better screen reader support
5. **Analytics**: Track which sectors are most frequently selected
6. **Performance**: Virtualize route list for areas with hundreds of routes

## Conclusion

These improvements significantly enhance the user experience by:

- Making sectors much easier to select (3x thicker stroke)
- Providing immediate visual feedback (no blue rectangle, red highlight on selection)
- Automatically filtering routes to relevant ones for the selected sector
- Eliminating blank space below the map
- Supporting toggle behavior for better control

The implementation is clean, performant, and maintains backward compatibility with existing data.
