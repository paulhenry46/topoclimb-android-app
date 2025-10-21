# Authentication Feature Implementation Summary

## Overview

This document describes the implementation of the authentication feature for the TopoClimb Android app. Users can now authenticate to different TopoClimb instances using email and password, and their profile information will be displayed from the default authenticated instance.

## Features Implemented

### 1. Authentication Data Models

Created `User.kt` with the following models:

- **User**: Contains user profile information (id, name, email, profile photo, birth date, gender, etc.)
- **LoginRequest**: Request body for login endpoint (email, password)
- **AuthResponse**: Response from login endpoint containing user data and authentication token

### 2. Backend Configuration Updates

Updated `BackendConfig.kt` to support authentication:

- Added `authToken` field to store the authentication token
- Added `user` field to store the authenticated user's information
- Added `isDefault` field to mark the default instance for user profile data
- Added `isAuthenticated()` helper method to check authentication status

### 3. API Service Updates

Updated `TopoClimbApiService.kt`:

- Added `POST /login` endpoint that accepts `LoginRequest` and returns `AuthResponse`

### 4. Repository Updates

Enhanced `BackendConfigRepository.kt` with authentication methods:

- `authenticateBackend()`: Save authentication token and user data after successful login
- `logoutBackend()`: Clear authentication data for a backend
- `setDefaultBackend()`: Set a backend as the default for user profile data
- `getDefaultBackend()`: Get the default backend (or first authenticated backend)

### 5. Backend Management UI

Updated `BackendManagementScreen.kt`:

- **Authentication Status Indicator**: Shows a checkmark icon next to authenticated instances
- **Default Instance Badge**: Displays "Default" badge for the default instance
- **User Information**: Shows "Logged in as [username]" for authenticated instances
- **Login Button**: Allows users to login to instances
- **Logout Button**: Allows users to logout from instances
- **Set as Default**: Allows users to set an authenticated instance as default

### 6. Login Screen

Created `LoginScreen.kt`:

- Clean, user-friendly login form
- Email and password input fields
- Password visibility toggle
- Loading state during authentication
- Error message display
- Form validation

### 7. Profile Screen

Updated `ProfileScreen.kt`:

- Displays real user data from the default authenticated instance
- Shows user's profile photo, name, and email
- Displays account information (birth date, gender, member since)
- Shows which instance the data is from
- Falls back to "Not Logged In" state when no authentication exists

### 8. Profile ViewModel

Created `ProfileViewModel.kt`:

- Manages profile UI state
- Observes backend configurations for changes
- Automatically updates profile when default backend changes
- Provides refresh functionality

### 9. Backend Management ViewModel

Enhanced `BackendManagementViewModel.kt`:

- Added `login()` method to authenticate with credentials
- Added `logout()` method to clear authentication
- Added `setDefaultBackend()` method to manage default instance
- Added login state management (loading, error)

### 10. Navigation Updates

Updated `TopoClimbApp.kt`:

- Added login screen route: `login/{backendId}/{backendName}`
- Connected backend management screen to login screen
- Auto-navigation back to backend management after successful login
- Proper state sharing between login and backend management screens

## User Flow

### Login Flow

1. User navigates to Profile → Manage TopoClimb Instances
2. User clicks "Login" button on an instance
3. User is taken to login screen with email/password fields
4. User enters credentials and clicks "Sign In"
5. App sends POST request to `/login` endpoint
6. On success, authentication token and user data are saved
7. User is automatically returned to backend management screen
8. Instance now shows authenticated status with checkmark and username
9. If this is the first authenticated instance, it becomes the default automatically

### Profile Display Flow

1. User navigates to Profile tab
2. App retrieves the default backend configuration
3. If authenticated, displays user information from that instance
4. Shows profile photo, name, email, and account details
5. Indicates which instance the data is from

### Default Instance Management

1. User can have multiple authenticated instances
2. First authenticated instance becomes default automatically
3. User can change default by clicking "Set as Default" button
4. Profile screen always shows data from the default instance
5. Default instance is marked with a "Default" badge

## API Integration

The authentication feature integrates with the TopoClimb API:

**Login Endpoint**: `POST /login`

Request body:
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

Response:
```json
{
  "user": {
    "id": 3,
    "name": "User",
    "email": "mail@mail.com",
    "email_verified_at": null,
    "current_team_id": null,
    "profile_photo_path": null,
    "created_at": "2025-09-08T18:08:49.000000Z",
    "updated_at": "2025-10-05T06:40:34.000000Z",
    "google_id": null,
    "two_factor_confirmed_at": null,
    "birth_date": "2014-09-01T00:00:00.000000Z",
    "gender": "male",
    "profile_photo_url": "https://ui-avatars.com/api/?name=U&color=7F9CF5&background=EBF4FF"
  },
  "token": "1|3GNJrtggFEihg19dyVg0whwb8REr8X0ArfjWpoPi8bb8855d"
}
```

## Data Persistence

Authentication data is persisted using SharedPreferences:

- All backend configurations (including auth tokens and user data) are stored
- Data persists across app restarts
- Logout clears authentication data but keeps the backend configuration

## Security Considerations

- Authentication tokens are stored in SharedPreferences
- Passwords are only transmitted during login (not stored)
- Each instance maintains its own authentication state
- Logout properly clears sensitive data

## UI/UX Highlights

1. **Visual Authentication Status**: Checkmark icon clearly indicates authenticated instances
2. **Default Instance Badge**: Easy identification of which instance provides profile data
3. **Contextual Actions**: Login/logout buttons appear based on authentication state
4. **User Feedback**: Success messages and error handling throughout
5. **Seamless Navigation**: Automatic navigation after successful login
6. **Profile Integration**: Real user data displayed with instance attribution

## Files Modified

1. `app/src/main/java/com/example/topoclimb/data/BackendConfig.kt` - Added auth fields
2. `app/src/main/java/com/example/topoclimb/network/TopoClimbApiService.kt` - Added login endpoint
3. `app/src/main/java/com/example/topoclimb/repository/BackendConfigRepository.kt` - Added auth methods
4. `app/src/main/java/com/example/topoclimb/ui/screens/BackendManagementScreen.kt` - Added auth UI
5. `app/src/main/java/com/example/topoclimb/ui/screens/ProfileScreen.kt` - Updated to show real data
6. `app/src/main/java/com/example/topoclimb/viewmodel/BackendManagementViewModel.kt` - Added auth logic
7. `app/src/main/java/com/example/topoclimb/ui/TopoClimbApp.kt` - Added login navigation

## Files Created

1. `app/src/main/java/com/example/topoclimb/data/User.kt` - Authentication data models
2. `app/src/main/java/com/example/topoclimb/ui/screens/LoginScreen.kt` - Login UI
3. `app/src/main/java/com/example/topoclimb/viewmodel/ProfileViewModel.kt` - Profile state management

## Future Enhancements

Potential improvements for future development:

1. Token refresh mechanism
2. Encrypted storage for authentication tokens
3. Biometric authentication support
4. Session timeout handling
5. Multiple account switching
6. Social login integration
7. Remember me functionality
8. Password reset flow
9. Email verification status display
10. Two-factor authentication support (UI for existing backend support)

## Testing

The feature has been compiled successfully. Manual testing should verify:

1. Login with valid credentials succeeds
2. Login with invalid credentials shows error
3. Logout clears authentication data
4. Profile screen displays correct user data
5. Default instance selection works correctly
6. Authentication status persists across app restarts
7. Multiple instances can be authenticated independently
8. Navigation flows work smoothly

## Conclusion

The authentication feature is now fully integrated into the TopoClimb Android app. Users can authenticate to multiple TopoClimb instances, manage their authentication status, and see their real profile data from their chosen default instance. The implementation follows Android best practices and integrates seamlessly with the existing multi-backend architecture.
