# Site UI Visual Guide

## Before vs After

### BEFORE - Old Site Card
```
┌─────────────────────────────────────────────────┐
│ ┌─────────────────────────────────────────────┐ │
│ │  Fontainebleau                              │ │
│ │                                             │ │
│ │  Famous bouldering area in France          │ │
│ │                                             │ │
│ │  Lat: 48.4044, Lon: 2.6992                 │ │
│ └─────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────┘
```

### AFTER - New Site Card with Banner and Logo
```
┌─────────────────────────────────────────────────┐
│ ╔═════════════════════════════════════════════╗ │
│ ║  [Banner Image: Forest/rocks background]   ║ │
│ ║  ┌─────────┐  [Semi-transparent overlay]   ║ │
│ ║  │  Logo   │                                ║ │
│ ║  │  Image  │                                ║ │
│ ║  └─────────┘                                ║ │
│ ║                                             ║ │
│ ║                                             ║ │
│ ║  Fontainebleau                              ║ │
│ ║  Famous bouldering area in France          ║ │
│ ╚═════════════════════════════════════════════╝ │
└─────────────────────────────────────────────────┘
      (Card height: 200dp, clickable)
```

## New Site Detail Screen

```
┌─────────────────────────────────────────────────┐
│ ← Fontainebleau                                 │ ← TopAppBar with back
├─────────────────────────────────────────────────┤
│                                                 │
│ ╔═════════════════════════════════════════════╗ │
│ ║                                             ║ │
│ ║     [Full width banner image - 200dp]      ║ │
│ ║                                             ║ │
│ ╠═════════════════════════════════════════════╣ │
│ ║  Fontainebleau                              ║ │
│ ║  World-famous bouldering area in France.   ║ │
│ ║  Over 30,000 problems spread across        ║ │
│ ║  multiple sectors in the forest.           ║ │
│ ╚═════════════════════════════════════════════╝ │
│                                                 │
│  Areas                                          │ ← Section header
│ ┌───────────────────────────────────────────┐   │
│ │  Cuvier                                   │   │
│ │  Classic sector with famous problems      │   │
│ │  Location: 48.4044, 2.6992               │   │
│ └───────────────────────────────────────────┘   │
│                                                 │
│ ┌───────────────────────────────────────────┐   │
│ │  Bas Cuvier                               │   │
│ │  Beginner-friendly sector                 │   │
│ │  Location: 48.4055, 2.6988               │   │
│ └───────────────────────────────────────────┘   │
│                                                 │
│  Contests                                       │ ← Section header
│ ┌───────────────────────────────────────────┐   │
│ │  Summer Boulder Festival                  │   │
│ │  Annual competition in the forest         │   │
│ │  Start: 2025-07-01 • End: 2025-07-03     │   │
│ └───────────────────────────────────────────┘   │
│                                                 │
└─────────────────────────────────────────────────┘
```

## Navigation Flow

```
Sites Screen                Site Detail Screen
┌──────────────┐           ┌──────────────────┐
│              │           │  ← Back          │
│  Site Cards  │  Click    │                  │
│  [with       │ ────────> │  Site Info       │
│   banner &   │           │  + Banner        │
│   logo]      │           │                  │
│              │           │  Areas List      │
│              │           │  Contests List   │
└──────────────┘           └──────────────────┘
```

## Card Component Details

### SiteItem Card
- **Total Height**: 200dp
- **Background**: Banner image (site.banner) with ContentScale.Crop
- **Overlay**: Semi-transparent surface (alpha 0.6) for text readability
- **Logo**: 
  - Size: 60dp circle
  - Position: Top of card
  - Image: site.profilePicture
  - Shape: CircleShape
- **Text**:
  - Title: HeadlineSmall style
  - Description: BodyMedium, max 2 lines
  - Color: onSurface (visible over overlay)

### Area Card
- Simple card with:
  - Area name (TitleMedium)
  - Description (BodyMedium)
  - Coordinates if available (BodySmall, secondary color)

### Contest Card
- Simple card with:
  - Contest name (TitleMedium)
  - Description (BodyMedium)
  - Date range (BodySmall, secondary color)

## Color Scheme

The UI uses Material3 theme colors:
- **Card background**: surface color
- **Text on cards**: onSurface color
- **Overlay**: surface with 60% opacity
- **Secondary info**: secondary color
- **Error states**: error color

## Image Loading States

```
No Image Available:
┌─────────────┐
│             │
│   [Empty]   │
│             │
└─────────────┘

Loading:
┌─────────────┐
│             │
│ [Shimmer]   │
│             │
└─────────────┘

Loaded:
┌─────────────┐
│   [Image]   │
│   Content   │
│             │
└─────────────┘
```

## Responsive Behavior

- Cards are full-width (fillMaxWidth)
- Spacing between cards: 8dp
- Padding around list: 16dp
- Banner images scale to fill width while maintaining aspect ratio (ContentScale.Crop)
- Text truncates with ellipsis after max lines

## Accessibility

- All images have contentDescription
- Cards are clickable with proper touch targets
- Text has sufficient contrast over images (thanks to overlay)
- Back button clearly labeled
- Section headers provide structure

## Empty States

When no areas or contests are available:
```
┌─────────────────────────────────────────────┐
│                                             │
│    No areas or contests available           │
│         for this site.                      │
│                                             │
└─────────────────────────────────────────────┘
```
