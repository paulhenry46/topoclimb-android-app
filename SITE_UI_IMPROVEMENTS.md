# Site UI Improvements - Implementation Details

## Overview
This document describes the UI improvements made to the TopoClimb Android app for displaying climbing sites.

## Changes Made

### 1. Enhanced Site Cards (SitesScreen)

The site list now displays cards with:
- **Background Image**: The card uses the site's `banner` property as a background image (200dp height)
- **Site Logo**: The `profilePicture` property is displayed as a circular logo (60dp) at the top of the card
- **Overlay**: A semi-transparent overlay ensures text readability over the banner image
- **Site Information**: Site name (headline style) and description (2 lines max) are displayed at the bottom

### 2. New Site Detail Screen

When clicking on a site card, users are taken to a new detail screen that shows:
- **Site Header**: Full banner image (200dp height) with site name and complete description
- **Areas Section**: Lists all climbing areas associated with the site
  - Each area shows: name, description, and GPS coordinates (if available)
- **Contests Section**: Lists all contests available at the site
  - Each contest shows: name, description, start date, and end date
- **Empty State**: If no areas or contests exist, a friendly message is displayed

### 3. Updated Navigation

Navigation now includes:
- `sites` → Main sites list (unchanged)
- `site/{siteId}` → NEW: Site detail screen with areas and contests
- `routes/{siteId}` → Routes for a specific site (unchanged)

### 4. New Data Models

#### Contest.kt
```kotlin
data class Contest(
    val id: Int,
    val name: String,
    val description: String?,
    val siteId: Int?,
    val startDate: String?,
    val endDate: String?,
    val createdAt: String?,
    val updatedAt: String?
)
```

#### Response Wrappers
All API responses now properly handle the `{"data": ...}` structure:
- `AreasResponse` - wraps areas list
- `ContestsResponse` - wraps contests list
- `SitesResponse` - wraps sites list (already existed)

### 5. New API Endpoints

Added to `TopoClimbApiService`:
- `GET sites/{siteId}/areas` → Returns `AreasResponse` with areas for a specific site
- `GET sites/{siteId}/contests` → Returns `ContestsResponse` with contests for a specific site

### 6. Image Loading

Using Coil for efficient image loading:
- Asynchronous loading of banner and logo images
- Proper content scaling (Crop for banners, Crop for logos)
- Graceful handling when images are not available

## UI Component Hierarchy

```
SitesScreen
├── TopAppBar ("Climbing Sites")
└── LazyColumn
    └── SiteItem (Card with banner + logo)
        ├── AsyncImage (banner background)
        ├── Surface (overlay for text readability)
        └── Column
            ├── AsyncImage (circular logo)
            └── Column
                ├── Text (site name)
                └── Text (description)

SiteDetailScreen
├── TopAppBar (site name + back button)
└── LazyColumn
    ├── Site Header Card
    │   ├── AsyncImage (banner)
    │   └── Column (name + description)
    ├── "Areas" Section Header
    ├── AreaItem Cards
    ├── "Contests" Section Header
    └── ContestItem Cards
```

## Testing Notes

### Manual Testing Checklist
- [ ] Sites list displays correctly with banner images
- [ ] Site logos appear as circular images
- [ ] Clicking a site navigates to detail screen
- [ ] Site detail screen shows site information
- [ ] Areas are displayed when available
- [ ] Contests are displayed when available
- [ ] Empty state shows when no areas/contests
- [ ] Back button returns to sites list
- [ ] Images load asynchronously without blocking UI
- [ ] Text remains readable over various banner images

### API Response Format

The app expects all API responses in this format:
```json
{
  "data": [
    // ... array of objects or single object
  ]
}
```

Example for areas by site:
```json
{
  "data": [
    {
      "id": 1,
      "name": "Sector A",
      "description": "Main climbing sector",
      "latitude": 48.404,
      "longitude": 2.699,
      "siteId": 1
    }
  ]
}
```

Example for contests by site:
```json
{
  "data": [
    {
      "id": 1,
      "name": "Summer Competition 2025",
      "description": "Annual climbing competition",
      "site_id": 1,
      "start_date": "2025-07-01",
      "end_date": "2025-07-03"
    }
  ]
}
```

## Implementation Benefits

1. **Better Visual Experience**: Cards with banner images make sites more appealing and recognizable
2. **More Information**: Site logos help with brand recognition
3. **Complete Site Information**: Areas and contests are now accessible from the site detail screen
4. **Proper API Integration**: All responses follow the `{"data": ...}` pattern consistently
5. **Extensible**: Easy to add more sections to the site detail screen (e.g., routes, photos, reviews)

## Future Enhancements

Potential improvements for the future:
- Add pull-to-refresh for site details
- Cache images for offline viewing
- Add animations when navigating to detail screen
- Show route count and difficulty range on site cards
- Add map view showing area locations within a site
- Filter/search contests by date or status
