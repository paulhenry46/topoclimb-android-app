# Architecture Diagram: SVG Map Feature

## System Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│                        User Interface Layer                          │
├─────────────────────────────────────────────────────────────────────┤
│                                                                       │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │           AreaDetailScreen (Composable)                       │  │
│  │  ┌────────────────────────┐  ┌─────────────────────────────┐ │  │
│  │  │   Area Info Card       │  │   SvgMapView Component      │ │  │
│  │  │  - Name                │  │  - Canvas rendering         │ │  │
│  │  │  - Description         │  │  - Tap gesture detection    │ │  │
│  │  │  - Location            │  │  - Path highlighting        │ │  │
│  │  └────────────────────────┘  └─────────────────────────────┘ │  │
│  │                                                                │  │
│  │  ┌──────────────────────────────────────────────────────────┐ │  │
│  │  │             Route List (Filtered/All)                     │ │  │
│  │  │  - RouteItem 1                                            │ │  │
│  │  │  - RouteItem 2                                            │ │  │
│  │  │  - RouteItem 3                                            │ │  │
│  │  └──────────────────────────────────────────────────────────┘ │  │
│  └──────────────────────────────────────────────────────────────┘  │
│                                                                       │
└───────────────────────────────┬───────────────────────────────────────┘
                                │
                                │ observes UiState
                                ↓
┌─────────────────────────────────────────────────────────────────────┐
│                       ViewModel Layer                                │
├─────────────────────────────────────────────────────────────────────┤
│                                                                       │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │          AreaDetailViewModel                                  │  │
│  │                                                                │  │
│  │  State:                                                        │  │
│  │  - area: Area?                                                │  │
│  │  - routes: List<Route>                                        │  │
│  │  - svgPaths: List<SvgPathData>                                │  │
│  │  - svgDimensions: SvgDimensions?                              │  │
│  │  - selectedSectorId: Int?                                     │  │
│  │  - sectors: List<Sector>                                      │  │
│  │                                                                │  │
│  │  Methods:                                                      │  │
│  │  - loadAreaDetails(areaId)                                    │  │
│  │  - onSectorTapped(sectorId)                                   │  │
│  └──────────────────────────────────────────────────────────────┘  │
│                                                                       │
└───────────────┬───────────────────────┬───────────────────────────────┘
                │                       │
                │ uses                  │ uses
                ↓                       ↓
┌─────────────────────────┐   ┌─────────────────────────────────────┐
│    Utility Layer        │   │      Repository Layer                │
├─────────────────────────┤   ├─────────────────────────────────────┤
│                         │   │                                       │
│  ┌──────────────────┐  │   │  ┌──────────────────────────────┐   │
│  │   SvgParser      │  │   │  │  TopoClimbRepository          │   │
│  │                  │  │   │  │                                │   │
│  │  - parseSvg()    │  │   │  │  - getArea(id)                │   │
│  │  - extract       │  │   │  │  - getRoutesByArea(areaId)    │   │
│  │    Dimensions    │  │   │  │  - getSectorsByArea(areaId)   │   │
│  │  - extract       │  │   │  │  - getLinesBySector(sectorId) │   │
│  │    SectorIds     │  │   │  │  - getRoutesByLine(lineId)    │   │
│  └──────────────────┘  │   │  └──────────────────────────────┘   │
│                         │   │                                       │
└─────────────────────────┘   └───────────────┬───────────────────────┘
                                              │
                                              │ uses
                                              ↓
                              ┌─────────────────────────────────────┐
                              │       Network Layer                 │
                              ├─────────────────────────────────────┤
                              │                                      │
                              │  ┌────────────────────────────────┐ │
                              │  │  TopoClimbApiService (Retrofit)│ │
                              │  │                                 │ │
                              │  │  Endpoints:                     │ │
                              │  │  - GET /areas/{id}              │ │
                              │  │  - GET /areas/{id}/routes       │ │
                              │  │  - GET /areas/{id}/sectors      │ │
                              │  │  - GET /sectors/{id}/lines      │ │
                              │  │  - GET /lines/{id}/routes       │ │
                              │  │  - GET <svg_url>                │ │
                              │  └────────────────────────────────┘ │
                              │                                      │
                              └──────────────────┬───────────────────┘
                                                 │
                                                 │ HTTP requests
                                                 ↓
                              ┌─────────────────────────────────────┐
                              │         TopoClimb API               │
                              └─────────────────────────────────────┘
```

## Data Flow: Loading Area with Interactive Map

```
User navigates to Area
        │
        ↓
AreaDetailScreen.LaunchedEffect
        │
        ↓
AreaDetailViewModel.loadAreaDetails(areaId)
        │
        ├──→ Repository.getArea(areaId)
        │    └──→ API: GET /areas/{id}
        │         └──→ Returns: Area with svgMap URL
        │
        ├──→ Repository.getRoutesByArea(areaId)
        │    └──→ API: GET /areas/{areaId}/routes
        │         └──→ Returns: List<Route>
        │
        ├──→ Repository.getSectorsByArea(areaId)
        │    └──→ API: GET /areas/{areaId}/sectors
        │         └──→ Returns: List<Sector>
        │
        ├──→ HTTP GET area.svgMap (URL)
        │    └──→ Returns: SVG XML content
        │
        └──→ SvgParser.parseSvg(svgContent)
             └──→ Returns: (SvgDimensions, List<SvgPathData>)
        │
        ↓
Update UiState with all data
        │
        ↓
AreaDetailScreen recomposes
        │
        ├──→ SvgMapView renders paths
        │    └──→ Canvas draws all paths in black
        │
        └──→ Route list shows all routes
```

## Data Flow: User Taps on Sector

```
User taps on map
        │
        ↓
SvgMapView.detectTapGestures
        │
        ↓
Calculate tap position in SVG coordinates
        │
        ↓
Find which sector was tapped (hit detection)
        │
        ↓
onPathTapped(sectorId)
        │
        ↓
ViewModel.onSectorTapped(sectorId)
        │
        ├──→ Repository.getLinesBySector(sectorId)
        │    └──→ API: GET /sectors/{sectorId}/lines
        │         └──→ Returns: List<Line>
        │
        └──→ For each line:
             Repository.getRoutesByLine(lineId)
             └──→ API: GET /lines/{lineId}/routes
                  └──→ Returns: List<Route>
        │
        ↓
Combine all routes from all lines
        │
        ↓
Update UiState:
  - selectedSectorId = sectorId
  - routes = filtered routes
        │
        ↓
AreaDetailScreen recomposes
        │
        ├──→ SvgMapView renders:
        │    - Selected path in RED (thicker)
        │    - Other paths in BLACK
        │
        └──→ Route list shows filtered routes
             Title: "Routes in Sector X (count)"
```

## Component Relationships

```
┌─────────────────────────────────────────────────────────┐
│                 AreaDetailScreen                         │
│  ┌────────────────────────────────────────────────┐    │
│  │           viewModel.uiState (State)            │    │
│  │  ┌──────────────────────────────────────────┐ │    │
│  │  │ area, routes, svgPaths, svgDimensions,   │ │    │
│  │  │ selectedSectorId, sectors                 │ │    │
│  │  └──────────────────────────────────────────┘ │    │
│  └────────────────────────────────────────────────┘    │
│                                                          │
│  Components:                                             │
│  ┌────────────────────────────────────────────────┐    │
│  │  SvgMapView(                                   │    │
│  │    svgPaths = uiState.svgPaths,                │    │
│  │    svgDimensions = uiState.svgDimensions,      │    │
│  │    selectedSectorId = uiState.selectedSectorId,│    │
│  │    onPathTapped = { sectorId ->                │    │
│  │      viewModel.onSectorTapped(sectorId)        │    │
│  │    }                                            │    │
│  │  )                                              │    │
│  └────────────────────────────────────────────────┘    │
│                                                          │
│  ┌────────────────────────────────────────────────┐    │
│  │  LazyColumn {                                  │    │
│  │    items(uiState.routes) { route ->            │    │
│  │      RouteItem(route)                          │    │
│  │    }                                            │    │
│  │  }                                              │    │
│  └────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────┘
```

## SVG Parsing Flow

```
SVG XML Content
        │
        ↓
DocumentBuilderFactory.parse()
        │
        ↓
XML Document
        │
        ├──→ Extract <svg> element
        │    └──→ Get viewBox attribute
        │         └──→ Parse to SvgDimensions
        │
        └──→ Get all <path> elements
             │
             For each path:
             │
             ├──→ Get "d" attribute (path data)
             │    └──→ PathParser().parsePathString()
             │         └──→ Convert to Compose Path
             │
             └──→ Get "id" attribute
                  └──→ Extract sector ID using regex
                       Pattern: "sector[_-]?(\\d+)"
                       Examples: sector_123, sector-456, SECTOR_789
             │
             ↓
        SvgPathData(path, sectorId, pathString)
        │
        ↓
Return (SvgDimensions, List<SvgPathData>)
```

## State Management

```
AreaDetailUiState
├── isLoading: Boolean
├── area: Area?
├── routes: List<Route>
├── error: String?
├── svgMapContent: String?
├── svgPaths: List<SvgPathData>
├── svgDimensions: SvgDimensions?
├── selectedSectorId: Int?
└── sectors: List<Sector>

State Transitions:
1. Initial: isLoading = true
2. Loaded: isLoading = false, area/routes/svgPaths populated
3. Sector Selected: selectedSectorId set, routes filtered
4. Sector Deselected: selectedSectorId = null, all routes shown
5. Error: isLoading = false, error message set
```

## File Organization

```
app/src/main/java/com/example/topoclimb/
├── data/
│   ├── Area.kt
│   ├── Route.kt
│   ├── Sector.kt ★ NEW
│   ├── Line.kt ★ NEW
│   ├── SectorsResponse.kt ★ NEW
│   └── LinesResponse.kt ★ NEW
│
├── network/
│   └── TopoClimbApiService.kt ★ MODIFIED
│       (added 3 new endpoints)
│
├── repository/
│   └── TopoClimbRepository.kt ★ MODIFIED
│       (added 3 new methods)
│
├── utils/ ★ NEW
│   └── SvgParser.kt ★ NEW
│
├── ui/
│   ├── components/ ★ NEW
│   │   └── SvgMapView.kt ★ NEW
│   │
│   └── screens/
│       └── AreaDetailScreen.kt ★ MODIFIED
│           (replaced WebView with SvgMapView)
│
└── viewmodel/
    └── AreaDetailViewModel.kt ★ MODIFIED
        (added sector selection logic)
```

## Summary Statistics

- **Files Created**: 6
- **Files Modified**: 6
- **Total Lines Added**: 1,058
- **API Endpoints Added**: 3
- **New Data Models**: 2 (Sector, Line)
- **New UI Components**: 1 (SvgMapView)
- **New Utilities**: 1 (SvgParser)

