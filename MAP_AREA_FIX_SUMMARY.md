# Map Area Bug Fixes - Tap Detection and Bottom-Right Alignment

## Issues Addressed

This update fixes two critical issues in the SVG map area:

1. **Tap detection failing when scrolling/zooming** - Users couldn't select sectors accurately after panning or zooming
2. **Initial alignment** - Changed from top-left to bottom-right alignment as requested

## Root Cause Analysis

### Issue 1: Tap Detection with Pan/Zoom

The tap detection coordinate transformation was using an incorrect `baseScale` calculation:

**Before (Incorrect):**
```kotlin
val baseScale = size.width / dims.viewBoxWidth
```

**Problem:** This calculation didn't account for the user's zoom level (`scale` variable), but the drawing code did:
```kotlin
val baseScale = size.width / scale / dims.viewBoxWidth
```

This mismatch caused tap coordinates to be transformed incorrectly when the user zoomed in or out, making sector selection fail or select random sectors.

### Issue 2: Initial Alignment

The previous implementation aligned the map to the top-left corner. The requirement was to align the bottom-right corner of the map to the bottom-right corner of the container.

## Solution Implemented

### 1. Fixed Tap Detection

Updated the tap detection to use the correct `baseScale` calculation that matches the drawing code:

```kotlin
// Calculate base scale factor to fit viewBox to canvas width
// Must match the calculation in the draw scope
val baseScale = size.width / scale / dims.viewBoxWidth
```

This ensures that the coordinate transformation for tap detection exactly reverses the transformation chain used for drawing:

**Drawing chain:**
```
Screen → User Pan (offsetX, offsetY) → User Zoom (scale) → Base Scale (baseScale) → SVG Space
```

**Tap detection chain (inverse):**
```
Tap Point → Undo Pan → Undo Zoom → Undo Base Scale → SVG Coordinates
```

### 2. Bottom-Right Alignment

Added logic to calculate the initial offset on the first draw:

```kotlin
// Set initial offset to align bottom-right on first draw
if (!initialOffsetSet && size.width > 0 && size.height > 0) {
    // Calculate the SVG content size in canvas space
    val svgCanvasWidth = dims.viewBoxWidth * baseScale
    val svgCanvasHeight = dims.viewBoxHeight * baseScale
    
    // Align bottom-right: offset so that bottom-right of SVG aligns with bottom-right of canvas
    offsetX = size.width - svgCanvasWidth * scale
    offsetY = size.height - svgCanvasHeight * scale
    initialOffsetSet = true
}
```

This calculation:
- Determines the SVG content size in canvas coordinates
- Sets the offset so that the bottom-right of the SVG content aligns with the bottom-right of the container
- Only runs once on the first draw when size is available

## Code Changes Summary

### File: `SvgMapView.kt`

**Key changes:**
1. Added `initialOffsetSet` state variable to track first draw
2. Fixed `baseScale` calculation in tap detection to include division by `scale`
3. Added initial offset calculation to align bottom-right
4. Updated component documentation to reflect bottom-right alignment

**Lines changed:** 19 insertions, 3 deletions

## Testing

✅ **Build:** Successful  
✅ **Lint:** Clean (0 errors)

### Expected Behavior

After these changes:
- **Tap detection:** Works correctly at any zoom level and pan position
- **Initial position:** Map's bottom-right corner aligns with container's bottom-right corner
- **Pan gestures:** Work smoothly in all directions
- **Zoom gestures:** Work correctly with accurate tap detection maintained
- **Sector selection:** Accurately selects the tapped sector regardless of zoom/pan state

## Technical Details

### Coordinate Transformation

The transformation chain is now consistent between drawing and tap detection:

**Drawing:**
```kotlin
translate(offsetX, offsetY) {           // User pan
    scale(scale, scale) {                // User zoom
        translate(-viewBoxX * baseScale, -viewBoxY * baseScale) {  // ViewBox origin
            scale(baseScale) {           // Base scaling to fit canvas
                drawPath(...)            // SVG coordinates
            }
        }
    }
}
```

**Tap Detection (inverse):**
```kotlin
// Undo pan
val canvasX = (tapOffset.x - offsetX) / scale
val canvasY = (tapOffset.y - offsetY) / scale

// Undo base scale and add viewBox origin
val svgX = (canvasX / baseScale) + dims.viewBoxX
val svgY = (canvasY / baseScale) + dims.viewBoxY
```

The key fix was ensuring `baseScale` is calculated identically in both places:
```kotlin
val baseScale = size.width / scale / dims.viewBoxWidth
```

## Benefits

1. **Accurate tap detection** - Users can reliably select sectors at any zoom level
2. **Better initial view** - Bottom-right alignment as requested
3. **Consistent behavior** - Zoom and pan work smoothly with proper tap detection
4. **Minimal changes** - Surgical fix addressing only the core issues

## Migration Notes

No breaking changes - this is a drop-in fix. Existing code using `SvgMapView` will automatically benefit from these improvements.
