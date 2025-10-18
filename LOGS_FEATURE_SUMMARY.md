# Logs Feature Implementation Summary

## Overview
Implemented a comprehensive logs feature for the route detail bottom sheet. Users can now view all logs for a route, including user information, climbing metadata, and comments.

## Changes Made

### 1. Data Model (`Log.kt`)
- Created `Log` data class with all required fields:
  - `id`, `routeId`, `comments`, `type`, `way`, `grade`
  - `createdAt`, `isVerified`, `userName`, `userPpUrl`
- Created `LogsResponse` wrapper class for API response

### 2. API Service (`TopoClimbApiService.kt`)
- Added `getRouteLogs()` endpoint to fetch logs from `/routes/{route}/logs`

### 3. ViewModel (`RouteDetailViewModel.kt`)
- Extended `RouteDetailUiState` to include:
  - `logs: List<Log>` - List of logs
  - `isLogsLoading: Boolean` - Loading state
  - `logsError: String?` - Error message
- Added `loadRouteLogs()` function to fetch logs when route details are loaded
- Logs are automatically fetched when route details are loaded

### 4. UI Components (`RouteDetailBottomSheet.kt`)
- Completely redesigned `LogsTab` with modern Material Design 3 UI
- **Filter Header**: 
  - Toggle switch to filter logs with/without comments
  - Shows current filter state
  - Sticky header with light background
- **Log Cards**: Each log displays:
  - User avatar (circular, 40dp) with fallback to first letter
  - User name and formatted timestamp
  - Verified badge (green) if `isVerified` is true
  - Three badges for metadata:
    - **Type badge**: Color-coded (Flash=Orange, Redpoint=Blue, Onsight=Green)
    - **Way badge**: Shows climbing style (bouldering, sport, etc.)
    - **Grade badge**: Shows the grade value
  - Comments section (only shown if comments exist)
- **Loading & Error States**:
  - Loading spinner when fetching logs
  - Error message display if fetch fails
  - Empty state message when no logs exist or no logs match filter

## UI Features

### Design Highlights
- **Modern Card Design**: Elevated cards with proper spacing
- **Color-Coded Badges**: Visual distinction for different log types
- **Responsive Layout**: Adapts to different content sizes
- **Loading States**: Clear feedback for all async operations
- **Filter Functionality**: Toggle between all logs and logs with comments only

### Badge Colors
- **Flash**: Orange/Amber (#FFB74D / #E65100)
- **Redpoint**: Blue (#64B5F6 / #0D47A1)
- **Onsight**: Green (#81C784 / #1B5E20)
- **Verified**: Green with checkmark (#4CAF50)
- **Way**: Tertiary container colors
- **Grade**: Primary container colors

## User Experience
1. User opens a route detail sheet
2. Switches to "Logs" tab
3. Logs are automatically loaded and displayed
4. User can toggle "With Comments" filter to see only logs with comments
5. Each log shows comprehensive information in a clean, modern card layout
6. Loading and error states provide clear feedback

## Technical Notes
- Logs are fetched asynchronously when route details load
- Filter state is preserved during tab switches within the same session
- Avatar images use Coil for efficient loading with fallback to initials
- Date formatting uses SimpleDateFormat for consistent display
- All UI follows Material Design 3 guidelines
