# TopoClimb Android App - UI Screenshots Guide

This document provides a visual representation of the new site UI features.

## 1. Sites List Screen

The main sites screen now displays cards with:
- Background banner image from `site.banner`
- Circular logo from `site.profilePicture` 
- Site name and description with semi-transparent overlay

### Layout Description
```
╔═══════════════════════════════════════════════════╗
║                 Climbing Sites                    ║ ← TopAppBar
╠═══════════════════════════════════════════════════╣
║                                                   ║
║  ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓  ║
║  ┃ [Banner: Forest/rocks background image]  ┃  ║
║  ┃   ╭─────╮                                 ┃  ║
║  ┃   │Logo │ ← Circular profile picture     ┃  ║
║  ┃   ╰─────╯                                 ┃  ║
║  ┃                                           ┃  ║
║  ┃                                           ┃  ║
║  ┃   Fontainebleau                           ┃  ║
║  ┃   Famous bouldering area in France       ┃  ║
║  ┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛  ║
║                                                   ║
║  ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓  ║
║  ┃ [Banner: Mountain cliff background]      ┃  ║
║  ┃   ╭─────╮                                 ┃  ║
║  ┃   │Logo │                                 ┃  ║
║  ┃   ╰─────╯                                 ┃  ║
║  ┃                                           ┃  ║
║  ┃                                           ┃  ║
║  ┃   Ceüse                                   ┃  ║
║  ┃   Legendary sport climbing destination   ┃  ║
║  ┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛  ║
║                                                   ║
╠═══════════════════════════════════════════════════╣
║  🗺️ Sites    📋 Routes    🏡 Areas                ║ ← Bottom Nav
╚═══════════════════════════════════════════════════╝
```

### Key Visual Features
- **Card Height**: 200dp for consistent appearance
- **Logo**: 60dp circle at top of card
- **Overlay**: 60% opacity for text readability
- **Typography**: 
  - Site name: Headline Small (bold)
  - Description: Body Medium (max 2 lines)

---

## 2. Site Detail Screen (NEW)

When you tap on a site card, you navigate to the detail screen:

### Layout Description
```
╔═══════════════════════════════════════════════════╗
║  ← Fontainebleau                                  ║ ← TopAppBar
╠═══════════════════════════════════════════════════╣
║                                                   ║
║  ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓  ║
║  ┃                                           ┃  ║
║  ┃  [Full Width Banner Image - 200dp high]  ┃  ║
║  ┃                                           ┃  ║
║  ┣━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┫  ║
║  ┃  Fontainebleau                            ┃  ║
║  ┃                                           ┃  ║
║  ┃  World-famous bouldering area in France. ┃  ║
║  ┃  Over 30,000 problems spread across      ┃  ║
║  ┃  multiple sectors in the forest of       ┃  ║
║  ┃  Fontainebleau.                           ┃  ║
║  ┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛  ║
║                                                   ║
║  Areas                                            ║ ← Section Header
║  ┌───────────────────────────────────────────┐   ║
║  │ Cuvier                                    │   ║
║  │ Classic sector with famous problems      │   ║
║  │ Location: 48.4044, 2.6992                │   ║
║  └───────────────────────────────────────────┘   ║
║                                                   ║
║  ┌───────────────────────────────────────────┐   ║
║  │ Bas Cuvier                                │   ║
║  │ Beginner-friendly sector                  │   ║
║  │ Location: 48.4055, 2.6988                │   ║
║  └───────────────────────────────────────────┘   ║
║                                                   ║
║  ┌───────────────────────────────────────────┐   ║
║  │ Apremont                                  │   ║
║  │ Diverse sector with all grades            │   ║
║  │ Location: 48.4101, 2.7012                │   ║
║  └───────────────────────────────────────────┘   ║
║                                                   ║
║  Contests                                         ║ ← Section Header
║  ┌───────────────────────────────────────────┐   ║
║  │ Summer Boulder Festival                   │   ║
║  │ Annual competition in the forest          │   ║
║  │ Start: 2025-07-01 • End: 2025-07-03      │   ║
║  └───────────────────────────────────────────┘   ║
║                                                   ║
║  ┌───────────────────────────────────────────┐   ║
║  │ Youth Climbing Championship               │   ║
║  │ Competition for young climbers            │   ║
║  │ Start: 2025-09-15 • End: 2025-09-16      │   ║
║  └───────────────────────────────────────────┘   ║
║                                                   ║
╠═══════════════════════════════════════════════════╣
║  🗺️ Sites    📋 Routes    🏡 Areas                ║ ← Bottom Nav
╚═══════════════════════════════════════════════════╝
```

---

## 3. Empty State (No Areas/Contests)

When a site has no areas or contests:

```
╔═══════════════════════════════════════════════════╗
║  ← New Site Name                                  ║
╠═══════════════════════════════════════════════════╣
║                                                   ║
║  ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓  ║
║  ┃  [Banner Image]                           ┃  ║
║  ┣━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┫  ║
║  ┃  New Site Name                            ┃  ║
║  ┃  Just opened climbing site                ┃  ║
║  ┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛  ║
║                                                   ║
║  ┌───────────────────────────────────────────┐   ║
║  │                                           │   ║
║  │   No areas or contests available          │   ║
║  │        for this site.                     │   ║
║  │                                           │   ║
║  └───────────────────────────────────────────┘   ║
║                                                   ║
╚═══════════════════════════════════════════════════╝
```

---

## 4. User Interaction Flow

### Browsing Sites
1. User opens app → Sites screen (default)
2. Sees list of site cards with banner images and logos
3. Scrolls through available sites

### Viewing Site Details
1. User taps on a site card
2. App navigates to Site Detail Screen
3. User sees:
   - Site header with full banner
   - Complete site description
   - List of areas at the site
   - List of contests at the site
4. User can tap back button to return to sites list

### Navigation Between Sections
1. Bottom navigation allows switching between:
   - Sites (shows all sites)
   - Routes (shows all routes, can filter by site)
   - Areas (shows all areas)

---

## 5. Color and Style

### Material 3 Theme
The app uses Material Design 3 with:
- Dynamic color support on Android 12+
- Light/dark theme support
- Elevation and shadows for depth
- Rounded corners on cards

### Typography Hierarchy
- **Headline Large**: Screen titles in TopAppBar
- **Headline Medium**: Site name on detail screen
- **Headline Small**: Site name on cards
- **Title Large**: Section headers (Areas, Contests)
- **Title Medium**: Item names in lists
- **Body Medium**: Descriptions
- **Body Small**: Secondary info (dates, coordinates)

### Card Elevations
- Site cards: 4dp elevation (increased for prominence)
- Area/Contest cards: 2dp elevation (standard)

---

## 6. Image Loading

### Coil Integration
Images are loaded asynchronously using Coil:
- Smooth loading without blocking UI
- Automatic caching
- Graceful fallback when images unavailable

### Image Display
- **Banners**: ContentScale.Crop (fills space, maintains aspect)
- **Logos**: ContentScale.Crop with CircleShape clip

---

## 7. Responsive Design

### Different Screen Sizes
- Cards adapt to screen width (fillMaxWidth)
- Fixed card height (200dp) for consistency
- Proper padding and spacing
- Scrollable lists for any number of items

### Orientation
- Portrait: Optimized vertical scrolling
- Landscape: Cards remain full-width for better visibility

---

## Implementation Notes

### Code Highlights

**SiteItem Card with Banner & Logo:**
```kotlin
Card(height = 200.dp) {
    Box {
        // Banner background
        AsyncImage(model = site.banner, ...)
        
        // Overlay for readability
        Surface(alpha = 0.6f) {}
        
        // Content
        Column {
            // Logo
            AsyncImage(
                model = site.profilePicture,
                modifier = Modifier.size(60.dp).clip(CircleShape)
            )
            // Site info
            Text(site.name)
            Text(site.description)
        }
    }
}
```

**Site Detail Screen Structure:**
```kotlin
LazyColumn {
    // Header card with banner
    item { SiteHeaderCard(site) }
    
    // Areas section
    if (areas.isNotEmpty()) {
        item { Text("Areas") }
        items(areas) { AreaItem(it) }
    }
    
    // Contests section
    if (contests.isNotEmpty()) {
        item { Text("Contests") }
        items(contests) { ContestItem(it) }
    }
}
```

---

## Accessibility

- All images have proper contentDescription
- Sufficient color contrast (overlay ensures text readability)
- Proper touch targets (cards are clickable with standard size)
- Screen reader support with semantic elements
- Back button clearly labeled

---

## Performance

- Lazy loading with LazyColumn (only renders visible items)
- Async image loading (non-blocking)
- Efficient state management with StateFlow
- Minimal recomposition with proper state handling

