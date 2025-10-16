# Area Map Improvements

## Summary

Updated the Area map SVG rendering to improve visibility and interaction according to the following requirements:

1. **Transparent Background**: The SVG map now has no background and uses the parent container's background
2. **Black Paths**: All SVG paths are displayed in black for better visibility
3. **Interactive Sectors**: Users can tap on any path (sector) to select it
4. **Visual Feedback**: Selected paths turn red to indicate selection

## Changes Made

### File Modified: `app/src/main/java/com/example/topoclimb/ui/screens/AreaDetailScreen.kt`

#### 1. WebView Background Transparency
Added `setBackgroundColor(android.graphics.Color.TRANSPARENT)` to make the WebView itself transparent.

#### 2. CSS Updates
- Set `background: transparent` for both body and SVG elements
- Styled all `svg path` elements with:
  - `stroke: black` - black outline for paths
  - `fill: none` - no fill to maintain transparency
  - `cursor: pointer` - visual indicator that paths are clickable

#### 3. Interactive Selection
- Added CSS class `svg path.selected` with `stroke: red` to highlight selected paths
- Implemented JavaScript event handler that:
  - Listens for clicks on all SVG paths
  - Removes the `selected` class from all paths
  - Adds the `selected` class to the clicked path
  - Ensures only one path can be selected at a time

## Technical Implementation

The SVG is rendered in a WebView with JavaScript enabled. The HTML structure includes:

```html
<style>
    body { background: transparent; }
    svg { background: transparent; }
    svg path { stroke: black; fill: none; cursor: pointer; }
    svg path.selected { stroke: red; }
</style>
<script>
    // Click handler for path selection
    document.addEventListener('DOMContentLoaded', function() {
        const paths = document.querySelectorAll('svg path');
        paths.forEach(function(path) {
            path.addEventListener('click', function() {
                paths.forEach(p => p.classList.remove('selected'));
                this.classList.add('selected');
            });
        });
    });
</script>
```

## Testing

- ✅ Build successful (`./gradlew assembleDebug`)
- ✅ All existing tests pass (`./gradlew test`)
- ✅ No new dependencies added
- ✅ Minimal code changes (single file modified)

## User Experience

Users can now:
1. See the SVG map with clear black paths against the container background
2. Tap any path/sector to select it
3. See visual feedback as the selected path turns red
4. Tap another path to deselect the previous and select the new one

## Compatibility

These changes are backward compatible and do not affect existing functionality. The changes only enhance the visual appearance and interactivity of the SVG map display.
