# TopoClimb Android App - Architecture

## Overview

The TopoClimb Android app follows the **MVVM (Model-View-ViewModel)** architecture pattern with a **Repository** layer for clean separation of concerns.

## Architecture Layers

```
┌─────────────────────────────────────────┐
│           UI Layer (Compose)            │
│  ┌────────────┬──────────┬────────────┐ │
│  │  Sites     │  Routes  │   Areas    │ │
│  │  Screen    │  Screen  │   Screen   │ │
│  └────────────┴──────────┴────────────┘ │
└─────────────────┬───────────────────────┘
                  │ observes StateFlow
                  │
┌─────────────────▼───────────────────────┐
│          ViewModel Layer                │
│  ┌────────────┬──────────┬────────────┐ │
│  │  Sites     │  Routes  │   Areas    │ │
│  │ ViewModel  │ViewModel │ ViewModel  │ │
│  └────────────┴──────────┴────────────┘ │
└─────────────────┬───────────────────────┘
                  │ calls Repository
                  │
┌─────────────────▼───────────────────────┐
│         Repository Layer                │
│    ┌──────────────────────────────┐    │
│    │  TopoClimbRepository         │    │
│    │  - getSites()                │    │
│    │  - getRoutes()               │    │
│    │  - getAreas()                │    │
│    └──────────────────────────────┘    │
└─────────────────┬───────────────────────┘
                  │ uses API Service
                  │
┌─────────────────▼───────────────────────┐
│          Network Layer                  │
│    ┌──────────────────────────────┐    │
│    │  Retrofit + OkHttp           │    │
│    │  TopoClimbApiService         │    │
│    └──────────────────────────────┘    │
└─────────────────┬───────────────────────┘
                  │ HTTP Requests
                  │
          ┌───────▼────────┐
          │  TopoClimb API │
          └────────────────┘
```

## Component Details

### UI Layer (Compose)
- **Purpose**: Display data and handle user interactions
- **Components**:
  - `SitesScreen`: Displays list of climbing sites
  - `RoutesScreen`: Shows routes with filtering options
  - `AreasScreen`: Lists climbing areas
  - `TopoClimbApp`: Main navigation and bottom bar
- **Technology**: Jetpack Compose with Material3

### ViewModel Layer
- **Purpose**: Manage UI state and business logic
- **Components**:
  - `SitesViewModel`: Manages sites list state
  - `RoutesViewModel`: Handles routes and filtering logic
  - `AreasViewModel`: Manages areas list state
- **State Management**: Uses `StateFlow` for reactive UI updates
- **Lifecycle**: Survives configuration changes

### Repository Layer
- **Purpose**: Abstract data source and provide clean API
- **Component**: `TopoClimbRepository`
- **Responsibilities**:
  - Fetch data from API
  - Error handling
  - Return `Result<T>` objects
- **Benefits**: Easy to mock for testing

### Network Layer
- **Purpose**: Handle HTTP communication
- **Components**:
  - `TopoClimbApiService`: Retrofit interface defining endpoints
  - `RetrofitInstance`: Configures Retrofit client
  - `AppConfig`: Stores API configuration
- **Technology**: Retrofit 2 + OkHttp + Gson

### Data Models
- **Site**: Represents a climbing site
- **Route**: Represents a climbing route
- **Area**: Represents a geographic climbing area

## Data Flow

### Loading Data Flow:
```
User opens screen
    ↓
ViewModel.init() called
    ↓
ViewModel calls Repository
    ↓
Repository calls API Service
    ↓
Retrofit makes HTTP request
    ↓
API returns JSON response
    ↓
Gson deserializes to Kotlin objects
    ↓
Repository returns Result<List<T>>
    ↓
ViewModel updates StateFlow
    ↓
UI recomposes with new data
```

### Filtering Flow (Routes):
```
User selects filter (grade/type)
    ↓
ViewModel.filterByGrade() called
    ↓
ViewModel updates selectedGrade state
    ↓
ViewModel.applyFilters() filters routes
    ↓
ViewModel updates filteredRoutes in StateFlow
    ↓
UI recomposes showing filtered list
```

## Key Design Patterns

### 1. MVVM Pattern
- Separates UI from business logic
- Makes testing easier
- Survives configuration changes

### 2. Repository Pattern
- Single source of truth for data
- Abstracts data source details
- Enables easy mocking for tests

### 3. Unidirectional Data Flow
- Data flows down from ViewModel to UI
- Events flow up from UI to ViewModel
- Predictable state management

### 4. Reactive Programming
- `StateFlow` for state updates
- Coroutines for async operations
- Compose reacts to state changes

## Navigation

The app uses Jetpack Navigation Compose with a bottom navigation bar:

```
Bottom Navigation:
├── Sites Tab → Sites List
│   └── Click Site → Routes for that Site
├── Routes Tab → All Routes (with filters)
└── Areas Tab → Areas List
```

## Error Handling Strategy

1. **Repository Layer**: Catches exceptions and returns `Result.failure()`
2. **ViewModel Layer**: Updates UI state with error message
3. **UI Layer**: Displays error with retry option

## Dependency Injection

Currently uses simple object instantiation. Future versions could use:
- Hilt/Dagger for DI
- ViewModelFactory for ViewModel creation

## Testing Strategy

### Unit Tests
- ViewModel logic (filtering, state updates)
- Repository error handling
- Data model validation

### Integration Tests
- API service with mock server
- Repository with fake API

### UI Tests
- Screen navigation
- Filter interactions
- Error state handling

## Performance Considerations

1. **Lazy Loading**: LazyColumn for efficient list rendering
2. **Coroutines**: Non-blocking async operations
3. **StateFlow**: Efficient state updates
4. **Compose**: Minimal recomposition

## Future Enhancements

1. **Offline Support**: Room database for caching
2. **Image Loading**: Already prepared with Coil dependency
3. **Authentication**: Add auth interceptor to OkHttp
4. **Pagination**: Add paging support for large lists
5. **Search**: Add search functionality to ViewModels
