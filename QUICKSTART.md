# TopoClimb Android App - Quick Start Guide

This guide will help you get started with the TopoClimb Android app development.

## Prerequisites

Before you begin, ensure you have:

- **Android Studio**: Hedgehog (2023.1.1) or later
- **JDK**: Version 11 or later
- **Android SDK**: 
  - Minimum SDK: 24 (Android 7.0)
  - Target SDK: 36
- **TopoClimb API**: Access to a running TopoClimb API instance

## Initial Setup

### 1. Clone the Repository

```bash
git clone https://github.com/paulhenry46/topoclimb-android-app.git
cd topoclimb-android-app
```

### 2. Open in Android Studio

1. Launch Android Studio
2. Select "Open an Existing Project"
3. Navigate to the cloned repository folder
4. Click "OK"

### 3. Configure API Endpoint

Edit `app/src/main/java/com/example/topoclimb/AppConfig.kt`:

```kotlin
object AppConfig {
    // Replace with your actual TopoClimb API URL
    const val API_BASE_URL = "https://your-api-url.com/"
    
    // Set to true for development, false for production
    const val ENABLE_LOGGING = true
}
```

### 4. Sync Gradle

Android Studio should automatically sync Gradle. If not:
1. Click "File" → "Sync Project with Gradle Files"
2. Wait for dependencies to download

### 5. Run the App

1. Connect an Android device or start an emulator
2. Click the "Run" button (green play icon) or press `Shift + F10`
3. Select your device/emulator
4. The app should build and launch

## Development Workflow

### Project Structure

```
topoclimb-android-app/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/topoclimb/
│   │   │   │   ├── data/           # Data models
│   │   │   │   ├── network/        # API services
│   │   │   │   ├── repository/     # Data repositories
│   │   │   │   ├── viewmodel/      # ViewModels
│   │   │   │   ├── ui/             # Compose UI
│   │   │   │   │   ├── screens/    # Screen composables
│   │   │   │   │   └── theme/      # App theme
│   │   │   │   ├── AppConfig.kt    # Configuration
│   │   │   │   └── MainActivity.kt # App entry point
│   │   │   └── AndroidManifest.xml
│   │   ├── test/                   # Unit tests
│   │   └── androidTest/            # Instrumented tests
│   └── build.gradle.kts            # App-level Gradle config
├── build.gradle.kts                # Project-level Gradle config
└── gradle/
    └── libs.versions.toml          # Dependency versions
```

### Common Tasks

#### Adding a New Screen

1. Create a new composable in `ui/screens/`:
```kotlin
@Composable
fun MyNewScreen() {
    // Your UI code
}
```

2. Add navigation route in `ui/Navigation.kt`
3. Update `NavigationGraph` in `ui/TopoClimbApp.kt`

#### Adding a New API Endpoint

1. Add method to `network/TopoClimbApiService.kt`:
```kotlin
@GET("new-endpoint")
suspend fun getNewData(): List<NewModel>
```

2. Add repository method in `repository/TopoClimbRepository.kt`:
```kotlin
suspend fun getNewData(): Result<List<NewModel>> {
    return try {
        Result.success(api.getNewData())
    } catch (e: Exception) {
        Result.failure(e)
    }
}
```

3. Update ViewModel to call the repository method

#### Modifying UI Theme

Edit files in `ui/theme/`:
- `Color.kt`: Define color palette
- `Type.kt`: Typography styles
- `Theme.kt`: Apply theme configuration

### Building Different Variants

#### Debug Build
```bash
./gradlew assembleDebug
```
Output: `app/build/outputs/apk/debug/app-debug.apk`

#### Release Build
```bash
./gradlew assembleRelease
```
Output: `app/build/outputs/apk/release/app-release.apk`

### Testing

#### Run Unit Tests
```bash
./gradlew test
```

#### Run Instrumented Tests
```bash
./gradlew connectedAndroidTest
```

#### Run Specific Test Class
```bash
./gradlew test --tests "com.example.topoclimb.ExampleUnitTest"
```

### Debugging

#### Enable API Logging

In `AppConfig.kt`, set:
```kotlin
const val ENABLE_LOGGING = true
```

Then check Logcat for HTTP requests and responses.

#### Common Issues

**Issue**: "Unable to resolve dependency"
- **Solution**: File → Invalidate Caches → Invalidate and Restart

**Issue**: "SDK location not found"
- **Solution**: Create `local.properties` with:
  ```
  sdk.dir=/path/to/Android/Sdk
  ```

**Issue**: API calls failing
- **Solution**: 
  1. Check `API_BASE_URL` in `AppConfig.kt`
  2. Verify API is accessible
  3. Check Logcat for error details
  4. Ensure `INTERNET` permission in manifest

## Working with the API

### Test API Connection

Add this temporary code to `MainActivity.kt` to test API:

```kotlin
LaunchedEffect(Unit) {
    val repository = TopoClimbRepository()
    repository.getSites().onSuccess { sites ->
        Log.d("TopoClimb", "Sites loaded: ${sites.size}")
    }.onFailure { error ->
        Log.e("TopoClimb", "Error loading sites", error)
    }
}
```

### Mock API for Testing

For local development without a backend:

1. Create a `MockApiService` implementing `TopoClimbApiService`
2. Return hardcoded test data
3. Use in Repository for testing:

```kotlin
class TopoClimbRepository(
    private val api: TopoClimbApiService = 
        if (BuildConfig.DEBUG) MockApiService() else RetrofitInstance.api
)
```

## Code Style

This project follows:
- Kotlin coding conventions
- Material Design 3 guidelines
- MVVM architecture pattern

### Format Code
- **Windows/Linux**: `Ctrl + Alt + L`
- **Mac**: `Cmd + Option + L`

### Organize Imports
- **Windows/Linux**: `Ctrl + Alt + O`
- **Mac**: `Cmd + Option + O`

## Useful Resources

- [Jetpack Compose Documentation](https://developer.android.com/jetpack/compose)
- [Material Design 3](https://m3.material.io/)
- [Retrofit Documentation](https://square.github.io/retrofit/)
- [Kotlin Coroutines Guide](https://kotlinlang.org/docs/coroutines-guide.html)

## Next Steps

1. Configure your API endpoint in `AppConfig.kt`
2. Build and run the app
3. Test with your TopoClimb API
4. Customize the UI to match your branding
5. Add additional features as needed

## Getting Help

- Check `ARCHITECTURE.md` for architecture details
- See `API_INTEGRATION.md` for API documentation
- Review `README.md` for project overview

## Contributing

When contributing:
1. Create a feature branch
2. Follow existing code style
3. Write tests for new features
4. Update documentation
5. Submit a pull request

Happy coding! 🧗‍♂️
