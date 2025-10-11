# TopoClimb Android App

A native Android application for browsing climbing sites, routes, and areas from the TopoClimb platform.

## Features

- **Browse Sites**: View all climbing sites with location information
- **View Routes**: Browse climbing routes with filtering capabilities
- **Filter Routes**: Filter routes by grade and type (sport, trad, boulder, etc.)
- **Browse Areas**: Explore climbing areas
- **Clean Navigation**: Easy navigation between sites, routes, and areas using bottom navigation

## Architecture

This app follows modern Android development practices:

- **MVVM Architecture**: Separation of concerns with ViewModels and UI State
- **Jetpack Compose**: Modern declarative UI framework
- **Coroutines & Flow**: Asynchronous programming and reactive state management
- **Retrofit**: Type-safe HTTP client for API communication
- **Navigation Component**: Declarative navigation with Compose Navigation
- **Repository Pattern**: Clean data layer abstraction

## Project Structure

```
app/src/main/java/com/example/topoclimb/
├── data/               # Data models (Site, Route, Area)
├── network/            # API service and Retrofit setup
├── repository/         # Repository layer for data operations
├── viewmodel/          # ViewModels for each screen
├── ui/
│   ├── screens/        # Composable screens (Sites, Routes, Areas)
│   ├── theme/          # App theming
│   └── TopoClimbApp.kt # Main app navigation
└── MainActivity.kt     # Entry point
```

## Setup

### Prerequisites

- Android Studio Hedgehog or later
- JDK 11 or later
- Android SDK 24+ (minimum)
- Android SDK 36 (target)

### Configuration

1. Clone the repository
2. Open the project in Android Studio
3. Update the API base URL in `app/src/main/java/com/example/topoclimb/network/RetrofitInstance.kt`:

```kotlin
private const val BASE_URL = "https://your-topoclimb-api.com/"
```

4. Sync Gradle dependencies
5. Build and run the app

## API Integration

The app expects the following API endpoints (matching the TopoClimb OpenAPI specification):

- `GET /sites` - List all climbing sites
- `GET /sites/{id}` - Get a specific site
- `GET /routes` - List all routes (with optional filters: siteId, grade, type)
- `GET /routes/{id}` - Get a specific route
- `GET /areas` - List all climbing areas
- `GET /areas/{id}` - Get a specific area

## Building

### Debug Build
```bash
./gradlew assembleDebug
```

### Release Build
```bash
./gradlew assembleRelease
```

## Testing

Run unit tests:
```bash
./gradlew test
```

Run instrumented tests:
```bash
./gradlew connectedAndroidTest
```

## Dependencies

Key dependencies include:

- AndroidX Core KTX
- Jetpack Compose (Material3, Navigation, Lifecycle)
- Retrofit & OkHttp (Networking)
- Coil (Image loading)
- Coroutines & Flow (Async operations)

See `app/build.gradle.kts` for complete dependency list.

## No Authentication Required

This app implements the public-facing features of TopoClimb that don't require user authentication, making it easy for anyone to browse climbing sites and routes.

## Future Enhancements

Potential features for future development:

- Route detail screen with photos and comments
- Map view showing site locations
- Offline caching of sites and routes
- Search functionality
- Favorites/bookmarking
- User contributions (with authentication)

## License

MIT License - see LICENSE file for details

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.
