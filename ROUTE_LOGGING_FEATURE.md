# Route Logging Feature Implementation

## Overview
This document describes the implementation of the route logging feature, which allows authenticated users to log the routes they climb and view their climbing history.

## Features Implemented

### 1. Data Models (Log.kt)
- **CreateLogRequest**: Request body for creating a new log with the following fields:
  - `grade` (Int, 300-950): Difficulty grade value
  - `type` (String): Type of ascent ("work", "flash", "view")
  - `way` (String): Climbing style ("top-rope", "lead", "bouldering")
  - `comment` (String?, optional): Comments about the climb (max 1000 chars)
  - `video_url` (String?, optional): URL to a video of the climb (max 255 chars)
  
- **CreateLogResponse**: Response containing the created log
- **UserLogsResponse**: Response from /user/logs endpoint containing route IDs the user has logged

### 2. API Endpoints (TopoClimbApiService.kt)
- **POST /routes/{id}/logs**: Create a new log for a route
  - Requires authentication via `Authorization: Bearer {token}` header
  - Accepts CreateLogRequest body
  
- **GET /user/logs**: Retrieve all route IDs that the user has logged
  - Requires authentication via `Authorization: Bearer {token}` header
  - Returns list of route IDs

### 3. ViewModel (RouteDetailViewModel.kt)
Extended with the following functionality:
- **Shared State Management**: Uses companion object to maintain logged route IDs across all instances
- **createLog()**: Creates a new log entry for a route
- **refreshLogs()**: Refreshes logs with pull-to-refresh support
- **loadUserLoggedRoutes()**: Loads all routes the user has logged
- **updateRouteLoggedState()**: Updates whether the current route is logged
- Changed to extend `AndroidViewModel` to access BackendConfigRepository

### 4. UI Components

#### RouteDetailBottomSheet.kt
- **Overview Tab**: 
  - Check icon shows filled state when route is logged
  - Clicking check icon navigates to Log tab to create a new log
  
- **Logs Tab**:
  - "Add Log" button to create new logs
  - Pull-to-refresh functionality to reload logs
  - Filter to show only logs with comments
  
- **CreateLogDialog**:
  - Form to input log details (grade, type, way, comment, video URL)
  - Dropdown menus for type and way selection
  - Character counters for comment (1000 chars) and video URL (255 chars)
  - Validation to ensure grade is within valid range (300-950)
  - Loading state while creating log
  - Error display if log creation fails

#### AreaDetailScreen.kt
- Displays logged state on RouteCard components
- Integrates with shared logged routes state

#### RouteCard.kt
- Shows filled check icon when route is logged
- `isClimbed` parameter determines logged state

### 5. App Initialization (TopoClimbApp.kt)
- Loads user's logged routes on app start
- Stores logged route IDs in shared state for efficient access

## Authentication Flow
1. User must be authenticated (have an auth token stored in BackendConfig)
2. Auth token is automatically included in API requests via `Authorization: Bearer {token}` header
3. If not authenticated, log creation shows appropriate error message

## State Management
- **Shared State**: Logged route IDs are stored in a companion object in RouteDetailViewModel
- This ensures all parts of the app have access to the logged state
- State is updated when:
  - App starts (loads from /user/logs)
  - User creates a new log
  - Logs are refreshed

## User Experience
1. **Creating a Log**:
   - Click check icon in Overview tab OR "Add Log" button in Logs tab
   - Fill in log details in dialog
   - Submit to create log
   - Logs tab automatically refreshes to show new log
   
2. **Viewing Logged Routes**:
   - Check icons appear filled on RouteCard in lists
   - Check icon appears filled in RouteDetailBottomSheet Overview tab
   
3. **Refreshing Logs**:
   - Pull down in Logs tab to refresh
   - Automatic refresh after creating a log

## Error Handling
- Authentication errors show appropriate messages
- Network errors are caught and displayed
- Graceful degradation if user is not authenticated
- Silent failure if user/logs endpoint fails on app start

## Technical Notes
- Uses Retrofit for API calls
- Kotlin Coroutines for asynchronous operations
- StateFlow for reactive state management
- Compose UI with Material3 components
- Pull-to-refresh using Material3's PullToRefreshBox

## Future Enhancements
- Edit/delete logs
- Sort and filter logs by various criteria
- Display log statistics
- Social features (sharing logs, comparing with friends)
