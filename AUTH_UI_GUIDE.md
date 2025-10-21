# Authentication Feature UI Guide

## Overview

This guide illustrates the user interface changes for the authentication feature in the TopoClimb Android app.

## UI Screens

### 1. Backend Management Screen (Before Authentication)

The Backend Management screen shows all configured TopoClimb instances. Before authentication:

```
┌─────────────────────────────────────────┐
│ ← Manage TopoClimb Instances            │
├─────────────────────────────────────────┤
│                                         │
│ Configure TopoClimb instance URLs to   │
│ fetch climbing data from multiple      │
│ sources                                │
│                                         │
│ ┌─────────────────────────────────┐   │
│ │ Default Backend            [ON] │   │
│ │ https://api.example.com/        │   │
│ │                                 │   │
│ │ [Login]        [Edit] [Delete] │   │
│ └─────────────────────────────────┘   │
│                                         │
│ ┌─────────────────────────────────┐   │
│ │ My Instance            [ON]     │   │
│ │ https://climb.mysite.com/       │   │
│ │                                 │   │
│ │ [Login]        [Edit] [Delete] │   │
│ └─────────────────────────────────┘   │
│                                         │
└─────────────────────────────────────────┘
                 [+]
```

### 2. Login Screen

When user clicks "Login" on an instance:

```
┌─────────────────────────────────────────┐
│ ← Login to Default Backend              │
├─────────────────────────────────────────┤
│                                         │
│         Sign in to your account         │
│                                         │
│ ┌─────────────────────────────────┐   │
│ │ Email                           │   │
│ │ user@example.com                │   │
│ └─────────────────────────────────┘   │
│                                         │
│ ┌─────────────────────────────────┐   │
│ │ Password              👁         │   │
│ │ ●●●●●●●●●●●●●●●●●●●             │   │
│ └─────────────────────────────────┘   │
│                                         │
│ ┌─────────────────────────────────┐   │
│ │         Sign In                 │   │
│ └─────────────────────────────────┘   │
│                                         │
└─────────────────────────────────────────┘
```

### 3. Backend Management Screen (After Authentication)

After successful authentication, the instance shows authenticated status:

```
┌─────────────────────────────────────────┐
│ ← Manage TopoClimb Instances            │
├─────────────────────────────────────────┤
│                                         │
│ Configure TopoClimb instance URLs to   │
│ fetch climbing data from multiple      │
│ sources                                │
│                                         │
│ ┌─────────────────────────────────┐   │
│ │ Default Backend ✓ [Default][ON]│   │
│ │ https://api.example.com/        │   │
│ │ Logged in as John Climber       │   │
│ │                                 │   │
│ │ [Logout]       [Edit] [Delete] │   │
│ └─────────────────────────────────┘   │
│                                         │
│ ┌─────────────────────────────────┐   │
│ │ My Instance ✓          [ON]     │   │
│ │ https://climb.mysite.com/       │   │
│ │ Logged in as Jane Smith         │   │
│ │                                 │   │
│ │ [Logout] [Set as Default]      │   │
│ │          [Edit] [Delete]       │   │
│ └─────────────────────────────────┘   │
│                                         │
└─────────────────────────────────────────┘
                 [+]
```

**UI Elements:**
- ✓ Checkmark icon: Indicates authenticated instance
- [Default] badge: Shows which instance provides profile data
- "Logged in as..." text: Shows authenticated username
- [Logout] button: Replaces [Login] when authenticated
- [Set as Default] button: Available on non-default authenticated instances

### 4. Profile Screen (Not Authenticated)

When no instances are authenticated:

```
┌─────────────────────────────────────────┐
│ Profile                                 │
├─────────────────────────────────────────┤
│                                         │
│ ┌─────────────────────────────────┐   │
│ │                                 │   │
│ │       Not Logged In             │   │
│ │                                 │   │
│ │ Login to a TopoClimb instance   │   │
│ │ to see your profile             │   │
│ │                                 │   │
│ └─────────────────────────────────┘   │
│                                         │
│ ┌─────────────────────────────────┐   │
│ │ Settings                        │   │
│ │                                 │   │
│ │ ┌─────────────────────────┐     │   │
│ │ │ ⚙ Manage TopoClimb     │     │   │
│ │ │   Instances            │     │   │
│ │ └─────────────────────────┘     │   │
│ └─────────────────────────────────┘   │
│                                         │
└─────────────────────────────────────────┘
```

### 5. Profile Screen (Authenticated)

When authenticated to an instance:

```
┌─────────────────────────────────────────┐
│ Profile                                 │
├─────────────────────────────────────────┤
│                                         │
│            ┌─────────┐                  │
│            │  Photo  │                  │
│            └─────────┘                  │
│                                         │
│          John Climber                   │
│       john@example.com                  │
│     from Default Backend                │
│                                         │
│ ┌─────────────────────────────────┐   │
│ │ Account Information             │   │
│ │                                 │   │
│ │ Birth Date      2014-09-01      │   │
│ │ Gender          Male            │   │
│ │ Member Since    2025-09-08      │   │
│ │                                 │   │
│ └─────────────────────────────────┘   │
│                                         │
│ ┌─────────────────────────────────┐   │
│ │ Settings                        │   │
│ │                                 │   │
│ │ ┌─────────────────────────┐     │   │
│ │ │ ⚙ Manage TopoClimb     │     │   │
│ │ │   Instances            │     │   │
│ │ └─────────────────────────┘     │   │
│ └─────────────────────────────────┘   │
│                                         │
└─────────────────────────────────────────┘
```

**UI Elements:**
- Profile photo from API (or UI avatars fallback)
- User name and email
- "from [Instance Name]" text showing data source
- Account information card with user details
- Settings button to manage instances

## User Flows

### Flow 1: First-time Authentication

1. User opens app → Profile screen shows "Not Logged In"
2. User taps "Manage TopoClimb Instances"
3. User sees their configured instances with [Login] buttons
4. User taps [Login] on an instance
5. User enters email and password
6. User taps "Sign In"
7. App sends request to `/login` endpoint
8. On success, user returns to Backend Management screen
9. Instance now shows ✓ checkmark and [Default] badge
10. Profile screen now displays user data

### Flow 2: Multiple Instance Authentication

1. User has one authenticated instance (marked as Default)
2. User taps [Login] on another instance
3. User logs in successfully
4. New instance shows ✓ checkmark but no [Default] badge
5. User can tap [Set as Default] to change profile data source
6. Default badge moves to newly selected instance
7. Profile screen updates with new instance's user data

### Flow 3: Logout

1. User has authenticated instances
2. User taps [Logout] on an instance
3. Instance loses ✓ checkmark and shows [Login] button again
4. If it was the default instance, another authenticated instance becomes default
5. If it was the only authenticated instance, Profile screen shows "Not Logged In"

## Key Features

### Visual Indicators

1. **Authentication Status**: ✓ checkmark icon clearly shows which instances are authenticated
2. **Default Instance**: [Default] badge indicates which instance provides profile data
3. **Username Display**: "Logged in as [name]" provides quick identification
4. **Instance Attribution**: "from [Instance]" on profile shows data source

### Button States

- **Not Authenticated**: [Login] button available
- **Authenticated (Default)**: [Logout] button only
- **Authenticated (Non-default)**: [Logout] and [Set as Default] buttons

### Form Validation

- Email format validation with error messages
- Password visibility toggle for user convenience
- Disabled submit button until form is valid
- Loading state during authentication
- Error messages for failed authentication

### Data Display

- Profile photo with fallback to UI avatars
- Formatted dates (YYYY-MM-DD)
- Capitalized gender display
- Safe string operations with length checks

## Error Handling

### Login Errors

```
┌─────────────────────────────────────────┐
│ ← Login to Default Backend              │
├─────────────────────────────────────────┤
│                                         │
│         Sign in to your account         │
│                                         │
│ ┌─────────────────────────────────┐   │
│ │ Email                           │   │
│ │ user@example.com                │   │
│ └─────────────────────────────────┘   │
│                                         │
│ ┌─────────────────────────────────┐   │
│ │ Password              👁         │   │
│ │ ●●●●●●●●●●●●●●●●●●●             │   │
│ └─────────────────────────────────┘   │
│                                         │
│ ⚠ Invalid email or password            │
│                                         │
│ ┌─────────────────────────────────┐   │
│ │         Sign In                 │   │
│ └─────────────────────────────────┘   │
│                                         │
└─────────────────────────────────────────┘
```

### Email Validation

```
┌─────────────────────────────────────┐
│ Email                               │
│ notanemail                          │
│ Invalid email format                │
└─────────────────────────────────────┘
```

## Accessibility

- Proper content descriptions for icons
- Keyboard navigation support
- Error messages are clearly communicated
- Form validation with visual feedback
- High contrast colors for important indicators

## Material Design 3

The implementation follows Material Design 3 guidelines:

- Cards for grouping related content
- Proper elevation and shadows
- Material color scheme usage
- Icon usage from Material Icons
- Typography scale consistency
- Touch target sizes (48dp minimum)

## Conclusion

This authentication feature provides a complete, user-friendly solution for managing authentication across multiple TopoClimb instances while maintaining a clean and intuitive interface.
