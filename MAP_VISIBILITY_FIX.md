# Map Visibility Fix

## Problem
The map area container was being displayed but the map itself was not visible. Users could see an empty card where the map should be.

## Root Cause
The Area data model was using the wrong field name for the SVG map URL. The API returns the field as `svg_graphic`, but the code was looking for `svg_schema`. This resulted in the `svgMap` field always being `null`, preventing the map from loading.

## Solution
1. **Updated API field mapping**: Changed the `@SerializedName` annotation in `Area.kt` from `"svg_schema"` to `"svg_graphic"`
2. **Added minimum height**: Added `heightIn(min = 200.dp)` to the WebView in `SvgWebMapView.kt` to ensure the map container has a visible height even if there are rendering issues

## Files Changed

### 1. `app/src/main/java/com/example/topoclimb/data/Area.kt`
```kotlin
// Before
@SerializedName("svg_schema") val svgMap: String?

// After
@SerializedName("svg_graphic") val svgMap: String?
```

### 2. `app/src/main/java/com/example/topoclimb/ui/components/SvgWebMapView.kt`
Added minimum height constraint to the WebView:
```kotlin
modifier = modifier
    .fillMaxWidth()
    .heightIn(min = 200.dp)  // New: ensures minimum visibility
    .then(aspectRatioModifier)
```

### 3. `app/src/test/java/com/example/topoclimb/data/DataModelsTest.kt`
Added a test case to verify the `svg_graphic` field is properly deserialized:
```kotlin
@Test
fun areaResponse_deserializesSvgGraphicCorrectly() {
    val json = """
        {
            "data": {
                "id": 1,
                "name": "Cuvier",
                "description": "Classic sector",
                "latitude": 48.4044,
                "longitude": 2.6992,
                "siteId": 1,
                "svg_graphic": "https://example.com/map.svg"
            }
        }
    """.trimIndent()
    
    val response = gson.fromJson(json, AreaResponse::class.java)
    
    assertNotNull(response)
    assertNotNull(response.data)
    assertEquals("Cuvier", response.data.name)
    assertEquals("https://example.com/map.svg", response.data.svgMap)
}
```

## Expected Behavior After Fix
- When an area has a `svg_graphic` field in the API response, the map will now load correctly
- The map container will have a minimum height of 200dp, ensuring it's always visible
- Users can interact with the map to select sectors and view routes

## Testing
✅ All unit tests pass (including the new test for `svg_graphic`)
✅ Lint check clean (0 errors)
✅ Code compiles successfully

## Technical Details
The fix is minimal and surgical - it only changes the field name that Gson uses to deserialize the API response. The `svgMap` property name in the Kotlin code remains the same, so no other code changes are required. The minimum height ensures that even if there are edge cases with SVG dimensions, the map container will be visible to users.
