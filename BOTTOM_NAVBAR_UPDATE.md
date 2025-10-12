# Bottom Navbar Update - Implementation Summary

## Overview
Updated the bottom navigation bar to have 3 buttons instead of the previous layout, added favorite site functionality, created a profile screen with placeholder data, and fixed the Area.svgMap to fetch map content from URL.

## Changes Made

### 1. Bottom Navigation Items Updated

**File:** `app/src/main/java/com/example/topoclimb/ui/TopoClimbApp.kt`

**Changed from:**
- Sites (Place icon, "Sites" label)
- AllRoutes (List icon, "Routes" label)
- Areas (Home icon, "Areas" label)

**Changed to:**
- Sites (Place icon, "Sites" label)
- Favorite (Star icon, "Favorite" label)
- Profile (Person icon, "You" label)

### 2. Profile Screen Created

**File:** `app/src/main/java/com/example/topoclimb/ui/screens/ProfileScreen.kt` (NEW)

Features:
- Profile picture (placeholder from `https://via.placeholder.com/150`)
- User name: "John Climber"
- Email: "john.climber@example.com"
- Climbing stats card with:
  - Routes: 42
  - Sites: 8
  - Grade: 7a+
- About section with note: "This is a placeholder profile. Authentication will be added in the future."

### 3. Favorite Site Functionality

**Files modified:**
- `app/src/main/java/com/example/topoclimb/viewmodel/SitesViewModel.kt`
- `app/src/main/java/com/example/topoclimb/ui/screens/SitesScreen.kt`

**Changes:**
- Added `favoriteSiteId` to `SitesUiState` data class
- Added `toggleFavorite(siteId: Int)` function to SitesViewModel
- Updated SitesScreen to support `favoriteOnly` parameter
- Added star button to each site card:
  - Filled star icon when site is favorite
  - Outlined star icon when site is not favorite
  - Click to toggle favorite status
- Favorite screen shows only the favorited site
- Empty state message when no favorite is selected: "No favorite site selected. Tap the star on a site card to set it as favorite."

### 4. SVG Map URL Fetching

**Files modified:**
- `app/src/main/java/com/example/topoclimb/viewmodel/AreaDetailViewModel.kt`
- `app/src/main/java/com/example/topoclimb/ui/screens/AreaDetailScreen.kt`

**Changes:**
- Updated AreaDetailUiState to include `svgMapContent: String?` field
- Modified `loadAreaDetails()` to fetch SVG content from the URL stored in `area.svgMap`
- Uses `URL(mapUrl).readText()` to fetch the actual map content
- Handles errors gracefully (returns null if fetch fails)
- Updated AreaDetailScreen to use `uiState.svgMapContent` instead of `uiState.area?.svgMap`

### 5. Navigation Updates

**File:** `app/src/main/java/com/example/topoclimb/ui/TopoClimbApp.kt`

**Added routes:**
- `favorite` route → Shows SitesScreen with favoriteOnly=true
- `profile` route → Shows ProfileScreen

**Removed routes:**
- `areas` route (removed AreasScreen from bottom nav, but area detail screens still accessible via site details)

## Icon Changes

**Imports updated:**
```kotlin
// Removed
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home

// Added
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star  // For unfavorited state
```

## User Experience Flow

### Viewing Sites
1. User opens app → Sites screen (default)
2. User sees site cards with star buttons in top-right corner
3. User can tap star to mark/unmark a site as favorite

### Favorite Site
1. User taps "Favorite" in bottom nav
2. If a favorite is set: Shows the favorite site card
3. If no favorite: Shows empty state message
4. User can navigate to site details from the favorite screen

### Profile
1. User taps "You" in bottom nav
2. Shows profile screen with placeholder user data
3. Displays profile picture, name, email, stats, and about section

### Area Map Viewing
1. User navigates to site details → area details
2. If area has an svgMap URL:
   - App fetches the SVG content from the URL
   - Displays the map in a WebView
3. If fetch fails or no URL: Map section is not displayed

## Testing

All existing tests pass:
- Build: ✅ `./gradlew assembleDebug`
- Tests: ✅ `./gradlew test`

## Future Enhancements

1. **Favorite Persistence:** Store favorite site ID in SharedPreferences or database
2. **User Authentication:** Replace placeholder profile data with real user data
3. **Multiple Favorites:** Allow users to favorite multiple sites
4. **Profile Editing:** Allow users to edit their profile information
5. **Map Caching:** Cache fetched SVG maps to avoid repeated network requests
6. **Error Handling:** Show error message if map fetch fails instead of silently hiding the map

## Files Changed

### Modified (5 files)
1. `app/src/main/java/com/example/topoclimb/ui/TopoClimbApp.kt`
2. `app/src/main/java/com/example/topoclimb/ui/screens/SitesScreen.kt`
3. `app/src/main/java/com/example/topoclimb/viewmodel/SitesViewModel.kt`
4. `app/src/main/java/com/example/topoclimb/viewmodel/AreaDetailViewModel.kt`
5. `app/src/main/java/com/example/topoclimb/ui/screens/AreaDetailScreen.kt`

### Created (1 file)
1. `app/src/main/java/com/example/topoclimb/ui/screens/ProfileScreen.kt`

## Breaking Changes

None. All existing functionality is preserved. The bottom navigation changed but all screens are still accessible:
- Sites screen: Via bottom nav "Sites" button
- Areas: Via site detail screens (click on a site, then click on an area)
- Routes: Via site detail screens or programmatically via navigation

## Backwards Compatibility

- All existing data models remain unchanged
- All existing API calls remain unchanged
- Navigation to area and route screens still works via site details
