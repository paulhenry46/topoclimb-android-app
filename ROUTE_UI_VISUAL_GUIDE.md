# Route Display Component - Visual Guide

## New Route Card Design

This document provides a visual representation of the new route display component.

## Layout Structure

```
╔═════════════════════════════════════════════════════════════════════╗
║                          Routes Screen                               ║
╠═════════════════════════════════════════════════════════════════════╣
║                                                                      ║
║  ╭─────────────────────────────────────────────────────────────╮   ║
║  │  ●●●●●●   ╔═══════╗   Route Name 1                          │   ║
║  │  ●●●●●●   ║  5a   ║   Sector A                              │   ║
║  │  ●●●●●●   ╚═══════╝                                          │   ║
║  ╰─────────────────────────────────────────────────────────────╯   ║
║                                                                      ║
║  ╭─────────────────────────────────────────────────────────────╮   ║
║  │  ●●●●●●   ╔═══════╗   Classic Boulder Problem               │   ║
║  │  ●●●●●●   ║  7a+  ║   Line 3B                               │   ║
║  │  ●●●●●●   ╚═══════╝                                          │   ║
║  ╰─────────────────────────────────────────────────────────────╯   ║
║                                                                      ║
║  ╭─────────────────────────────────────────────────────────────╮   ║
║  │  ●●●●●●   ╔═══════╗   La Marie-Rose                         │   ║
║  │  ●●●●●●   ║  7c   ║   Line 12A                              │   ║
║  │  ●●●●●●   ╚═══════╝                                          │   ║
║  ╰─────────────────────────────────────────────────────────────╯   ║
║                                                                      ║
║  ╭─────────────────────────────────────────────────────────────╮   ║
║  │  ●●●●●●   ╔═══════╗   Epic Sport Route                      │   ║
║  │  ●●●●●●   ║  6b+  ║   Line 5C                               │   ║
║  │  ●●●●●●   ╚═══════╝                                          │   ║
║  ╰─────────────────────────────────────────────────────────────╯   ║
║                                                                      ║
╚═════════════════════════════════════════════════════════════════════╝
```

## Component Breakdown

### Single Route Card (Detailed View)

```
╭────────────────────────────────────────────────────────────────╮
│                                                                 │
│   ╭──────╮   ┌─────────┐   ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓   │
│   │      │   │   7a+   │   ┃  Route Name                 ┃   │
│   │ IMG  │   │         │   ┃  Line 3B (or Sector A)      ┃   │
│   │      │   └─────────┘   ┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛   │
│   ╰──────╯    (colored)     (flexible width)                  │
│    60x60      background                                       │
│   (circle)                                                     │
│                                                                 │
╰────────────────────────────────────────────────────────────────╯
```

### Spacing and Measurements

```
Row (horizontal arrangement):
│←─ 12dp ─→│  Image  │←─ 12dp ─→│  Grade  │←─ 12dp ─→│  Info Column  │
            (60dp)                (auto)                (flex: 1f)
```

### Colors

The grade badge uses the `color` field from the API (hex format):
- Example colors: `#FF5722` (red-orange), `#2196F3` (blue), `#4CAF50` (green)
- Fallback color: `#6200EE` (Material Purple) if color is missing or invalid
- Text on badge is always white for contrast

### Typography

```
Route Name:
  - Style: titleMedium
  - Weight: SemiBold
  - Color: Primary text color (theme dependent)

Local ID (Line/Sector):
  - Style: bodySmall
  - Size: 12sp
  - Color: onSurfaceVariant (muted gray)

Grade Badge:
  - Style: titleMedium
  - Weight: Bold
  - Size: 16sp
  - Color: White
```

## Visual Examples with Different Data

### Example 1: Route with All Data

```
╭────────────────────────────────────────────────────────────╮
│  [Photo]  │ 7c │  La Marie-Rose                            │
│   (60px)  │    │  Line 3B                                  │
╰────────────────────────────────────────────────────────────╯
```

### Example 2: Route in Single-Line Sector

```
╭────────────────────────────────────────────────────────────╮
│  [Photo]  │ 5a │  Easy Route                               │
│   (60px)  │    │  Sector A                                 │
╰────────────────────────────────────────────────────────────╯
```
Note: Shows "Sector A" (sectorLocalId) because lineCount = 1, indicating this sector has only one line.

### Example 3: Route with No Thumbnail

```
╭────────────────────────────────────────────────────────────╮
│  [Gray]   │ 6b+│  Modern Problem                           │
│   (60px)  │    │  Line 12A                                 │
╰────────────────────────────────────────────────────────────╯
```
Note: Coil library shows placeholder when thumbnail is null

### Example 4: Route with No Grade

```
╭────────────────────────────────────────────────────────────╮
│  [Photo]        │  Project Route                           │
│   (60px)        │  Line 8C                                 │
╰────────────────────────────────────────────────────────────╯
```
Note: Grade badge is hidden when grade is null

## Color Scheme Examples

### Material Design Colors
Common route difficulty colors that might be used:

```
Easy (3-4):    #4CAF50  ░░░░░░░  Green
               #8BC34A  ░░░░░░░  Light Green

Medium (5-6):  #FFC107  ░░░░░░░  Amber
               #FF9800  ░░░░░░░  Orange

Hard (7-8):    #FF5722  ░░░░░░░  Deep Orange
               #F44336  ░░░░░░░  Red

Expert (9+):   #E91E63  ░░░░░░░  Pink
               #9C27B0  ░░░░░░░  Purple
```

## Responsive Behavior

- **Image**: Fixed at 60x60 dp, circular crop
- **Grade Badge**: Auto-width based on text content (e.g., "5a" vs "8b+")
- **Info Column**: Flexible, takes remaining space
- **Spacing**: 12dp gaps between all elements
- **Card Padding**: 12dp all around

## Dark Mode Support

The component automatically adapts to dark mode:
- Card background: Material surface color
- Text colors: Adjust based on theme
- Grade badge: Uses specified color (works in both light/dark)
- Image: No adjustments needed

## Accessibility

- **Semantic content**: Each element has clear purpose
- **Touch target**: Entire card is clickable (48dp minimum height)
- **Contrast**: White text on colored badges ensures readability
- **Content description**: Images have descriptive alt text
