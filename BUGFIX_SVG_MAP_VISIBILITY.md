# Bugfix: SVG Map Visibility Issue

## Problem
Users reported that the SVG map was not visible on the Area Detail screen, even though:
- The "Topo Map" header text was displayed
- Tapping on the empty space would select sectors (indicating the Canvas existed)
- The map component was rendering but not visible

## Root Cause
In `SvgMapView.kt`, when SVG dimensions were not available (`svgDimensions` is null), the aspect ratio modifier fell back to an empty `Modifier`:

```kotlin
val aspectRatioModifier = svgDimensions?.let { dims ->
    Modifier.aspectRatio(dims.viewBoxWidth / dims.viewBoxHeight)
} ?: Modifier  // Empty modifier - no height constraint!
```

When the Canvas component had no height constraint in a LazyColumn layout, it would collapse to zero height, making it invisible.

## Solution
Added a fallback height constraint when SVG dimensions are not available:

```kotlin
val aspectRatioModifier = svgDimensions?.let { dims ->
    Modifier.aspectRatio(dims.viewBoxWidth / dims.viewBoxHeight)
} ?: Modifier.height(300.dp)  // Fallback height when no dimensions available
```

Additionally, added padding around the SvgMapView in `AreaDetailScreen.kt` for better visual presentation:

```kotlin
modifier = Modifier.padding(16.dp)
```

## Files Changed
1. `app/src/main/java/com/example/topoclimb/ui/components/SvgMapView.kt`
   - Added `height` and `dp` imports
   - Changed fallback modifier to `Modifier.height(300.dp)`

2. `app/src/main/java/com/example/topoclimb/ui/screens/AreaDetailScreen.kt`
   - Added `.padding(16.dp)` to SvgMapView modifier

## Testing
✅ Build successful: `./gradlew assembleDebug`  
✅ All tests passing: `./gradlew test`

## Result
The SVG map is now visible even when SVG dimensions are not available from the server, with a reasonable fallback height of 300dp.
