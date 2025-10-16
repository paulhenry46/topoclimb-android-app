# Area Map Refactoring

## Overview
This refactoring simplifies the area map feature by removing interactive sector selection from the SVG map and replacing it with a dedicated UI selector.

## Changes Made

### 1. New Components

#### SimpleSvgView.kt
- **Purpose**: Display SVG maps as static images without interaction
- **Features**:
  - WebView-based rendering with JavaScript disabled
  - Aspect ratio preservation based on SVG dimensions
  - Transparent background for native appearance
  - No tap/click handling

#### SectorSelector.kt
- **Purpose**: Provide a clean UI for selecting sectors
- **Features**:
  - Dropdown menu with all available sectors
  - "All Sectors" option to show all routes
  - Clear button to deselect current sector
  - Shows sector name and description
  - Material 3 design with ExposedDropdownMenuBox

### 2. Modified Components

#### AreaDetailScreen.kt
**Before**:
- SVG map was interactive with tap-to-select functionality
- Instructions to "Tap on a sector to view its routes"
- Selected sector shown with red highlight on map
- Routes filtered by tapping map sectors

**After**:
- SVG map in collapsible card (non-interactive)
- Dedicated sector selector dropdown above the map
- Map can be expanded/collapsed with arrow icon
- Routes section shows selected sector name instead of just ID
- Better visual separation of concerns

#### AreaDetailViewModel.kt
**Changes**:
- Renamed `onSectorTapped()` to `onSectorSelected()`
- Removed toggle logic (no longer needed since selection is explicit in dropdown)
- Simplified sector selection - directly sets selected sector without toggling

### 3. Behavior Changes

| Aspect | Before | After |
|--------|--------|-------|
| Map Interaction | Tap sectors to select | View only, no interaction |
| Sector Selection | Tap on map paths | Dropdown menu |
| Selection Feedback | Red highlight on map | Dropdown shows selected sector |
| Deselection | Tap selected sector again | Click clear button or select "All Sectors" |
| Routes Title | "Routes in Sector 123" | "Routes in Sector Name" |
| Map Visibility | Always visible | Collapsible card |

### 4. User Experience Improvements

1. **Clearer Sector Selection**: Dropdown shows all available sectors with names and descriptions
2. **Better Accessibility**: Standard dropdown is more accessible than SVG tap targets
3. **Reduced Complexity**: Removed JavaScript interaction layer
4. **Better Performance**: Simpler WebView without event handlers
5. **Space Efficiency**: Collapsible map saves screen space when not needed

## Code Structure

```
app/src/main/java/com/example/topoclimb/
├── ui/
│   ├── components/
│   │   ├── SimpleSvgView.kt (NEW)
│   │   ├── SectorSelector.kt (NEW)
│   │   └── SvgWebMapView.kt (DEPRECATED - kept for reference)
│   └── screens/
│       └── AreaDetailScreen.kt (MODIFIED)
└── viewmodel/
    └── AreaDetailViewModel.kt (MODIFIED)
```

## Technical Details

### SimpleSvgView
- Disables JavaScript for security and performance
- Uses `aspectRatio` modifier to maintain SVG proportions
- Falls back to square aspect ratio if dimensions unavailable
- Minimal HTML wrapper around SVG content

### SectorSelector
- Uses Material 3 ExposedDropdownMenuBox
- Shows sector descriptions in dropdown items
- Clear button only appears when sector is selected
- Handles empty sectors list gracefully

## Migration Notes

- Old `SvgWebMapView` component is still present but unused
- Can be removed in a future cleanup if no other screens need it
- All sector selection now goes through `onSectorSelected()` instead of `onSectorTapped()`

## Testing

- ✅ Build successful
- ✅ All unit tests passing
- ✅ No compilation warnings (except one deprecation warning fixed)
- 🔲 Manual UI testing recommended to verify user experience

## Future Considerations

1. Consider removing `SvgWebMapView.kt` entirely if no longer needed
2. Could add sector filtering/search if sector list becomes large
3. Could add visual indicators on the static map (e.g., colored overlays) for selected sector
4. Could make map expansion state persistent across configuration changes
