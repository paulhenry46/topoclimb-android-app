# API URL Refresh Issue Fix

## Problem Statement
When editing an API URL in the backend management screen and refreshing the sites screen, the changes were not being reflected. Sites were still fetched from the old URL.

## Root Cause Analysis

The issue was caused by a race condition in the cache management system:

1. **FederatedTopoClimbRepository** had an asynchronous cache clearing mechanism in its `init` block:
   ```kotlin
   init {
       scope.launch {
           backendConfigRepository.backends.collect {
               retrofitManager.clearCache()
           }
       }
   }
   ```

2. **SitesViewModel** also listened to backend changes:
   ```kotlin
   init {
       loadSites()
       viewModelScope.launch {
           backendConfigRepository.backends
               .drop(1)
               .collect {
                   loadSites()
               }
       }
   }
   ```

3. When a backend URL was updated:
   - Both listeners would be triggered by the same backend flow emission
   - The cache clearing happened asynchronously in a coroutine
   - The sites reload might execute before the cache was cleared
   - This resulted in the old cached API instance being used with the old URL

## Solution

Removed the redundant async cache clearing mechanism from `FederatedTopoClimbRepository`. 

The fix relies on the existing URL change detection in `MultiBackendRetrofitManager.getApiService()`:

```kotlin
fun getApiService(backend: BackendConfig): TopoClimbApiService {
    val cachedUrl = backendUrls[backend.id]
    
    // If URL has changed, remove the old cached instance
    if (cachedUrl != null && cachedUrl != backend.baseUrl) {
        apiInstances.remove(backend.id)
    }
    
    // Store the current URL
    backendUrls[backend.id] = backend.baseUrl
    
    return apiInstances.getOrPut(backend.id) {
        createApiService(backend.baseUrl)
    }
}
```

This method:
1. Checks if the backend URL has changed from the cached URL
2. Removes the old cached API instance if the URL changed
3. Creates a new API service with the updated URL

This synchronous approach eliminates race conditions and ensures API URL changes are immediately reflected.

## Changes Made

**File: `app/src/main/java/com/example/topoclimb/repository/FederatedTopoClimbRepository.kt`**
- Removed the `scope` property that created an application-scoped coroutine scope
- Removed the `init` block that asynchronously cleared the Retrofit cache
- Removed unused coroutine imports

## Testing

All existing unit tests pass, including:
- `getApiService_recreatesInstanceWhenUrlChanges` - Validates that URL changes trigger creation of new API service instances
- `clearCache_removesAllCachedInstances` - Validates cache clearing functionality still works when explicitly called
- `removeBackend_removesSpecificBackendCache` - Validates selective cache removal works

## Impact

**Positive:**
- API URL changes now properly reflected when refreshing sites screen
- Eliminated race condition in cache management
- Simplified code by removing redundant cache clearing mechanism
- Reduced complexity by having a single source of truth for URL change detection

**No Breaking Changes:**
- Public API remains unchanged
- All existing functionality preserved
- Existing tests continue to pass

## How to Verify

1. Navigate to Backend Management screen
2. Edit an existing backend's URL
3. Save the changes
4. Navigate back to Sites screen
5. Pull to refresh
6. Verify that sites are fetched from the new URL (check logs or verify data changes)
