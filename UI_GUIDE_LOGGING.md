# Route Logging Feature - UI/UX Guide

## User Flow

### 1. Viewing Logged Routes
**Location: Route Lists (AreaDetailScreen)**
```
┌─────────────────────────────────────┐
│  [Thumbnail] [Grade]  Route Name    │
│                       Line n°1    ✓ │ ← Filled checkmark = Logged
└─────────────────────────────────────┘
```

### 2. Route Detail - Overview Tab
**Location: RouteDetailBottomSheet**
```
┌─────────────────────────────────────┐
│  Route Photo                        │
│  Focus □                            │
├─────────────────────────────────────┤
│  Route Name               ☆  ✓     │ ← Click to log
│  Line n°1                           │
│                                     │
│  ┌─────────────────────────────┐   │
│  │ ★ Grade: 6a                 │   │
│  │ ℹ Height: 15m              │   │
│  │ 👤 Opener: John Doe        │   │
│  └─────────────────────────────┘   │
├─────────────────────────────────────┤
│  Overview  |  Logs                  │
└─────────────────────────────────────┘
```

### 3. Logs Tab - Initial View
**Location: RouteDetailBottomSheet > Logs Tab**
```
┌─────────────────────────────────────┐
│  Filter Logs                        │
│  Showing all logs                   │
│                    With Comments □  │
│                                     │
│  [      Add Log      ]              │
├─────────────────────────────────────┤
│  Pull down to refresh ↓             │
│                                     │
│  ┌─────────────────────────────┐   │
│  │ 👤 Jane Doe                 │   │
│  │    Mar 15, 2024 at 14:30    │   │
│  │                             │   │
│  │ [Flash] [Lead] [Grade: 6b↑] │   │
│  │                             │   │
│  │ 💬 "Great route!"           │   │
│  └─────────────────────────────┘   │
│                                     │
│  ┌─────────────────────────────┐   │
│  │ 👤 John Doe                 │   │
│  │    Mar 14, 2024 at 10:15    │   │
│  │                             │   │
│  │ [Work] [Top-rope] [Grade: 6a]│  │
│  └─────────────────────────────┘   │
├─────────────────────────────────────┤
│  Overview  |  Logs                  │
└─────────────────────────────────────┘
```

### 4. Create Log Dialog
**Triggered by: Click ✓ in Overview OR "Add Log" button**
```
┌─────────────────────────────────────┐
│  Log Route                          │
│                                     │
│  Grade: [6a____________]            │
│                                     │
│  Type: [Work ▼]                     │
│  └─ work, flash, view               │
│                                     │
│  Way: [Bouldering ▼]                │
│  └─ top-rope, lead, bouldering      │
│                                     │
│  Comment (optional)                 │
│  ┌─────────────────────────────┐   │
│  │ Amazing route!              │   │
│  │                             │   │
│  │                             │   │
│  └─────────────────────────────┘   │
│  0/1000                             │
│                                     │
│  Video URL (optional)               │
│  [https://...____________]          │
│  0/255                              │
│                                     │
│  [Cancel]      [Create]             │
└─────────────────────────────────────┘
```

### 5. After Creating Log
**Action: Automatically switches to Logs tab and refreshes**
```
┌─────────────────────────────────────┐
│  Filter Logs                        │
│  Showing all logs                   │
│                    With Comments □  │
│                                     │
│  [      Add Log      ]              │
├─────────────────────────────────────┤
│  🔄 Refreshing...                   │
│                                     │
│  ┌─────────────────────────────┐   │
│  │ 👤 You                  NEW │   │ ← Your new log
│  │    Just now                 │   │
│  │                             │   │
│  │ [Flash] [Bouldering] [6a]   │   │
│  │                             │   │
│  │ 💬 "Amazing route!"         │   │
│  └─────────────────────────────┘   │
│                                     │
│  [Previous logs...]                 │
├─────────────────────────────────────┤
│  Overview  |  Logs                  │
└─────────────────────────────────────┘
```

## Key UI Elements

### Checkmark States
- **Empty ☐**: Route not logged
- **Filled ✓**: Route logged (at least once)

### Log Badges
- **Type Badge**: Work (gray), Flash (orange), View (blue)
- **Way Badge**: Only shown if not bouldering
- **Grade Badge**: Shows up/down arrow if different from route grade
  - ↑ = Logged harder than route grade
  - ↓ = Logged easier than route grade
  - ✓ = Same as route grade

### Pull-to-Refresh
- Standard Android gesture
- Shows loading spinner
- Refreshes all logs for the route
- Updates logged state

### Form Validation
- Grade must be between 300-950 points
- Comment limited to 1000 characters
- Video URL limited to 255 characters
- Type and Way are required (dropdown selection)

### Error States
```
┌─────────────────────────────────────┐
│  Log Route                          │
│                                     │
│  ┌─────────────────────────────┐   │
│  │ ⚠ Not authenticated.        │   │
│  │   Please log in.            │   │
│  └─────────────────────────────┘   │
│                                     │
│  [Fields...]                        │
│                                     │
│  [Cancel]      [Create]             │
└─────────────────────────────────────┘
```

## Loading States

### Creating Log
```
[   🔄 Loading...   ]  ← Disabled button with spinner
```

### Refreshing Logs
```
↓ Pull to refresh
...releases...
🔄 Refreshing...      ← Shows in pull-to-refresh area
```

### Initial Load
```
        🔄           ← Center spinner while loading
   Loading logs...
```

## Navigation Flow
```
Route List
    │
    ├─ Click Route
    │     │
    │     └─ RouteDetailBottomSheet (Overview)
    │           │
    │           ├─ Click ✓ → Opens CreateLogDialog
    │           │              │
    │           │              └─ Submit → Logs Tab (refreshed)
    │           │
    │           └─ Switch to Logs Tab
    │                 │
    │                 ├─ Pull to refresh → Reload logs
    │                 │
    │                 └─ Click "Add Log" → CreateLogDialog
    │                                        │
    │                                        └─ Submit → Logs Tab (refreshed)
    │
    └─ ✓ indicates logged route
```

## Accessibility
- All buttons have content descriptions
- Loading states announced
- Error messages clearly visible
- Standard Material Design components for familiarity

## Responsive Design
- Dialog adapts to screen size
- Scrollable content for long log lists
- Pull-to-refresh works on all screen sizes
- Form fields stack vertically for easy input
