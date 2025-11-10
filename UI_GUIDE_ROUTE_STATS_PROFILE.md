# UI Changes Visual Guide

This document describes the visual changes made to the TopoClimb Android app UI.

## 1. Route Detail Bottom Sheet - Removal Info

The route detail bottom sheet now displays dynamic removal information at the bottom of the Overview tab.

### Three Display States:

#### State 1: No Removal Planned (Default)
```
┌─────────────────────────────────────────┐
│ ✓  There are no plans to dismantle     │
│    this route at this time.             │
└─────────────────────────────────────────┘
```
- **Icon**: Checkmark (✓) 
- **Color**: Primary container (typically light blue/green)
- **Shown when**: `removing_at` is null or parsing fails

#### State 2: Removal Scheduled (> 7 days)
```
┌─────────────────────────────────────────┐
│ ℹ️  The removal of this route is       │
│    scheduled for 06/09/25.              │
└─────────────────────────────────────────┘
```
- **Icon**: Info (ℹ️)
- **Color**: Tertiary container (typically light purple/orange)
- **Shown when**: `removing_at` date is more than 7 days in the future

#### State 3: Urgent Removal (≤ 7 days)
```
┌─────────────────────────────────────────┐
│ ⚠️  This track is on its last legs!    │
│    It will be dismantled on 06/09/25.   │
└─────────────────────────────────────────┘
```
- **Icon**: Warning (⚠️)
- **Color**: Error container (typically light red/pink)
- **Shown when**: `removing_at` date is 7 days or less in the future

## 2. Profile Screen - Stats Display

### New Stats Card (displayed when user is authenticated)

```
┌───────────────────────────────────────────────────┐
│ Climbing Statistics                               │
│                                                    │
│  📈          🏆           🏆                       │
│  122        6a           3c                        │
│  Total      Trad         Boulder                   │
│  Climbed    Level        Level                     │
│                                                    │
│ ────────────────────────────────────────────────   │
│                                                    │
│ 📊 Routes by Grade                                │
│                                                    │
│     2      2      1                                │
│    ▐▌    ▐▌    ▐▌                                 │
│    ▐▌    ▐▌    ▐▌                                 │
│    ▐▌    ▐▌    ▐▌                                 │
│    4a     5c     6a                                │
└───────────────────────────────────────────────────┘
```

**Components:**
1. **Title**: "Climbing Statistics" in bold
2. **Top Row**: Three metric cards
   - Total Climbed: Shows total number with trending up icon
   - Trad Level: Shows highest trad grade with trophy icon
   - Boulder Level: Shows highest boulder grade with trophy icon
3. **Divider**
4. **Bar Chart Section**:
   - Section header with bar chart icon
   - Custom Compose bar chart
   - Grades sorted alphabetically on x-axis
   - Count displayed above each bar
   - Bars scaled proportionally

### Loading State
```
┌───────────────────────────────────────────────────┐
│                                                    │
│                   ⭕ Loading...                    │
│                                                    │
└───────────────────────────────────────────────────┘
```
Shown while stats are being fetched from the API.

## 3. Profile Screen - Edit Mode

### View Mode (Default)
```
┌───────────────────────────────────────────────────┐
│ Account Information                  ✏️ [Edit]     │
│                                                    │
│ 🎂 Birth Date                                     │
│    1990-05-15                                      │
│                                                    │
│ 👤 Gender                                         │
│    Male                                            │
│                                                    │
│ 📅 Member Since                                   │
│    2024-01-15                                      │
└───────────────────────────────────────────────────┘
```

### Edit Mode (After clicking Edit button)
```
┌───────────────────────────────────────────────────┐
│ Account Information                                │
│                                                    │
│ ┌─────────────────────────────────────────────┐  │
│ │ Name                                         │  │
│ │ John Doe                        [Clear icon]│  │
│ └─────────────────────────────────────────────┘  │
│                                                    │
│ ┌─────────────────────────────────────────────┐  │
│ │ Birth Date (YYYY-MM-DD)                     │  │
│ │ 1990-05-15                      [Clear icon]│  │
│ └─────────────────────────────────────────────┘  │
│                                                    │
│ ┌─────────────────────────────────────────────┐  │
│ │ Gender                          [Dropdown ▼]│  │
│ │ Male                                         │  │
│ └─────────────────────────────────────────────┘  │
│                                                    │
│  [❌ Cancel]              [💾 Save]               │
└───────────────────────────────────────────────────┘
```

**Features:**
- All fields are editable with material outlined text fields
- Gender dropdown with options: Male, Female, Other, Not specified
- Character limits: Name (255 chars), Birth Date (date format)
- Cancel button restores original values
- Save button submits changes to API
- Loading indicator on Save button during submission

### Success State (Snackbar)
```
                    ────────────────────
                    │ Profile updated  │
                    │  successfully!   │
                    ────────────────────
```
Appears at bottom of screen after successful update.

### Error State
```
┌───────────────────────────────────────────────────┐
│ Account Information                                │
│ ... (form fields) ...                              │
│                                                    │
│ ⚠️ Failed to update profile: Invalid date format  │
│                                                    │
│  [❌ Cancel]              [💾 Save]               │
└───────────────────────────────────────────────────┘
```
Error message displayed in red text above action buttons.

## Color Scheme

All UI components use Material3 dynamic color scheme:
- **Primary**: Main brand color (for icons, highlights)
- **Primary Container**: Light version of primary (for info boxes)
- **Error/Error Container**: Red/pink tones (for warnings)
- **Tertiary/Tertiary Container**: Purple/orange tones (for scheduled info)
- **Surface Variant**: Slightly tinted background (for cards)
- **On Surface Variant**: Muted text color (for labels)

## Responsive Design

All components:
- Use `fillMaxWidth()` to adapt to screen size
- Utilize Material3 spacing (4dp, 8dp, 12dp, 16dp, 24dp)
- Include proper padding and spacing for readability
- Support both light and dark themes

## Accessibility

- All icons have content descriptions
- Text follows Material3 typography scale
- Sufficient color contrast for readability
- Touch targets meet minimum size requirements
- Form labels clearly identify fields
