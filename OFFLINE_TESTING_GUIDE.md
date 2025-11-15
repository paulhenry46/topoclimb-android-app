# Offline-First Architecture - Testing Guide

## Overview
This document describes how to test the new offline-first architecture implemented in the TopoClimb Android app.

## What Was Changed
- Added Room database as the single source of truth
- Implemented cache-then-network pattern in FederatedTopoClimbRepository
- Added OkHttp caching for network efficiency
- All Sites, Areas, and Routes are now cached locally

## Manual Testing Scenarios

### Scenario 1: First Launch (No Cache)
**Objective**: Verify app works correctly with no cached data

**Steps**:
1. Clear app data (Settings → Apps → TopoClimb → Clear Data)
2. Launch the app with network enabled
3. Navigate to Sites screen

**Expected Result**:
- App loads sites from network API
- Sites are displayed in the UI
- Sites are cached in Room database

---

### Scenario 2: Second Launch (With Cache, Online)
**Objective**: Verify cached data loads instantly and refreshes in background

**Steps**:
1. Ensure app has cached data (run Scenario 1 first)
2. Close and restart the app with network enabled
3. Navigate to Sites screen

**Expected Result**:
- Cached sites appear **immediately** (no loading spinner needed)
- Fresh data is fetched in background
- If API returns different data, UI updates automatically

---

### Scenario 3: Offline Mode (With Cache)
**Objective**: Verify app works gracefully without network

**Steps**:
1. Ensure app has cached data (run Scenario 1 first)
2. Enable Airplane Mode on device
3. Close and restart the app
4. Navigate to Sites screen
5. Try navigating to a Site detail screen
6. Try viewing Areas and Routes

**Expected Result**:
- All cached data loads correctly from Room
- No error messages about network unavailable
- App remains fully functional with stale data
- No crashes or blank screens

---

### Scenario 4: Offline Mode (No Cache)
**Objective**: Verify app handles no cache + no network gracefully

**Steps**:
1. Clear app data (Settings → Apps → TopoClimb → Clear Data)
2. Enable Airplane Mode
3. Launch the app
4. Navigate to Sites screen

**Expected Result**:
- Empty state is shown (no sites available)
- No crashes
- Appropriate message shown (if any)

---

### Scenario 5: Network Recovery
**Objective**: Verify app refreshes when network becomes available

**Steps**:
1. Start app in offline mode with cached data
2. View Sites screen (shows cached data)
3. Disable Airplane Mode (network becomes available)
4. Pull to refresh or wait a few seconds

**Expected Result**:
- App detects network availability
- Fresh data is fetched from API
- Cache is updated
- UI shows latest data

---

## Database Verification

### Using Android Studio Database Inspector
1. Run the app with debugger attached
2. Open Database Inspector (View → Tool Windows → App Inspection)
3. Select "topoclimb_database"
4. Verify tables exist:
   - `sites`
   - `areas`
   - `routes`
5. Check that data is being inserted correctly

### Using ADB
```bash
# Access device shell
adb shell

# Navigate to app data
cd /data/data/com.example.topoclimb/databases

# Use sqlite3 to inspect
sqlite3 topoclimb_database

# Query tables
.tables
SELECT * FROM sites LIMIT 5;
SELECT * FROM areas LIMIT 5;
SELECT * FROM routes LIMIT 5;
```

---

## Performance Testing

### Cache Hit Performance
- **Metric**: Time to display data from cache
- **Expected**: < 100ms (instant)
- **How to Test**: Use Android Studio Profiler to measure

### Network Fetch Performance
- **Metric**: Time to fetch and cache data
- **Expected**: Same as before (no regression)
- **How to Test**: Use Android Studio Network Profiler

---

## Edge Cases to Test

1. **Slow Network**: Enable network throttling, verify cached data shows immediately
2. **Intermittent Network**: Toggle airplane mode on/off, verify no crashes
3. **Large Dataset**: Test with many sites/routes, verify Room handles it
4. **Concurrent Requests**: Navigate quickly between screens, verify no race conditions
5. **Backend Switch**: Change backend configuration, verify cache updates correctly

---

## Known Limitations

1. **Contests**: Not cached (fetched fresh each time)
2. **Logs**: Not cached (fetched fresh each time)
3. **Sectors/Lines**: Not cached in this implementation (can be added later)
4. **Cache Expiration**: No TTL implemented (cache never expires)
5. **Selective Refresh**: Refreshes all data, not selective

---

## Success Criteria

The offline-first architecture is working correctly if:

✅ App loads cached data instantly (< 100ms)
✅ App works fully offline with cached data
✅ App doesn't crash when network is unavailable
✅ Fresh data is fetched in background when online
✅ UI updates automatically when new data arrives
✅ No regression in app performance
✅ All existing features continue to work

---

## Troubleshooting

### Data not caching
- Check logcat for Room database errors
- Verify `backendId` is correctly set in entities
- Ensure `insertSites/Areas/Routes` is being called

### App crashes offline
- Check for unhandled exceptions in repository
- Verify all network calls are wrapped in try-catch
- Ensure Room queries handle empty results

### Stale data never updates
- Verify `NetworkUtils.isNetworkAvailable()` returns true when online
- Check if background refresh coroutine is launched
- Look for API errors in logcat

---

## Next Steps

After manual testing, consider:
1. Adding integration tests with in-memory Room database
2. Adding UI tests with fake network responses
3. Implementing cache expiration (TTL)
4. Adding pull-to-refresh for manual updates
5. Showing cache age in UI ("Updated 2 hours ago")
