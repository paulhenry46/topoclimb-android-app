# TopoClimb API Integration Guide

This document describes how the TopoClimb Android app integrates with the TopoClimb API.

## API Configuration

The app is configured to use a RESTful API. To configure the API endpoint:

1. Open `app/src/main/java/com/example/topoclimb/AppConfig.kt`
2. Update the `API_BASE_URL` constant with your TopoClimb API URL

```kotlin
const val API_BASE_URL = "https://your-topoclimb-api.com/"
```

## Expected API Endpoints

The app expects the following endpoints to be available (based on OpenAPI specification):

### Sites

#### GET /sites
List all climbing sites

**Response:**
```json
[
  {
    "id": 1,
    "name": "Fontainebleau",
    "description": "Famous bouldering area in France",
    "latitude": 48.4044,
    "longitude": 2.6992,
    "imageUrl": "https://example.com/image.jpg"
  }
]
```

#### GET /sites/{id}
Get details of a specific site

**Response:**
```json
{
  "id": 1,
  "name": "Fontainebleau",
  "description": "Famous bouldering area in France",
  "latitude": 48.4044,
  "longitude": 2.6992,
  "imageUrl": "https://example.com/image.jpg"
}
```

### Routes

#### GET /routes
List all routes with optional filters

**Query Parameters:**
- `siteId` (optional): Filter routes by site ID
- `grade` (optional): Filter routes by grade (e.g., "5a", "6b", "7c")
- `type` (optional): Filter routes by type (e.g., "sport", "trad", "boulder")

**Response:**
```json
[
  {
    "id": 1,
    "name": "La Marie-Rose",
    "grade": "7c",
    "type": "boulder",
    "description": "Classic Font problem",
    "height": 5,
    "siteId": 1,
    "siteName": "Fontainebleau",
    "thumbnail": "https://example.com/route-thumb.jpg",
    "color": "#FF5722",
    "line_local_id": "3B",
    "sector_local_id": "Sector A",
    "line_count": 5
  }
]
```

#### GET /routes/{id}
Get details of a specific route

**Response:**
```json
{
  "id": 1,
  "name": "La Marie-Rose",
  "grade": "7c",
  "type": "boulder",
  "description": "Classic Font problem",
  "height": 5,
  "siteId": 1,
  "siteName": "Fontainebleau",
  "thumbnail": "https://example.com/route-thumb.jpg",
  "color": "#FF5722",
  "line_local_id": "3B",
  "sector_local_id": "Sector A",
  "line_count": 5
}
```

### Areas

#### GET /areas
List all climbing areas

**Response:**
```json
[
  {
    "id": 1,
    "name": "Île-de-France",
    "description": "Region around Paris",
    "latitude": 48.8566,
    "longitude": 2.3522,
    "siteId": 1
  }
]
```

#### GET /areas/{id}
Get details of a specific area

**Response:**
```json
{
  "id": 1,
  "name": "Île-de-France",
  "description": "Region around Paris",
  "latitude": 48.8566,
  "longitude": 2.3522,
  "siteId": 1
}
```

## Data Models

The app uses the following Kotlin data classes to represent API responses:

### Site
```kotlin
data class Site(
    val id: Int,
    val name: String,
    val description: String?,
    val latitude: Double?,
    val longitude: Double?,
    val imageUrl: String?
)
```

### Route
```kotlin
data class Route(
    val id: Int,
    val name: String,
    val grade: String?,
    val type: String?,
    val description: String?,
    val height: Int?,
    val siteId: Int,
    val siteName: String?,
    val thumbnail: String?,
    val color: String?,
    val lineLocalId: String?,
    val sectorLocalId: String?,
    val lineCount: Int?
)
```

### Area
```kotlin
data class Area(
    val id: Int,
    val name: String,
    val description: String?,
    val latitude: Double?,
    val longitude: Double?,
    val siteId: Int
)
```

## Error Handling

The app handles the following error scenarios:

- **Network Errors**: Displayed with a retry button
- **API Errors**: Error messages shown to the user
- **Loading States**: Loading indicators shown during API calls

All errors are caught in the Repository layer and returned as `Result<T>` objects to the ViewModels.

## CORS Configuration

If you're testing with a local API, ensure CORS is properly configured to allow requests from the Android app.

## Authentication

This version of the app **does not require authentication** as it only accesses public endpoints. Future versions may add authentication for user-specific features.

## Testing the API Integration

To test the API integration:

1. Set up your TopoClimb API server
2. Update the `API_BASE_URL` in `AppConfig.kt`
3. Ensure the API endpoints match the expected format above
4. Build and run the app
5. Check the Logcat for API request/response logs (when `ENABLE_LOGGING` is true)

## Sample Data

For testing purposes, you can use the following sample data structure in your API:

**Sample Sites:**
```json
[
  {"id": 1, "name": "Fontainebleau", "description": "World-famous bouldering", "latitude": 48.4044, "longitude": 2.6992},
  {"id": 2, "name": "Ceüse", "description": "Legendary sport climbing", "latitude": 44.5167, "longitude": 6.0167}
]
```

**Sample Routes:**
```json
[
  {"id": 1, "name": "La Marie-Rose", "grade": "7c", "type": "boulder", "height": 5, "siteId": 1, "thumbnail": "https://example.com/route1.jpg", "color": "#FF5722", "line_local_id": "3B", "sector_local_id": "A", "line_count": 5},
  {"id": 2, "name": "Biographie", "grade": "9a+", "type": "sport", "height": 45, "siteId": 2, "thumbnail": "https://example.com/route2.jpg", "color": "#2196F3", "line_local_id": "12A", "sector_local_id": "B", "line_count": 8}
]
```

**Sample Areas:**
```json
[
  {"id": 1, "name": "Île-de-France", "description": "Paris region", "siteId": 1},
  {"id": 2, "name": "Provence-Alpes-Côte d'Azur", "description": "Southern Alps", "siteId": 2}
]
```
