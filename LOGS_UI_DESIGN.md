# Logs Tab UI Design Description

## Overview
The Logs tab in the route detail bottom sheet displays climb logs in a modern, card-based layout with Material Design 3 styling.

## Layout Structure

```
┌─────────────────────────────────────────────────────────┐
│                     LOGS TAB                             │
├─────────────────────────────────────────────────────────┤
│ ┌─────────────────────────────────────────────────────┐ │
│ │ Filter Header (Light Gray Background)              │ │
│ │ ┌──────────────────┐   ┌─────────────────────────┐ │ │
│ │ │ Filter Logs      │   │ With Comments  [Switch] │ │ │
│ │ │ Showing all logs │   └─────────────────────────┘ │ │
│ │ └──────────────────┘                                │ │
│ └─────────────────────────────────────────────────────┘ │
│                                                          │
│ ┌─────────────────────────────────────────────────────┐ │
│ │ Log Card 1 (Elevated Card)                         │ │
│ │ ┌──────────────────────────────────────────────┐   │ │
│ │ │ [Avatar] Paulhenry        [✓ Verified]       │   │ │
│ │ │          Oct 03, 2025 at 10:18                │   │ │
│ │ └──────────────────────────────────────────────┘   │ │
│ │                                                     │ │
│ │ ┌────────┐ ┌───────────┐ ┌──────────┐             │ │
│ │ │ Flash  │ │Bouldering │ │Grade: 600│             │ │
│ │ │(Orange)│ │ (Purple)  │ │  (Blue)  │             │ │
│ │ └────────┘ └───────────┘ └──────────┘             │ │
│ │                                                     │ │
│ │ [Comment section - if comments exist]              │ │
│ └─────────────────────────────────────────────────────┘ │
│                                                          │
│ ┌─────────────────────────────────────────────────────┐ │
│ │ Log Card 2                                          │ │
│ │ ...                                                 │ │
│ └─────────────────────────────────────────────────────┘ │
│                                                          │
│ ┌─────────────────────────────────────────────────────┐ │
│ │ Log Card 3                                          │ │
│ │ ...                                                 │ │
│ └─────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────┘
```

## Component Details

### 1. Filter Header
- **Background**: Light gray surface variant (semi-transparent)
- **Layout**: Two-column row
  - Left: Title "Filter Logs" with subtitle showing current filter state
  - Right: "With Comments" label with toggle switch
- **Behavior**: Toggle filters between all logs and logs with comments only

### 2. Log Card
Each log is displayed in an elevated Material Design card with:

#### User Section
- **Avatar**: 
  - 40dp circular image
  - Loads user profile picture from `user_pp_url`
  - Fallback: First letter of username in colored circle
  - Loading state: Small spinner
- **User Info**:
  - Username in bold, semi-bold typography
  - Formatted timestamp (e.g., "Oct 03, 2025 at 10:18")
- **Verified Badge** (conditional):
  - Green background with checkmark icon
  - Only shown if `is_verified` is true
  - Text: "Verified" in green

#### Badges Row
Three colored badges showing climb metadata:

1. **Type Badge** (Color-coded by type):
   - Flash: Orange background (#FFB74D) with dark orange text (#E65100)
   - Redpoint: Blue background (#64B5F6) with dark blue text (#0D47A1)
   - Onsight: Green background (#81C784) with dark green text (#1B5E20)
   - Other types: Secondary container color
   - Text: Capitalized type name

2. **Way Badge**:
   - Tertiary container background
   - Text: Capitalized climbing way (e.g., "Bouldering", "Sport")

3. **Grade Badge**:
   - Primary container background
   - Text: "Grade: {numeric_value}" (e.g., "Grade: 600")

#### Comments Section (conditional)
- Only shown if `comments` field is not empty
- Light gray card within the main card
- "Comment" label in semi-bold
- Comment text in body medium style

## States

### Loading State
- Centered circular progress indicator
- Shown while fetching logs from API

### Error State
- Centered column layout
- Error title in error color
- Error message in subdued color
- Text: "Failed to load logs" with error details

### Empty State
- Centered message
- Text: "No logs yet" (when no filter active)
- Text: "No logs with comments" (when filter active)

### Content State
- Scrollable list of log cards
- 12dp spacing between cards
- 16dp padding around the list

## Color Scheme

### Badges
- **Flash**: Amber/Orange palette
- **Redpoint**: Blue palette
- **Onsight**: Green palette
- **Verified**: Material Green (#4CAF50)
- **Way**: Tertiary container colors from theme
- **Grade**: Primary container colors from theme

### Backgrounds
- **Filter Header**: Surface variant at 50% opacity
- **Log Cards**: Surface color with 2dp elevation
- **Comments**: Surface variant at 50% opacity
- **Avatar Fallback**: Primary container

## Typography
- **Filter Title**: Title Small, Semi-bold
- **Filter Subtitle**: Body Small
- **Username**: Title Medium, Semi-bold
- **Timestamp**: Body Small
- **Badge Labels**: Label Medium, Medium weight
- **Comment Label**: Label Medium, Semi-bold
- **Comment Text**: Body Medium

## Spacing
- Card padding: 16dp
- Card spacing: 12dp
- Badge spacing: 8dp
- Section spacing: 12dp
- Avatar size: 40dp
- Icon size: 16-24dp

## Behavior

### Filter Toggle
- Toggle switch in header
- Updates filtered list in real-time
- Preserves state during tab switches within same session
- Shows count update in subtitle

### Scrolling
- Vertical scroll for log list
- Fixed filter header (not sticky in current implementation)
- Smooth scroll performance with lazy rendering considerations

### Image Loading
- Asynchronous avatar loading with Coil
- Loading state with small spinner
- Error fallback to initials
- Cached for performance

## Accessibility
- All badges have proper contrast ratios
- Icon content descriptions provided
- Readable font sizes throughout
- Touch targets meet minimum size requirements
