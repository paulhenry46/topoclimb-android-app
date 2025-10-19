# Multi-Backend Federation System

## Overview

The TopoClimb Android app now supports fetching data from multiple backend URLs, allowing users to aggregate climbing data from various sources into a single unified view. Each resource is wrapped in a `Federated<T>` object that includes backend metadata, making the source transparent yet accessible.

## Key Components

### 1. Core Data Classes

#### `Federated<T>`
Wraps any resource type with backend metadata:
```kotlin
data class Federated<T>(
    val data: T,
    val backend: BackendMetadata
)
```

#### `BackendMetadata`
Contains information about the source backend:
```kotlin
data class BackendMetadata(
    val backendId: String,
    val backendName: String,
    val baseUrl: String
)
```

#### `BackendConfig`
Configuration for a backend API endpoint:
```kotlin
data class BackendConfig(
    val id: String,
    val name: String,
    val baseUrl: String,
    val enabled: Boolean = true,
    val createdAt: Long,
    val updatedAt: Long
)
```

### 2. Repository Layer

#### `BackendConfigRepository`
- Manages backend configurations using SharedPreferences
- Provides CRUD operations for backends
- Ensures at least one backend is always enabled
- Initializes with default backend from `AppConfig.API_BASE_URL`

#### `FederatedTopoClimbRepository`
- Aggregates data from multiple enabled backends
- Fetches data in parallel using coroutines
- Wraps all resources in `Federated<T>` objects
- Handles failures gracefully (returns empty list for failed backends)

#### `MultiBackendRetrofitManager`
- Creates and caches Retrofit instances for each backend
- Manages multiple API services simultaneously
- Provides efficient API service retrieval

### 3. UI Components

#### Backend Management Screen
- Add new backends
- Edit existing backends
- Delete backends (with protection against deleting the last one)
- Toggle backends on/off (with protection against disabling all)
- URL validation (must start with http:// or https:// and end with /)

#### Updated Screens
All main screens now display the backend source:
- **SitesScreen**: Shows "Source: [Backend Name]" for each site
- **SiteDetailScreen**: Displays backend source for site and its areas
- **AreasScreen**: Shows backend source for each area
- **ProfileScreen**: Link to backend management

### 4. Navigation

Navigation has been updated to include `backendId` parameter:
- Sites: `/site/{backendId}/{siteId}`
- Areas: `/area/{backendId}/{areaId}`

This ensures proper routing and data fetching from the correct backend.

## User Experience

### From the User's Perspective
The federation system is **completely transparent**:
1. Users can browse sites, areas, and routes from all configured backends in a unified interface
2. The UI shows a subtle "Source: [Backend Name]" label to indicate the data origin
3. All data appears seamlessly integrated regardless of its source
4. Users interact with resources the same way, whether they come from one backend or many

### Backend Management
Users can easily manage backends through the Profile screen:
1. Navigate to Profile → Manage Backends
2. Add new backends with a name and URL
3. Toggle backends on/off without deleting them
4. Edit backend details or delete unwanted backends

## Technical Details

### Data Fetching
When fetching resources from multiple backends:
```kotlin
suspend fun getSites(): Result<List<Federated<Site>>> {
    val enabledBackends = backendConfigRepository.getEnabledBackends()
    
    val sites = coroutineScope {
        enabledBackends.map { backend ->
            async {
                try {
                    val api = retrofitManager.getApiService(backend)
                    val response = api.getSites()
                    response.data.map { site ->
                        Federated(
                            data = site,
                            backend = backend.toMetadata()
                        )
                    }
                } catch (e: Exception) {
                    emptyList<Federated<Site>>()
                }
            }
        }.awaitAll().flatten()
    }
    
    return Result.success(sites)
}
```

### Global Resource IDs
Each federated resource can generate a globally unique ID:
```kotlin
val globalId = federatedSite.getGlobalId(site.id)
// Returns: "backendId:resourceId" (e.g., "backend-uuid:42")
```

### URL Validation
Backend URLs must:
- Start with `http://` or `https://`
- End with `/`
- Not be blank

Example valid URLs:
- `https://api.topoclimb.example.com/`
- `http://localhost:8000/api/v1/`
- `https://topoclimb.saux.fr/api/v1/`

## Architecture Benefits

1. **Scalability**: Easy to add new backends without code changes
2. **Resilience**: Failures in one backend don't affect others
3. **Flexibility**: Users control which backends to query
4. **Transparency**: Clear indication of data source
5. **Performance**: Parallel fetching using coroutines
6. **Maintainability**: Clean separation of concerns

## Future Enhancements

Potential improvements:
1. Backend health monitoring
2. Caching federated data locally
3. Conflict resolution for duplicate resources
4. Backend-specific authentication
5. Sync status indicators
6. Backend priority/ordering
7. Custom backend icons/branding

## Testing

The federation system includes comprehensive unit tests:
- `FederationTest.kt`: Tests for `Federated`, `BackendConfig`, and `BackendMetadata`
- Validates URL format checking
- Tests global ID generation
- Verifies data wrapping

Run tests with:
```bash
./gradlew test
```

## Migration from Single Backend

The system maintains backward compatibility:
1. On first run, the default backend from `AppConfig.API_BASE_URL` is automatically added
2. All existing code continues to work
3. ViewModels now use `AndroidViewModel` to access application context
4. Navigation updated to pass `backendId` parameters

## Example Usage

### Adding a Backend Programmatically
```kotlin
val repository = BackendConfigRepository(context)
repository.addBackend(
    BackendConfig(
        name = "Production API",
        baseUrl = "https://api.production.example.com/"
    )
)
```

### Fetching Federated Data
```kotlin
val repository = FederatedTopoClimbRepository(context)
repository.getSites()
    .onSuccess { federatedSites ->
        federatedSites.forEach { federatedSite ->
            println("${federatedSite.data.name} from ${federatedSite.backend.backendName}")
        }
    }
```

### Displaying Backend Source in UI
```kotlin
Text(
    text = "Source: ${federatedSite.backend.backendName}",
    style = MaterialTheme.typography.labelSmall,
    color = MaterialTheme.colorScheme.onSurfaceVariant
)
```

## Conclusion

The multi-backend federation system provides a powerful, flexible way to aggregate climbing data from multiple sources while maintaining a seamless user experience. The implementation follows Android best practices and maintains clean architecture principles.
