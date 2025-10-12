# UI Changes - Bottom Navbar Update

## Before vs After

### Bottom Navigation Bar

**BEFORE:**
```
┌──────────────────────────────────────────────────┐
│  🗺️ Sites    📋 Routes    🏠 Areas              │
└──────────────────────────────────────────────────┘
```

**AFTER:**
```
┌──────────────────────────────────────────────────┐
│  🗺️ Sites    ⭐ Favorite    👤 You               │
└──────────────────────────────────────────────────┘
```

## New Screens

### 1. Sites Screen (Enhanced)

Each site card now has a **star button** in the top-right corner:

```
╔════════════════════════════════════════╗
║ [Banner Image]                     ⭐  ║
║   🔵                                   ║
║   Logo                                 ║
║                                        ║
║                                        ║
║   Site Name                            ║
║   Site description...                  ║
╚════════════════════════════════════════╝
```

- **Outlined star (☆)**: Site is not favorited
- **Filled star (⭐)**: Site is favorited
- Click the star to toggle favorite status

### 2. Favorite Screen (NEW)

Shows the favorited site when user taps "Favorite" in bottom nav:

**When favorite is set:**
```
╔════════════════════════════════════════╗
║        Favorite Site                   ║
╠════════════════════════════════════════╣
║                                        ║
║ ╔════════════════════════════════════╗ ║
║ ║ [Banner Image]                 ⭐  ║ ║
║ ║   🔵                               ║ ║
║ ║   Logo                             ║ ║
║ ║                                    ║ ║
║ ║                                    ║ ║
║ ║   Your Favorite Site               ║ ║
║ ║   Description...                   ║ ║
║ ╚════════════════════════════════════╝ ║
║                                        ║
╚════════════════════════════════════════╝
```

**When no favorite is set:**
```
╔════════════════════════════════════════╗
║        Favorite Site                   ║
╠════════════════════════════════════════╣
║                                        ║
║  ╔══════════════════════════════════╗  ║
║  ║  No favorite site selected.      ║  ║
║  ║                                  ║  ║
║  ║  Tap the star on a site card to ║  ║
║  ║  set it as favorite.             ║  ║
║  ╚══════════════════════════════════╝  ║
║                                        ║
╚════════════════════════════════════════╝
```

### 3. Profile Screen (NEW)

Shows user profile with placeholder data:

```
╔════════════════════════════════════════╗
║             Profile                    ║
╠════════════════════════════════════════╣
║                                        ║
║              ╭─────────╮               ║
║              │         │               ║
║              │  Photo  │               ║
║              │         │               ║
║              ╰─────────╯               ║
║                                        ║
║           John Climber                 ║
║     john.climber@example.com           ║
║                                        ║
║  ╔══════════════════════════════════╗  ║
║  ║      Climbing Stats              ║  ║
║  ║                                  ║  ║
║  ║   42        8         7a+        ║  ║
║  ║ Routes    Sites      Grade       ║  ║
║  ╚══════════════════════════════════╝  ║
║                                        ║
║  ╔══════════════════════════════════╗  ║
║  ║      About                       ║  ║
║  ║                                  ║  ║
║  ║  This is a placeholder profile.  ║  ║
║  ║  Authentication will be added    ║  ║
║  ║  in the future.                  ║  ║
║  ╚══════════════════════════════════╝  ║
║                                        ║
╚════════════════════════════════════════╝
```

## Navigation Flow Changes

### Sites → Site Details → Area Details (Unchanged)
Users can still navigate through:
1. Sites screen
2. Click on a site → Site detail screen
3. Click on an area → Area detail screen (with SVG map)

### New: Favorite Access
Users can now:
1. Tap star on any site card to mark as favorite
2. Tap "Favorite" in bottom nav to quickly access favorite site
3. Navigate to site details from favorite screen

### New: Profile Access
Users can now:
1. Tap "You" in bottom nav
2. View profile with placeholder data

## SVG Map Fetching (Internal Change)

**BEFORE:**
- `area.svgMap` was treated as the actual SVG content
- Displayed directly in WebView

**AFTER:**
- `area.svgMap` is treated as a URL
- App fetches the SVG content from the URL
- Fetched content is then displayed in WebView
- Error handling: If fetch fails, map section is not shown

### Code Flow:
```
Area Detail Screen
    ↓
AreaDetailViewModel.loadAreaDetails()
    ↓
Fetch area.svgMap URL → URL(mapUrl).readText()
    ↓
Store in uiState.svgMapContent
    ↓
Display in WebView
```

## Icon Reference

- **Sites**: 🗺️ Place icon (pin/marker)
- **Favorite**: ⭐ Star icon (filled when active, outlined when inactive)
- **Profile**: 👤 Person icon
- **Star on cards**: 
  - ☆ Outlined star = Not favorite
  - ⭐ Filled star = Is favorite

## Color Scheme

All UI elements follow Material Design 3 theming:
- Primary color: Used for favorite stars, stats numbers
- On-surface: Used for text, unfilled stars
- Secondary: Used for subtitle text, location info
- Surface: Used for card backgrounds
- Error: Used for error messages

## Accessibility

- All icons have content descriptions
- Star button has descriptive text: "Add to favorites" / "Remove from favorites"
- Profile stats are organized in clear columns
- Empty states have helpful guidance text
