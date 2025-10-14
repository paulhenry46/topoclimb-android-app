# Bug Fix: Route Details Crash

## Problem
When clicking on a route to view route details, the app crashed with a deserialization error - the same issue that was previously fixed for sites.

## Root Cause
The API endpoint `GET /routes/{id}` was returning a wrapped response:
```json
{
  "data": {
    "id": 1,
    "name": "La Marie-Rose",
    "grade": "7c",
    ...
  }
}
```

However, the app code expected a plain `Route` object without the `data` wrapper, causing Retrofit/Gson to fail deserializing the response.

## Solution
Created a `RouteResponse` wrapper class to match the API's response format, following the exact same pattern used for other endpoints (e.g., `SiteResponse`, `AreaResponse`).

### Changes Made

1. **Added `RouteResponse` data class** (`RoutesResponse.kt`)
   ```kotlin
   data class RouteResponse(
       @SerializedName("data")
       val data: Route
   )
   ```

2. **Updated API service** (`TopoClimbApiService.kt`)
   - Changed return type from `Route` to `RouteResponse` for `getRoute(id: Int)`
   - Added import for `RouteResponse`

3. **Updated repository** (`TopoClimbRepository.kt`)
   - Unwraps the `RouteResponse` to extract the `Route` object
   - Maintains the same `Result<Route>` return type for backward compatibility

4. **Added test** (`DataModelsTest.kt`)
   - Added test `routeResponse_deserializesCorrectly()` to verify `RouteResponse` deserializes correctly

## Impact
- Any ViewModels using `getRoute()` continue to work without any changes
- The UI layer is not affected
- All existing tests continue to pass
- New test ensures the wrapped response format is handled correctly

## API Consistency
This fix aligns the single route endpoint with the pattern used by ALL other API endpoints:
- `GET /sites` → `SitesResponse` with `data: List<Site>`
- `GET /sites/{id}` → `SiteResponse` with `data: Site` ✅
- `GET /routes` → `RoutesResponse` with `data: List<Route>`
- `GET /routes/{id}` → `RouteResponse` with `data: Route` ✅ **FIXED**
- `GET /areas` → `AreasResponse` with `data: List<Area>`
- `GET /areas/{id}` → `AreaResponse` with `data: Area` ✅

## Files Changed

1. `app/src/main/java/com/example/topoclimb/data/RoutesResponse.kt`
   - Added `RouteResponse` data class

2. `app/src/main/java/com/example/topoclimb/network/TopoClimbApiService.kt`
   - Line 11: Added import for `RouteResponse`
   - Line 38: Changed return type from `Route` to `RouteResponse`

3. `app/src/main/java/com/example/topoclimb/repository/TopoClimbRepository.kt`
   - Lines 51-53: Updated to unwrap `RouteResponse` and extract `Route`

4. `app/src/test/java/com/example/topoclimb/data/DataModelsTest.kt`
   - Lines 194-225: Added `routeResponse_deserializesCorrectly()` test

**Total changes**: 4 files, ~40 lines added, minimal modification to fix the core issue

## Testing

✅ **Build**: `./gradlew assembleDebug` - SUCCESS  
✅ **Tests**: `./gradlew test` - All tests passing (8/8)  
✅ **New Test**: RouteResponse deserialization verified  
✅ **Backward Compatibility**: Existing ViewModels unaffected

## Related Issues

This is the same issue that was previously fixed for `GET /sites/{id}` (see BUGFIX_SITE_DETAILS_CRASH.md). The API consistently returns wrapped responses with `{"data": ...}` for all endpoints, but the route endpoint was missed in the initial fix.
