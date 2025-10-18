# Route Detail Bottom Sheet Implementation

## Overview
This document describes the implementation of the route detail bottom sheet that displays comprehensive information about a climbing route when tapped.

## Features Implemented

### 1. Bottom Sheet with Tabs
- **Modal Bottom Sheet**: Opens when a route card is tapped in the Area Detail Screen
- **Two Tabs**: "Overview" and "Logs"
  - Overview tab: Shows complete route information
  - Logs tab: Placeholder for future implementation

### 2. Overview Tab Components

#### A. Route Photo with SVG Overlay
- Displays the full route photo (from `picture` field, falls back to `thumbnail`)
- Overlays an SVG circle graphic (from `circle` field) that shows where the route begins on the photo
- SVG is scaled to match the image dimensions exactly using a WebView implementation

#### B. Focus Toggle
- Located at the bottom-left of the photo
- Toggle switch with "Focus" label
- Controls focus mode state (can be used to highlight/zoom the route)

#### C. Route Information Header
- **Left side**: 
  - Route name in bold headline style
  - Sector/Line number (e.g., "Sector n°3" or "Line n°12")
- **Right side**:
  - Bookmark button (star icon) - toggleable
  - Success button (check icon) - toggleable to mark route as completed

#### D. Route Metadata Card
Displays key route information with icons:
- **Openers**: Names of the route setters/creators
- **Grade**: Climbing difficulty grade (e.g., "7c")
- **Date of Creation**: Formatted date when the route was created

#### E. Dismantling Information Box
- Green-colored info card with checkbox
- Message: "There are no plans to dismantle this track at this time."
- Provides climbers with assurance about route availability

### 3. Empty Logs Tab
- Placeholder component showing "Logs feature coming soon..."
- Ready for future enhancement

## Data Model Updates

### Route Model
Added new fields to support the bottom sheet:
```kotlin
val picture: String?       // URL of the full route picture
val circle: String?         // URL of the SVG circle overlay
val openers: String?        // Names of route openers
```

### RouteWithMetadata
Updated to delegate the new Route fields for consistent access.

## ViewModel

### RouteDetailViewModel
- Manages route detail state including loading status
- Fetches route details from API by route ID
- Loads and manages SVG circle content from URL
- Handles focus mode toggle state

State includes:
- `route`: The full route data
- `isLoading`: Loading indicator
- `error`: Error message if any
- `circleSvgContent`: Loaded SVG content for overlay
- `isFocusMode`: Focus mode toggle state

## Technical Implementation

### SVG Rendering
- Uses Android WebView to render SVG content
- Positioned absolutely over the route photo
- Scaled to match photo dimensions
- Transparent background for overlay effect

### Date Formatting
- Parses API date format: `yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'`
- Displays in user-friendly format: `MMM dd, yyyy`
- Handles parsing errors gracefully

### UI Components
- Material Design 3 components throughout
- Consistent with app theme and color scheme
- Responsive layout with scrolling support
- Proper spacing and visual hierarchy

## User Interaction Flow

1. User views routes in Area Detail Screen
2. User taps on a route card
3. Bottom sheet slides up from bottom
4. "Overview" tab is selected by default
5. User can:
   - View route photo with circle overlay
   - Toggle focus mode
   - Bookmark the route
   - Mark route as succeeded
   - Read route metadata
   - Switch to Logs tab (future feature)
   - Dismiss sheet by swiping down or tapping outside

## Files Modified/Created

### New Files
- `app/src/main/java/com/example/topoclimb/ui/components/RouteDetailBottomSheet.kt`
- `app/src/main/java/com/example/topoclimb/viewmodel/RouteDetailViewModel.kt`

### Modified Files
- `app/src/main/java/com/example/topoclimb/data/Route.kt` - Added new fields
- `app/src/main/java/com/example/topoclimb/network/RetrofitInstance.kt` - Exposed OkHttpClient
- `app/src/main/java/com/example/topoclimb/ui/screens/AreaDetailScreen.kt` - Added bottom sheet trigger

## Future Enhancements

1. **Logs Tab Implementation**: Add user logs, ascents, and comments
2. **Focus Mode Functionality**: Implement zoom/highlight when focus is enabled
3. **Persistent State**: Save bookmark and success status to backend
4. **Photo Gallery**: Support multiple route photos with swipe navigation
5. **Share Functionality**: Allow sharing route details with other climbers
6. **Offline Support**: Cache route details for offline viewing

## Testing

- ✅ Build successful
- ✅ All existing unit tests pass
- ✅ No compilation errors
- ✅ Proper error handling for missing data
- ⏳ Manual UI testing required with actual API data

## API Requirements

The implementation expects these fields in the route API response:
- `picture` (optional): Full route photo URL
- `circle` (optional): SVG circle overlay URL
- `openers` (optional): String with opener names
- `created_at` (optional): ISO 8601 date string

If these fields are not available in the API response, the UI handles them gracefully:
- Falls back to thumbnail if picture is missing
- Hides circle overlay if not available
- Hides metadata sections if data is missing
