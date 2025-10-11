# Bug Fix: Site Details Crash

## Problem
When clicking on a site to view site details, the app crashed with a deserialization error.

## Root Cause
The API endpoint `GET /sites/{id}` was returning a wrapped response:
```json
{
  "data": {
    "id": 1,
    "name": "Fontainebleau",
    ...
  }
}
```

However, the app code expected a plain `Site` object without the `data` wrapper, causing Retrofit/Gson to fail deserializing the response.

## Solution
Created a `SiteResponse` wrapper class to match the API's response format, similar to the existing pattern used for other endpoints (e.g., `SitesResponse`, `AreasResponse`, `ContestsResponse`).

### Changes Made

1. **Added `SiteResponse` data class** (`SiteResponse.kt`)
   ```kotlin
   data class SiteResponse(
       @SerializedName("data")
       val data: Site
   )
   ```

2. **Updated API service** (`TopoClimbApiService.kt`)
   - Changed return type from `Site` to `SiteResponse` for `getSite(id: Int)`

3. **Updated repository** (`TopoClimbRepository.kt`)
   - Unwraps the `SiteResponse` to extract the `Site` object
   - Maintains the same `Result<Site>` return type for backward compatibility

4. **Added test** (`DataModelsTest.kt`)
   - Added test to verify `SiteResponse` deserializes correctly

## Impact
- The `SiteDetailViewModel` continues to work without any changes
- The UI layer is not affected
- All existing tests continue to pass
- New test ensures the wrapped response format is handled correctly

## API Consistency
This fix aligns the single site endpoint with the pattern used by other API endpoints:
- `GET /sites` → `SitesResponse` with `data: List<Site>`
- `GET /sites/{id}` → `SiteResponse` with `data: Site`
- `GET /sites/{siteId}/areas` → `AreasResponse` with `data: List<Area>`
- `GET /sites/{siteId}/contests` → `ContestsResponse` with `data: List<Contest>`
