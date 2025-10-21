# Instance Metadata Preview Feature

## Overview

This document describes the new instance metadata preview feature that enhances the "Add Instance" workflow by automatically fetching and displaying information about a TopoClimb instance.

## Feature Description

When a user adds a new TopoClimb instance, the app now automatically:

1. Fetches metadata from the `/meta` endpoint as the user types the URL
2. Displays instance information (name, description, version, logo)
3. Auto-fills the instance name field with the fetched name

## API Integration

**Endpoint**: `GET /meta`

**Response Format**:
```json
{
  "name": "Laravel",
  "description": "The Instance used for test and development of TopoClimb",
  "version": "1.0",
  "picture_url": "http://192.168.1.16:8000/storage/pictures/logo"
}
```

## User Flow

### Step 1: Open Add Instance Dialog

User clicks the "+" button to add a new instance.

```
┌─────────────────────────────────────────┐
│ Add TopoClimb Instance                  │
├─────────────────────────────────────────┤
│                                         │
│ ┌─────────────────────────────────┐   │
│ │ Website URL                     │   │
│ │                                 │   │
│ └─────────────────────────────────┘   │
│ Must end with /                        │
│                                         │
│ ┌─────────────────────────────────┐   │
│ │ Instance Name                   │   │
│ │                                 │   │
│ └─────────────────────────────────┘   │
│                                         │
│           [Cancel]  [Add]              │
└─────────────────────────────────────────┘
```

### Step 2: User Types URL

User enters a valid URL. After 500ms debounce, the app fetches metadata.

```
┌─────────────────────────────────────────┐
│ Add TopoClimb Instance                  │
├─────────────────────────────────────────┤
│                                         │
│ ┌─────────────────────────────────┐   │
│ │ Website URL                     │   │
│ │ https://api.example.com/        │   │
│ └─────────────────────────────────┘   │
│ Must end with /                        │
│                                         │
│    ⏳ Fetching instance info...        │
│                                         │
│ ┌─────────────────────────────────┐   │
│ │ Instance Name                   │   │
│ │                                 │   │
│ └─────────────────────────────────┘   │
│                                         │
│           [Cancel]  [Add]              │
└─────────────────────────────────────────┘
```

### Step 3: Metadata Displayed

Instance metadata is fetched and displayed in a card with logo, name, description, and version.

```
┌─────────────────────────────────────────┐
│ Add TopoClimb Instance                  │
├─────────────────────────────────────────┤
│                                         │
│ ┌─────────────────────────────────┐   │
│ │ Website URL                     │   │
│ │ https://api.example.com/        │   │
│ └─────────────────────────────────┘   │
│ Must end with /                        │
│                                         │
│ ┌─────────────────────────────────┐   │
│ │ 🖼️  Laravel                     │   │
│ │     The Instance used for test  │   │
│ │     and development of TopoClimb│   │
│ │     Version 1.0                 │   │
│ └─────────────────────────────────┘   │
│                                         │
│ ┌─────────────────────────────────┐   │
│ │ Instance Name                   │   │
│ │ Laravel                         │   │
│ └─────────────────────────────────┘   │
│                                         │
│           [Cancel]  [Add]              │
└─────────────────────────────────────────┘
```

**Note**: The instance name field is automatically filled with "Laravel" from the metadata.

## Technical Implementation

### New Data Model

Created `InstanceMeta.kt`:
```kotlin
data class InstanceMeta(
    val name: String,
    val description: String,
    val version: String,
    @SerializedName("picture_url")
    val pictureUrl: String?
)
```

### API Service Update

Added `/meta` endpoint to `TopoClimbApiService.kt`:
```kotlin
@GET("meta")
suspend fun getMeta(): InstanceMeta
```

### ViewModel Enhancement

Added methods to `BackendManagementViewModel.kt`:
- `fetchInstanceMeta(baseUrl: String)` - Fetches metadata from the given URL
- `clearInstanceMeta()` - Clears cached metadata

Updated UI state:
```kotlin
data class BackendManagementUiState(
    // ... existing fields
    val instanceMeta: InstanceMeta? = null,
    val metaLoading: Boolean = false
)
```

### UI Update

Enhanced `AddBackendDialog` in `BackendManagementScreen.kt`:

**Key Features**:
1. **Debounced URL Input**: Waits 500ms after user stops typing before fetching
2. **Loading Indicator**: Shows circular progress while fetching
3. **Metadata Card**: Displays logo, name, description, and version
4. **Auto-fill**: Automatically populates instance name field
5. **Error Handling**: Silently handles fetch failures (no error shown to user)

**Code Highlights**:
```kotlin
// Debounced metadata fetch
LaunchedEffect(url, urlError) {
    if (url.isNotBlank() && urlError == null) {
        delay(500) // Debounce
        viewModel.fetchInstanceMeta(url)
    }
}

// Auto-fill name
LaunchedEffect(instanceMeta) {
    if (instanceMeta != null && name.isBlank()) {
        name = instanceMeta.name
    }
}
```

## Benefits

1. **Better User Experience**: Users can see what instance they're adding before confirming
2. **Reduced Errors**: Auto-fill reduces typing errors in instance names
3. **Visual Confirmation**: Logo and description help users verify they have the correct URL
4. **Professional Look**: Makes the app feel more polished and integrated

## Edge Cases Handled

1. **Invalid URL**: Metadata fetch is only triggered for valid URLs
2. **Network Errors**: Failures are silent; user can still add instance manually
3. **Missing Logo**: Card layout adapts if `picture_url` is null
4. **Empty Name Field**: Auto-fill only works if user hasn't typed a custom name
5. **Slow Networks**: Loading indicator shows user that fetch is in progress
6. **URL Changes**: Clearing URL or making it invalid clears the cached metadata

## Usage Example

1. User opens "Add TopoClimb Instance" dialog
2. User types: `https://climb.example.com/`
3. App waits 500ms for user to finish typing
4. App fetches metadata from `https://climb.example.com/meta`
5. Metadata card appears showing instance details
6. Instance name field auto-fills with "Example Climbing Gym"
7. User clicks "Add" to save the instance

## Future Enhancements

Possible improvements for future versions:

1. Show instance statistics (number of sites, routes, etc.)
2. Display instance administrators or maintainers
3. Show instance features/capabilities
4. Color-coded instance types (production, staging, development)
5. Instance health check indicator
6. Verification badge for official instances

## Conclusion

This feature significantly improves the "Add Instance" workflow by providing immediate feedback and relevant information about the instance being added. It follows the user's suggestion to enhance the UX with automatic metadata fetching from the `/meta` endpoint.
