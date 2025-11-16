# Schema Caching Implementation

## Overview
This implementation adds offline-first caching for sector schema metadata, background images, and SVG overlays, enabling users to view topo schemas even when offline.

## Features
- **Schema Metadata Caching**: Schema information (id, name, paths URL, bg URL) is cached with 24-hour TTL
- **Background Image Caching**: Schema background images are cached with a 2-week TTL
- **SVG Overlay Caching**: SVG path overlays are cached with a 1-week TTL  
- **Offline-First**: Cached data is returned immediately, with background refresh when stale
- **Pull-to-Refresh**: Forces cache update regardless of age when user explicitly refreshes

## Implementation Details

### Database Changes
1. **New Entity**: `SchemaBgCacheEntity` - Stores background images as base64-encoded data URIs
   - Table: `schema_bg_cache`
   - Fields: `url` (primary key), `content`, `lastUpdated`

2. **New DAO**: `SchemaBgCacheDao` - Database access for background image cache
   - `getSchemaBgCache(url)` - Retrieve cached image
   - `insertSchemaBgCache(cache)` - Insert/update cached image
   - `deleteSchemaBgCache(url)` - Delete specific cache entry
   - `deleteOldCaches(timestamp)` - Cleanup old entries

3. **New Entity**: `SectorSchemaEntity` - Stores schema metadata for offline access
   - Table: `sector_schemas`
   - Fields: `id`, `name`, `paths`, `bg`, `areaId`, `backendId`, `lastUpdated`

4. **New DAO**: `SectorSchemaDao` - Database access for schema metadata cache
   - `getSchemasByArea(areaId, backendId)` - Retrieve cached schemas for an area
   - `insertSchemas(schemas)` - Insert/update cached schemas
   - `deleteSchemasByArea(areaId, backendId)` - Delete schemas for an area

5. **Database Version**: Bumped from 8 to 10 to include new entities

### Cache Utilities
Added to `CacheUtils.kt`:
- `isSchemaBgCacheStale(lastUpdated)` - Checks if background cache is older than 2 weeks
- Existing `isSvgMapCacheStale(lastUpdated)` - Checks if SVG cache is older than 1 week

### Repository Changes
Added to `TopoClimbRepository`:
1. **New Method**: `getAreaSchemasWithCache(areaId, forceRefresh)` 
   - Implements offline-first caching for schema metadata
   - Fetches schemas and caches background images and SVG overlays
   - Returns cached metadata immediately when offline
   - Returns `List<CachedSectorSchema>` with pre-loaded content

2. **Helper Methods**:
   - `fetchSchemaBgWithCache()` - Offline-first caching for background images
   - `fetchSvgOverlayWithCache()` - Offline-first caching for SVG overlays  
   - `downloadImageAsBase64()` - Downloads and converts images to data URIs

### Data Model Changes
1. **New Class**: `CachedSectorSchema`
   - Contains both URLs and cached content
   - Fields: `id`, `name`, `pathsUrl`, `bgUrl`, `pathsContent`, `bgContent`

2. **Extension Function**: `SectorSchema.toCached()` - Converts to cached version

### UI Changes
1. **SchemaView Component**: Simplified to use pre-cached data
   - Removed runtime HTTP fetching
   - Removed local cache management
   - Directly uses `bgContent` and `pathsContent` from `CachedSectorSchema`

2. **AreaDetailViewModel**: Updated to use `CachedSectorSchema`
   - State now holds `List<CachedSectorSchema>` instead of `List<SectorSchema>`
   - `loadAreaData()` calls `getAreaSchemasWithCache()`

3. **AreaDetailScreen**: Removed `schemaCache` state management

## Cache Flow

### Initial Load (No Cache)
1. User opens area with schemas
2. `getAreaSchemasWithCache()` is called
3. Checks database for cached schema metadata
4. If not found, downloads schema metadata from network
5. Stores schema metadata in database
6. For each schema:
   - Checks database for cached background and SVG
   - If not found, downloads from network
   - Stores in database
   - Returns cached data
7. SchemaView renders with cached content

### Subsequent Loads (With Cache)
1. User opens same area again
2. `getAreaSchemasWithCache()` is called  
3. Returns cached schema metadata immediately
4. For each schema:
   - Returns cached content immediately
   - If cache is stale, triggers background refresh
   - Updates database in background
5. SchemaView renders with cached content instantly

### Offline Load
1. User opens area with schemas while offline
2. `getAreaSchemasWithCache()` is called
3. Returns cached schema metadata from database
4. For each schema:
   - Returns cached background and SVG from database
5. SchemaView renders with cached content (no network required)

### Pull-to-Refresh
1. User pulls to refresh
2. `refreshAreaDetails()` passes `forceRefresh=true`
3. `getAreaSchemasWithCache(forceRefresh=true)` is called
4. Returns cached metadata immediately
5. For each schema:
   - Returns cached content immediately
   - Downloads fresh data from network in background (regardless of age)
   - Updates database
5. SchemaView renders with updated content

## TTL Configuration
- **Schema Metadata**: 24 hours (default cache TTL)
- **SVG Overlays**: 1 week (existing, unchanged)
- **Background Images**: 2 weeks (new)

## Testing
Added comprehensive unit tests:
1. **CacheUtilsTest**: Tests all cache TTL logic
   - Tests for each cache type (default, SVG, background)
   - Tests boundary conditions (exact TTL age)
   - Tests for stale/fresh determination

2. **CachedSectorSchemaTest**: Tests data model conversions
   - Tests `SectorSchema.toCached()` conversion
   - Tests with and without content
   - Tests handling of null URLs

## Benefits
1. **Offline Support**: Users can view schemas without internet connection after first online visit
2. **Performance**: Instant display of schemas (no loading time after first fetch)
3. **Reduced Bandwidth**: Images only downloaded once per TTL period
4. **Better UX**: No loading spinners when returning to previously viewed schemas
5. **Reliable**: Schema metadata cached separately ensures offline functionality works correctly

## Bug Fixes
### Offline Schema Loading Error (Fixed in v10)
**Issue**: Schemas failed to load when offline with "unable to resolve host" error, even after being cached.

**Root Cause**: Schema metadata (id, name, paths URL, bg URL) wasn't being cached - only the background images and SVG content were cached. The `getAreaSchemasWithCache()` method always called `api.getAreaSchemas(areaId)` which required network connectivity.

**Fix**: 
- Created `SectorSchemaEntity` to cache schema metadata
- Implemented offline-first pattern for schema metadata retrieval
- When offline, schemas now load from cached metadata
- Background images and SVG overlays load from their respective caches
- All components work together for complete offline functionality

## Future Improvements
- Add cache size limits to prevent database bloat
- Implement cache cleanup on app startup
- Add user setting to clear schema cache manually
- Pre-cache schemas for favorite areas
