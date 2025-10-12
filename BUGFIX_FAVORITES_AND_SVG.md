# Bug Fixes: Favorites and SVG Map Display

## Issues Fixed

### Issue 1: Favorites Not Appearing in Favorite Screen ✅

**Problem:** When a user marks a site as favorite and navigates to the Favorite screen, the favorite site doesn't appear.

**Root Cause:** The `SitesScreen` and Favorite screen were using separate instances of `SitesViewModel`. When navigating between screens, a new ViewModel instance was created, losing the favorite state.

**Solution:** 
- Modified `TopoClimbApp.kt` to share the same `SitesViewModel` instance between both screens
- Used ViewModel scoping to the parent `NavBackStackEntry` (the NavHost's start destination)
- Both screens now reference the same ViewModel instance via `viewModel(viewModelStoreOwner = parentEntry)`

**Code Changes:**
```kotlin
// Both Sites and Favorite screens now use:
val parentEntry = remember(backStackEntry) {
    navController.getBackStackEntry(navController.graph.findStartDestination().id)
}
SitesScreen(
    // ... other params
    viewModel = viewModel(viewModelStoreOwner = parentEntry)
)
```

### Issue 2: SVG Map Not Displaying in Area Details ✅

**Problem:** The SVG map doesn't display in the area detail screen, even though the API response is good.

**Root Cause:** 
- Used `URL.readText()` which is a Kotlin stdlib function not optimized for Android
- Missing proper IO dispatcher for network calls
- No error handling to debug issues

**Solution:**
- Replaced `URL.readText()` with OkHttp client (already available in dependencies)
- Added proper `Dispatchers.IO` for network operations
- Added response success check before reading body
- Added `printStackTrace()` for debugging any errors

**Code Changes:**
```kotlin
val svgContent = area?.svgMap?.let { mapUrl ->
    try {
        withContext(Dispatchers.IO) {
            val request = Request.Builder()
                .url(mapUrl)
                .build()
            
            httpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    response.body?.string()
                } else {
                    null
                }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
```

## Files Modified

1. **app/src/main/java/com/example/topoclimb/ui/TopoClimbApp.kt**
   - Added `remember` and `viewModel` imports
   - Modified Sites screen composable to use shared ViewModel
   - Modified Favorite screen composable to use shared ViewModel

2. **app/src/main/java/com/example/topoclimb/viewmodel/AreaDetailViewModel.kt**
   - Replaced `java.net.URL` import with OkHttp imports
   - Added `Dispatchers` and `withContext` for coroutines
   - Created OkHttpClient instance
   - Replaced URL.readText() with proper OkHttp request

## Testing

- ✅ Build successful (`./gradlew assembleDebug`)
- ✅ All existing tests pass (`./gradlew test`)
- ✅ No new dependencies added (used existing OkHttp)
- ✅ Minimal code changes (only 2 files modified)

## Impact

These fixes address both user-reported issues:
1. Favorites now persist when navigating between Sites and Favorite screens
2. SVG maps should now properly display in area detail screens with proper network handling
