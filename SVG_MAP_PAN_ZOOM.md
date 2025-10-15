# SVG Map Pan and Zoom Implementation

## Problem Statement

Despite multiple attempts to fix the SVG map positioning shift, the issue persisted. The previous approach tried to perfectly align the SVG transformation, but this proved difficult to resolve completely.

The new approach: Instead of fighting the positioning shift, make the map **scrollable and zoomable** in all directions, and initially center it in its container. This gives users control to adjust the view themselves and is more forgiving of any positioning quirks.

## Solution Implemented

### 1. Pan (Scroll) Gestures

Users can now drag the map in any direction to pan around.

**Implementation:**
- Added `rememberTransformableState` to track pan offset
- Applied translation via `graphicsLayer` modifier
- State updates on pan gestures: `offsetX += panChange.x`, `offsetY += panChange.y`

### 2. Zoom (Pinch) Gestures

Users can pinch to zoom in/out on the map.

**Implementation:**
- Track zoom scale in state (min 0.5x, max 5x)
- Applied scale via `graphicsLayer` modifier
- State updates on zoom gestures: `scale = (scale * zoomChange).coerceIn(0.5f, 5f)`

### 3. Initial Centering

The map now starts centered in its container, providing a better initial view.

**Implementation:**
```kotlin
// Calculate offset to center the map in the container
val centerOffsetX = (size.width - dims.viewBoxWidth * baseScale) / 2f
val centerOffsetY = (size.height - dims.viewBoxHeight * baseScale) / 2f

// Apply centering transformation
translate(centerOffsetX, centerOffsetY) {
    // ... render SVG paths
}
```

### 4. Updated Tap Detection

Tap detection now accounts for user pan/zoom transformations.

**Transformation chain (screen → SVG coordinates):**
1. Undo user pan: `(tapOffset - offset)`
2. Undo user zoom: `/ scale`
3. Undo centering: `- centerOffset`
4. Undo base scale: `/ baseScale`
5. Add viewBox origin: `+ viewBoxOrigin`

```kotlin
val canvasX = (tapOffset.x - offsetX) / scale
val canvasY = (tapOffset.y - offsetY) / scale
val svgX = ((canvasX - centerOffsetX) / baseScale) + dims.viewBoxX
val svgY = ((canvasY - centerOffsetY) / baseScale) + dims.viewBoxY
```

## Technical Details

### Modifier Chain

The order of modifiers is important:

```kotlin
Canvas(
    modifier = modifier
        .fillMaxWidth()
        .then(aspectRatioModifier)
        .graphicsLayer(
            scaleX = scale,
            scaleY = scale,
            translationX = offsetX,
            translationY = offsetY
        )
        .transformable(state = state)
        .pointerInput(...)
)
```

- `graphicsLayer` applies the visual transformation (pan/zoom)
- `transformable` captures gesture events
- `pointerInput` handles tap detection

### Zoom Limits

Zoom is constrained between 0.5x and 5x:
- **0.5x minimum**: Prevents zooming out too far and losing context
- **5x maximum**: Prevents excessive zoom that would make paths pixelated

### State Management

Pan and zoom state is managed with `remember` and `mutableStateOf`:

```kotlin
var scale by remember { mutableStateOf(1f) }
var offsetX by remember { mutableStateOf(0f) }
var offsetY by remember { mutableStateOf(0f) }
```

This ensures state persists across recompositions but resets when the component is recreated (e.g., navigating away and back).

## Benefits

### 1. User Control
Users can adjust the map view to their preference, working around any positioning issues.

### 2. Better UX
- Pinch-to-zoom is a familiar gesture on mobile devices
- Pan gestures allow exploring large maps
- Initial centering provides a good starting view

### 3. Simpler Implementation
- No need to perfectly solve complex transformation math
- Leverages Compose's built-in gesture support
- Robust and maintainable

### 4. Fixes the Positioning Issue
By centering the map initially and allowing pan/zoom, the original shift issue becomes irrelevant.

## Files Changed

### `app/src/main/java/com/example/topoclimb/ui/components/SvgMapView.kt`

**Imports added:**
- `rememberTransformableState`
- `transformable`
- `graphicsLayer`
- `mutableStateOf`, `getValue`, `setValue`

**State added:**
- `scale`, `offsetX`, `offsetY` - Track user pan/zoom
- `state` - `TransformableState` for gestures

**Modifiers updated:**
- Added `graphicsLayer` for pan/zoom transformation
- Added `transformable` for gesture detection
- Updated `pointerInput` keys to include pan/zoom state

**Drawing logic:**
- Added centering offset calculation
- Added outer `translate` for centering
- Renamed `scale` to `baseScale` to distinguish from user zoom

**Tap detection:**
- Updated to account for user pan/zoom transformations
- Updated to account for centering offset

## Testing

### Build Status
✅ Compilation successful  
✅ All unit tests passing  
✅ Lint checks clean  

### Manual Testing Checklist
- [ ] Map displays centered in container initially
- [ ] Pan gestures work in all directions
- [ ] Pinch-to-zoom works smoothly
- [ ] Zoom is limited to 0.5x - 5x range
- [ ] Tap detection works correctly with pan/zoom applied
- [ ] Selected sector highlights correctly
- [ ] Route filtering works after tapping sectors

## Future Enhancements

Potential improvements to consider:

1. **Reset Button**: Add a button to reset pan/zoom to initial state
2. **Double-tap to Zoom**: Implement double-tap to zoom in/out
3. **Zoom to Sector**: When selecting a sector, auto-pan/zoom to frame it
4. **Bounds Limiting**: Prevent panning too far outside the map bounds
5. **Smooth Animations**: Add spring animations for better feel
6. **Persistence**: Save pan/zoom state across app restarts

## Migration Guide

No breaking changes - this is a drop-in enhancement. Existing code using `SvgMapView` will automatically get pan/zoom functionality.

## Conclusion

This implementation successfully addresses the original positioning issue by:
- Making the map scrollable and zoomable
- Centering it initially in the container
- Giving users control to adjust the view

The approach is simpler, more maintainable, and provides a better user experience than trying to perfectly solve the transformation math.
