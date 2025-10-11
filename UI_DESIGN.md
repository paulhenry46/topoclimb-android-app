# TopoClimb Android App - UI Design

This document describes the user interface design of the TopoClimb Android app.

## App Theme

The app uses **Material Design 3** with:
- Dynamic color support (Android 12+)
- Light and dark theme support
- Material You design principles
- Responsive layout for different screen sizes

## Navigation Structure

```
┌─────────────────────────────────────────┐
│          TopoClimb App                  │
├─────────────────────────────────────────┤
│                                         │
│         [Main Content Area]             │
│                                         │
│                                         │
│                                         │
│                                         │
├─────────────────────────────────────────┤
│  [Sites]    [Routes]     [Areas]        │ ← Bottom Navigation
└─────────────────────────────────────────┘
```

## Screen Designs

### 1. Sites Screen

**Purpose**: Display all climbing sites

**Layout**:
```
┌─────────────────────────────────────────┐
│  ← Sites                                │ ← TopAppBar
├─────────────────────────────────────────┤
│ ┌─────────────────────────────────────┐ │
│ │ 🏔️ Fontainebleau                    │ │ ← Site Card
│ │ World-famous bouldering area in...  │ │
│ │ Lat: 48.4044, Lon: 2.6992           │ │
│ └─────────────────────────────────────┘ │
│                                         │
│ ┌─────────────────────────────────────┐ │
│ │ 🏔️ Ceüse                            │ │
│ │ Legendary sport climbing crag...    │ │
│ │ Lat: 44.5167, Lon: 6.0167           │ │
│ └─────────────────────────────────────┘ │
│                                         │
│ ┌─────────────────────────────────────┐ │
│ │ 🏔️ El Capitan                       │ │
│ │ Iconic big wall in Yosemite...      │ │
│ │ Lat: 37.7341, Lon: -119.6378        │ │
│ └─────────────────────────────────────┘ │
│                                         │
├─────────────────────────────────────────┤
│  [Sites]    [Routes]     [Areas]        │
└─────────────────────────────────────────┘
```

**Interactions**:
- Tap on a site card → Navigate to Routes screen filtered by that site
- Pull to refresh → Reload sites from API
- Scroll → Lazy load more sites

**States**:
- Loading: Shows circular progress indicator
- Error: Shows error message with "Retry" button
- Empty: Shows "No sites found" message
- Success: Shows list of site cards

### 2. Routes Screen

**Purpose**: Display routes with filtering options

**Layout**:
```
┌─────────────────────────────────────────┐
│  ← Routes                        🔍      │ ← TopAppBar with Filter
├─────────────────────────────────────────┤
│ Filters active: 7a sport     [Clear]    │ ← Active filters banner
├─────────────────────────────────────────┤
│ ┌─────────────────────────────────────┐ │
│ │ La Marie-Rose              7c        │ │ ← Route Card
│ │ [boulder] [5m]                       │ │
│ │ Classic Font problem with...         │ │
│ └─────────────────────────────────────┘ │
│                                         │
│ ┌─────────────────────────────────────┐ │
│ │ Biographie                 9a+       │ │
│ │ [sport] [45m]                        │ │
│ │ World's first 9a+, incredibly...    │ │
│ └─────────────────────────────────────┘ │
│                                         │
│ ┌─────────────────────────────────────┐ │
│ │ Action Directe             8c+       │ │
│ │ [sport] [30m]                        │ │
│ │ Historic first 8c+ route...          │ │
│ └─────────────────────────────────────┘ │
│                                         │
├─────────────────────────────────────────┤
│  [Sites]    [Routes]     [Areas]        │
└─────────────────────────────────────────┘
```

**Filter Dialog**:
```
┌─────────────────────────────────────────┐
│  Filter Routes                     ✕    │
├─────────────────────────────────────────┤
│ Grade                                   │
│ [5a] [5b] [5c] [6a] ...                │
│                                         │
│ Type                                    │
│ [sport] [trad] [boulder] [multi-pitch] │
│                                         │
│                            [Done]       │
└─────────────────────────────────────────┘
```

**Interactions**:
- Tap filter icon → Show filter dialog
- Select grade chip → Filter routes by grade
- Select type chip → Filter routes by type
- Tap "Clear" → Remove all filters
- Tap route card → View route details (future feature)

**States**:
- Loading: Shows circular progress indicator
- Error: Shows error message with "Retry" button
- Empty: Shows "No routes found" message
- Filtered: Shows active filter banner
- Success: Shows list of route cards

### 3. Areas Screen

**Purpose**: Display climbing areas

**Layout**:
```
┌─────────────────────────────────────────┐
│  ← Areas                                │ ← TopAppBar
├─────────────────────────────────────────┤
│ ┌─────────────────────────────────────┐ │
│ │ 🗺️ Île-de-France                     │ │ ← Area Card
│ │ Region around Paris with many...    │ │
│ │ Lat: 48.8566, Lon: 2.3522           │ │
│ └─────────────────────────────────────┘ │
│                                         │
│ ┌─────────────────────────────────────┐ │
│ │ 🗺️ Provence-Alpes-Côte d'Azur       │ │
│ │ Southern Alps region famous for...  │ │
│ │ Lat: 43.9352, Lon: 6.0679           │ │
│ └─────────────────────────────────────┘ │
│                                         │
│ ┌─────────────────────────────────────┐ │
│ │ 🗺️ Yosemite National Park           │ │
│ │ World-renowned climbing area...     │ │
│ │ Lat: 37.8651, Lon: -119.5383        │ │
│ └─────────────────────────────────────┘ │
│                                         │
├─────────────────────────────────────────┤
│  [Sites]    [Routes]     [Areas]        │
└─────────────────────────────────────────┘
```

**Interactions**:
- Tap area card → Could navigate to sites in that area (future feature)
- Pull to refresh → Reload areas from API
- Scroll → Lazy load more areas

**States**:
- Loading: Shows circular progress indicator
- Error: Shows error message with "Retry" button
- Empty: Shows "No areas found" message
- Success: Shows list of area cards

## UI Components

### Card Component
```
┌─────────────────────────────────────┐
│ Title                    Badge       │ ← Large, bold text
│ [chip] [chip]                        │ ← Optional chips
│ Description text limited to 2 lines  │ ← Body text
│ Metadata (location, etc.)            │ ← Small, secondary text
└─────────────────────────────────────┘
```

**Properties**:
- Elevation: 2dp
- Corner radius: 12dp (Material3 default)
- Padding: 16dp
- Clickable with ripple effect

### Loading State
```
┌─────────────────────────────────────────┐
│                                         │
│                                         │
│              ⭕ Loading...              │ ← Circular progress
│                                         │
│                                         │
└─────────────────────────────────────────┘
```

### Error State
```
┌─────────────────────────────────────────┐
│                                         │
│              ⚠️                         │
│     Error: Unable to load data          │
│                                         │
│            [Retry]                      │ ← Retry button
│                                         │
└─────────────────────────────────────────┘
```

### Empty State
```
┌─────────────────────────────────────────┐
│                                         │
│              📭                         │
│         No items found                  │
│                                         │
└─────────────────────────────────────────┘
```

## Color Scheme

### Light Theme
- **Primary**: Purple 40 (#6650a4)
- **Secondary**: Purple Grey 40 (#625b71)
- **Tertiary**: Pink 40 (#7D5260)
- **Background**: White
- **Surface**: Light grey
- **Error**: Red

### Dark Theme
- **Primary**: Purple 80 (#D0BCFF)
- **Secondary**: Purple Grey 80 (#CCC2DC)
- **Tertiary**: Pink 80 (#EFB8C8)
- **Background**: Dark grey
- **Surface**: Darker grey
- **Error**: Light red

### Dynamic Colors (Android 12+)
The app adapts to the user's wallpaper colors automatically.

## Typography

- **Title Large**: 22sp, Normal weight
- **Title Medium**: 16sp, Medium weight (used for card titles)
- **Body Large**: 16sp, Normal weight
- **Body Medium**: 14sp, Normal weight (used for descriptions)
- **Body Small**: 12sp, Normal weight (used for metadata)
- **Label Medium**: 12sp, Medium weight (used for chips)

## Icons

Bottom Navigation Icons:
- **Sites**: `Icons.Default.Place` (location pin)
- **Routes**: `Icons.Default.List` (list)
- **Areas**: `Icons.Default.Home` (home/area)
- **Filter**: `Icons.Default.FilterList` (filter funnel)

## Spacing

- **Screen padding**: 16dp
- **Card spacing**: 8dp vertical
- **Internal card padding**: 16dp
- **Chip spacing**: 8dp horizontal
- **Section spacing**: 16dp

## Animations

- **Screen transitions**: Material motion (slide + fade)
- **List items**: Fade in on appear
- **Click ripple**: Material ripple effect
- **Loading**: Rotating circular progress

## Accessibility

- **Minimum touch target**: 48dp × 48dp
- **Color contrast**: WCAG AA compliant
- **Screen reader support**: All interactive elements have content descriptions
- **Font scaling**: Supports system font size settings
- **Focus indicators**: Clear focus state for keyboard navigation

## Responsive Design

### Phone (Portrait)
- Single column layout
- Full-width cards
- Bottom navigation

### Phone (Landscape)
- Single column with adjusted padding
- Bottom navigation

### Tablet
- Potential for two-column layout (future enhancement)
- Navigation rail instead of bottom nav (future enhancement)

## Loading and Error States Summary

### Loading
- Centered circular progress indicator
- No content shown
- Navigation remains accessible

### Error
- Error icon and message
- Retry button
- Navigation remains accessible
- Red error color for text

### Empty
- Appropriate empty state icon
- "No items found" message
- Navigation remains accessible

### Success
- List of items in cards
- Smooth scrolling
- Pull-to-refresh enabled

## Future UI Enhancements

1. **Route Details Screen**
   - Full route information
   - Photos gallery
   - Comments section
   - Difficulty chart

2. **Map View**
   - Interactive map showing sites
   - Cluster markers
   - Site details on tap

3. **Search**
   - Search bar in top app bar
   - Filter by name
   - Recent searches

4. **Images**
   - Site and route photos using Coil
   - Image carousels
   - Full-screen image view

5. **Favorites**
   - Save favorite routes and sites
   - Quick access from home
   - Sync across devices (with auth)

This UI design provides a clean, modern, and user-friendly experience for browsing climbing sites, routes, and areas! 🧗‍♂️
