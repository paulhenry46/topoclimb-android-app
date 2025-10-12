# Interactive SVG Map Feature - Visual Guide

## Feature Overview

The area detail screen now displays an interactive SVG map where users can tap on sectors to filter routes.

## User Flow Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                   Area Detail Screen                         │
├─────────────────────────────────────────────────────────────┤
│  ← Back          "Area Name"                                │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  ┌────────────────────────────────────────────────────┐    │
│  │ Area Information                                    │    │
│  │ Description: ...                                    │    │
│  │ Location: lat, lon                                  │    │
│  └────────────────────────────────────────────────────┘    │
│                                                              │
│  Topo Map                                                   │
│  Tap on a sector to view its routes                        │
│  ┌────────────────────────────────────────────────────┐    │
│  │                                                      │    │
│  │    [SVG Map with Interactive Paths]                 │    │
│  │                                                      │    │
│  │    ╱╲     ╱╲      ╱╲    ← Climbing paths           │    │
│  │   ╱  ╲   ╱  ╲    ╱  ╲     (sectors)                │    │
│  │  │    │ │    │  │    │                              │    │
│  │  │    │ │    │  │    │                              │    │
│  │                                                      │    │
│  └────────────────────────────────────────────────────┘    │
│                                                              │
│  Routes (25)                                                │
│  ┌────────────────────────────────────────────────────┐    │
│  │ Route Name 1              Grade: 6a                 │    │
│  │ Type: Sport    Height: 25m                          │    │
│  └────────────────────────────────────────────────────┘    │
│  ┌────────────────────────────────────────────────────┐    │
│  │ Route Name 2              Grade: 5c                 │    │
│  └────────────────────────────────────────────────────┘    │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

## Interaction Flow

### Initial State
```
┌─────────────────────────────┐
│  Topo Map                   │
│  Tap on a sector to view    │
│  its routes                 │
│  ┌──────────────────────┐   │
│  │                      │   │
│  │   All paths shown    │   │
│  │   in BLACK color     │   │
│  │                      │   │
│  └──────────────────────┘   │
└─────────────────────────────┘

  Routes (25)  ← All routes shown
```

### After Tapping Sector 5
```
┌─────────────────────────────┐
│  Topo Map                   │
│  Sector 5 selected - Tap to │
│  deselect                   │
│  ┌──────────────────────┐   │
│  │                      │   │
│  │   Selected path in   │   │
│  │   RED (thicker)      │   │
│  │   Others in BLACK    │   │
│  └──────────────────────┘   │
└─────────────────────────────┘

  Routes in Sector 5 (8)  ← Filtered routes
```

### After Tapping Same Sector Again
```
┌─────────────────────────────┐
│  Topo Map                   │
│  Tap on a sector to view    │
│  its routes                 │
│  ┌──────────────────────┐   │
│  │                      │   │
│  │   All paths shown    │   │
│  │   in BLACK color     │   │
│  │                      │   │
│  └──────────────────────┘   │
└─────────────────────────────┘

  Routes (25)  ← All routes shown again
```

## Visual States

### Path Rendering States

| State | Color | Stroke Width | Behavior |
|-------|-------|--------------|----------|
| Normal | Black | 2px | Default appearance |
| Selected | Red | 3px | Highlighted when tapped |

### Map Features

- **Scaling**: SVG automatically scales to fit the available space
- **Centering**: Map is centered within the card
- **Hit Detection**: Tap detection uses path bounds with 10-unit tolerance
- **Coordinate Transform**: Tap coordinates are properly transformed from screen to SVG space

## Data Hierarchy

```
Area
  └── Sectors (extracted from SVG path IDs)
       └── Lines
            └── Routes
```

When a sector is selected:
1. Fetch all lines in that sector
2. For each line, fetch its routes
3. Display only those routes in the list

## SVG File Requirements

For the interactive features to work, SVG files must have:

1. **Root SVG element with viewBox**:
   ```xml
   <svg viewBox="0 0 1000 800">
   ```

2. **Path elements with sector IDs**:
   ```xml
   <path id="sector_5" d="M 100 100 L 200 200 ..." />
   <path id="sector-12" d="M 300 100 L 400 200 ..." />
   <path id="SECTOR_3" d="M 500 100 L 600 200 ..." />
   ```

   Supported formats:
   - `sector_123` (with underscore)
   - `sector-123` (with dash)
   - Case-insensitive

## Technical Implementation

### Components

1. **SvgParser** - Parses SVG XML and extracts path data
2. **SvgMapView** - Compose Canvas component that renders paths
3. **AreaDetailViewModel** - Manages state and sector selection logic
4. **AreaDetailScreen** - UI that displays the map and routes

### Key Features

- ✅ Pure Compose implementation (no WebView)
- ✅ Interactive path selection
- ✅ Real-time route filtering
- ✅ Visual feedback
- ✅ Proper coordinate transformation
- ✅ Responsive scaling

## Example User Journey

1. **Navigate to Area**: User clicks on an area from the site detail screen
2. **View Map**: The area's SVG map loads and displays
3. **Select Sector**: User taps on a climbing sector (path) on the map
   - Path turns red and becomes thicker
   - Message appears: "Sector 5 selected - Tap to deselect"
4. **View Filtered Routes**: Route list updates to show only routes in sector 5
   - Title shows: "Routes in Sector 5 (8)"
5. **Deselect**: User taps the same sector again
   - Path returns to black
   - All routes are shown again

## Benefits Over WebView

| Aspect | WebView | Compose Component |
|--------|---------|-------------------|
| Performance | Heavy, slower | Lightweight, fast |
| Interaction | Limited | Native gestures |
| Customization | CSS/JS needed | Direct Kotlin code |
| Integration | Bridge required | Seamless |
| Debugging | Complex | Standard tools |
| Maintenance | HTML/CSS/JS | Pure Kotlin |
