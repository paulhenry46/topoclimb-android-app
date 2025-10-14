# SVG ViewBox Parsing Fix

## Problem Description

The SVG map was shifted to the left in the Android application, although it rendered correctly in Inkscape and other SVG viewers.

### Root Cause

The issue was in `SvgParser.kt` at line 68:

```kotlin
val parts = viewBox.split(" ").map { it.toFloat() }
```

This parsing method fails when the SVG's `viewBox` attribute contains multiple consecutive spaces, which is common in SVG files edited by various tools or manually formatted.

### Example Failure

Given a viewBox with multiple spaces:
```
viewBox="0  0  786.15863  583.85938"
```

The old code would:
1. Split by single space: `["0", "", "0", "", "786.15863", "", "583.85938"]`
2. Try to convert empty strings to floats → **FAIL**
3. Return `null` for dimensions
4. Map displays incorrectly or not at all

## Solution

Changed the viewBox parsing to:

```kotlin
val parts = viewBox.trim().split(Regex("\\s+")).map { it.toFloat() }
```

This correctly handles:
- Single spaces: `"0 0 100 200"`
- Multiple spaces: `"0  0  100  200"`
- Leading/trailing spaces: `" 0 0 100 200 "`
- Mixed whitespace: `"0 0  100   200"`

## Changes Made

### 1. `app/src/main/java/com/example/topoclimb/utils/SvgParser.kt`

**Before:**
```kotlin
val parts = viewBox.split(" ").map { it.toFloat() }
```

**After:**
```kotlin
val parts = viewBox.trim().split(Regex("\\s+")).map { it.toFloat() }
```

### 2. `app/src/test/java/com/example/topoclimb/utils/SvgParserTest.kt` (NEW)

Added comprehensive unit tests including:
- Normal spacing
- Multiple consecutive spaces (the bug fix)
- Leading and trailing spaces
- Non-zero viewBox offsets
- Decimal values
- Real-world SVG example from the problem statement

## Testing

All tests pass:
```
BUILD SUCCESSFUL
17 tests completed, 0 failed
```

Key test demonstrating the fix:
```kotlin
@Test
fun parseSvg_withMultipleSpaces_parsesViewBoxCorrectly() {
    val svgContent = """
        <svg viewBox="0  0  100  200">
            <path d="M 10 10 L 20 20" id="sector_1"/>
        </svg>
    """.trimIndent()
    
    val (dimensions, _) = SvgParser.parseSvg(svgContent)
    
    assertNotNull(dimensions)
    assertEquals(0f, dimensions!!.viewBoxX, 0.001f)
    assertEquals(0f, dimensions.viewBoxY, 0.001f)
    assertEquals(100f, dimensions.viewBoxWidth, 0.001f)
    assertEquals(200f, dimensions.viewBoxHeight, 0.001f)
}
```

## Impact

This fix ensures that SVG maps with any whitespace formatting in the viewBox attribute will parse correctly and display properly in the application, matching the rendering in standard SVG viewers like Inkscape.

## Technical Details

### Why Regex("\\s+") Works

- `\s` matches any whitespace character (space, tab, newline, etc.)
- `+` means "one or more"
- Combined with `trim()`, this handles all edge cases

### Backward Compatibility

This change is fully backward compatible. SVGs that worked before continue to work, and SVGs that failed before now work correctly.
