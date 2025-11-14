# Cache Implementation Summary

## Overview
This implementation adds a comprehensive caching system to the TopoClimb Android app using Room database for persistent storage, significantly improving app performance and offline usability.

## Features Implemented

### 1. Room Database Integration
- **Dependencies Added**: Room 2.6.1 with KSP annotation processing
- **Database Structure**: AppDatabase with 7 entity tables
- **Type Converters**: Support for List<String> and Map<String, Int>

### 2. Cache Entities
All major data models have corresponding cache entities:
- `SiteEntity`: Caches site information including grading systems
- `AreaEntity`: Caches climbing areas
- `RouteEntity`: Caches route data (excluding filtered images per requirements)
- `SectorEntity`: Caches sectors within areas
- `LineEntity`: Caches lines within sectors
- `SectorSchemaEntity`: Caches topo map and schema data
- `ContestEntity`: Caches contest information

### 3. Data Access Objects (DAOs)
Each entity has a dedicated DAO with common operations:
- Query by ID and backend ID
- Query by parent entity (e.g., areas by site)
- Insert single or multiple entities
- Delete operations for cache clearing

### 4. Cache Manager
`CacheManager` handles cache operations and expiration logic:
- **Expiration Policies**:
  - Sites & Site Details: 1 week (604,800,000 ms)
  - Areas, Routes, Sectors, Lines: 3 days (259,200,000 ms)
  - Individual route data: 1 week
- **Cache Validation**: Automatically checks if cached data is still valid
- **Fallback Support**: Returns expired cache data if network fails

### 5. Cache Preferences
`CachePreferences` manages user settings via SharedPreferences:
- Cache enable/disable toggle (default: enabled)
- Persists across app restarts

### 6. Repository Integration

#### FederatedTopoClimbRepository
Updated to support caching with the following pattern for all operations:
1. Check if cache is enabled and not force refresh
2. Try to get data from cache
3. If cache miss, fetch from network
4. Cache the network response
5. On network error, return cached data even if expired

Updated methods:
- `getSites(forceRefresh: Boolean)`
- `getSite(backendId, siteId, forceRefresh: Boolean)`
- `getAreasBySite(backendId, siteId, forceRefresh: Boolean)`
- `getContestsBySite(backendId, siteId, forceRefresh: Boolean)`

#### TopoClimbRepository
Updated with similar cache support for non-federated usage

### 7. ViewModel Updates

#### SitesViewModel
- `loadSites()`: Uses cache (forceRefresh = false)
- `refreshSites()`: Forces network refresh (forceRefresh = true)

#### SiteDetailViewModel
- `loadSiteDetails()`: Uses cache for initial load
- `refreshSiteDetails()`: Forces network refresh on pull-to-refresh

#### ProfileViewModel
- Added cache toggle functionality
- Added clear cache functionality
- Auto-clears cache when disabled
- Loads and persists cache preference state

### 8. UI Integration

#### ProfileScreen
Added cache settings to the Settings card:
- Toggle switch to enable/disable caching
- Clear cache button (visible when caching is enabled)
- Descriptive labels explaining cache functionality

## Cache Policy Implementation

### A. Cache-First Strategy
1. App always reads from cache (DB) first
2. If resource is missing or expired, downloads from server
3. Network response is cached for future use

### B. Pull-to-Refresh Behavior
- Pull-to-refresh always bypasses cache (forceRefresh = true)
- Fetches fresh data from network
- Updates cache with new data
- If network fails, falls back to cached data

### C. Expiration Policies

| Resource | Expiration | Notes |
|----------|-----------|-------|
| Sites (list) | 1 week | Site objects + pictures |
| Site Details | 1 week | Site data + contests + areas |
| Areas | 3 days | Sectors + lines + topo data |
| Routes (list) | 3 days | Route additions/removals |
| Routes (data) | 1 week | Individual cached route data |

### D. User Settings
- Users can enable/disable caching in Profile > Settings
- When disabled, cache is automatically cleared
- Cache remains cleared until re-enabled

## Technical Details

### Database Schema
```
topoclimb_cache_database (version 1)
├── sites
├── areas
├── routes
├── sectors
├── lines
├── sector_schemas
└── contests
```

### Federation Support
- Each cached entity stores `backendId` to support multiple backends
- Cache operations are scoped to specific backends
- Allows independent caching for federated instances

### Type Conversions
- `StringListConverter`: Handles List<String> serialization
- `StringMapConverter`: Handles Map<String, Int> serialization
- Uses Gson for JSON serialization

## Testing

### Unit Tests
Created `CacheTest` with the following test cases:
- Entity conversion (to/from domain objects)
- Grading system preservation
- Cache timestamp generation
- Expiration constant validation

All tests pass successfully.

## Build Configuration

### Dependencies Added
```kotlin
// Room
implementation("androidx.room:room-runtime:2.6.1")
implementation("androidx.room:room-ktx:2.6.1")
ksp("androidx.room:room-compiler:2.6.1")
```

### Plugins Added
```kotlin
id("com.google.devtools.ksp") version "2.0.21-1.0.28"
```

## Performance Impact

### Benefits
1. **Faster Load Times**: Initial data loads from local database
2. **Offline Support**: App functions with cached data when offline
3. **Reduced Network Usage**: Less API calls, lower data consumption
4. **Better UX**: Instant content display from cache

### Trade-offs
1. **Storage**: Additional disk space for database
2. **Memory**: Room database instances in memory
3. **Complexity**: Additional cache management logic

## Future Enhancements

### Not Implemented (Optional)
1. **File Caching**: Local storage for images/SVG (Coil handles images)
2. **Offline Warning**: UI indicator when using expired cache
3. **Cache Statistics**: Show cache size and last update times
4. **Selective Cache**: Clear cache for specific items

### Potential Improvements
1. **Background Sync**: Periodic cache refresh in background
2. **Cache Preloading**: Preload frequently accessed data
3. **Compression**: Compress cached data to save space
4. **Analytics**: Track cache hit/miss rates

## Migration Notes

### From No Cache to Cache
- First app launch after update will populate cache
- No migration needed - database version 1
- Cache will build up naturally as users navigate

### Disabling Cache
- Users can disable via Settings
- Cache is automatically cleared
- App falls back to network-only mode

## Known Limitations

1. **Filtered Images**: Not cached per requirements
2. **Logs**: Route logs are not cached (as specified)
3. **Real-time Data**: Cache may show stale data until refresh
4. **Storage Limit**: No automatic cache size management

## Conclusion

The cache implementation successfully meets all requirements from the problem statement:
- ✅ Cache-first strategy with network fallback
- ✅ Pull-to-refresh bypasses cache
- ✅ Correct expiration policies (1 week / 3 days)
- ✅ User settings to enable/disable cache
- ✅ Automatic cache clearing when disabled

The implementation provides significant performance improvements while maintaining data freshness through appropriate expiration policies.
