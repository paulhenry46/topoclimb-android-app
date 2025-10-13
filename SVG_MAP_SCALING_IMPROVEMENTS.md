# SVG Map Scaling and Tap Improvements

## Overview
This update improves the SVG map display by making it scale to the full width of its parent container and automatically adapting the height based on the SVG's aspect ratio. It also makes the map paths thicker and easier to tap on mobile devices.

## Problem Statement
1. The map was using a fixed height (400.dp) and not utilizing the full width of the parent container
2. The stroke width was too thin (2f-3f), making it difficult to tap on paths, especially on mobile devices
3. The tap tolerance was too small (10f), requiring precise taps

## Solution Implemented

### 1. Full Width Scaling with Adaptive Height
**Before:**
- Map used `fillMaxSize()` with a fixed 400.dp height in the parent Card
- Scaling used `min(scaleX, scaleY)` which often left unused space
- Map was centered both horizontally and vertically

**After:**
- Map uses `fillMaxWidth()` + `aspectRatio()` modifier
- Scaling uses `scaleX` to fill the full width
- Height automatically adapts based on SVG's viewBox aspect ratio
- Map aligns to top-left (no vertical centering)

### 2. Increased Stroke Width
**Before:**
- Unselected paths: 2f stroke width
- Selected paths: 3f stroke width

**After:**
- Unselected paths: 4f stroke width (doubled)
- Selected paths: 6f stroke width (doubled)

This makes paths much more visible and easier to interact with on mobile devices.

### 3. Increased Tap Tolerance
**Before:**
- Tap detection used 10f tolerance around path bounds

**After:**
- Tap detection uses 20f tolerance around path bounds (doubled)

This makes it easier to tap on paths without requiring pixel-perfect precision.

## Technical Details

### Aspect Ratio Calculation
```kotlin
val aspectRatioModifier = svgDimensions?.let { dims ->
    Modifier.aspectRatio(dims.viewBoxWidth / dims.viewBoxHeight)
} ?: Modifier
```

The aspect ratio is calculated from the SVG's viewBox dimensions and applied to the Canvas composable.

### Scaling Algorithm
```kotlin
// Use full width scaling
val scaleX = canvasWidth / dims.viewBoxWidth
val scale = scaleX  // Use scaleX instead of min(scaleX, scaleY)

// Align to top-left
val translateX = -dims.viewBoxX * scale
val translateY = -dims.viewBoxY * scale
```

This ensures the SVG fills the full width of the container, and the height is determined by the aspect ratio.

### Coordinate Transformation for Tap Detection
The same scaling factor is used for tap detection to ensure accurate hit testing:
```kotlin
val svgX = (tapOffset.x - translateX) / scale
val svgY = (tapOffset.y - translateY) / scale
```

## Files Changed

### 1. `SvgMapView.kt`
- Changed imports: `fillMaxSize` → `fillMaxWidth`, added `aspectRatio`
- Added aspect ratio calculation and modifier
- Updated scaling to use `scaleX` only
- Removed centering logic
- Increased stroke widths (2f→4f, 3f→6f)
- Increased tap tolerance (10f→20f)

### 2. `AreaDetailScreen.kt`
- Removed fixed `.height(400.dp)` from Card
- Removed `.fillMaxSize()` from SvgMapView modifier
- Map now adapts height based on aspect ratio

## Benefits

1. **Better Space Utilization**: Map fills the full width of the screen
2. **Proper Aspect Ratio**: SVG is displayed without distortion
3. **Easier Tapping**: Thicker lines and larger tap tolerance make interaction more forgiving
4. **Responsive Design**: Height adapts automatically to different SVG aspect ratios
5. **Cleaner Layout**: No wasted space with fixed heights

## Testing

✅ All unit tests passing  
✅ Lint checks clean  
✅ Build successful

## Future Improvements

Potential enhancements to consider:
1. Add pinch-to-zoom functionality
2. Add pan/scroll gestures for large maps
3. Add min/max height constraints for very wide or tall SVGs
4. Add visual feedback on tap (ripple effect)
