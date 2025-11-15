package com.example.topoclimb.ui.state

/**
 * View mode for area detail screen - controls how routes are displayed
 */
enum class ViewMode {
    MAP,      // Traditional interactive map view
    SCHEMA    // Schema view with sector overlays
}

/**
 * Filter for climbed/unclimbed routes
 */
enum class ClimbedFilter {
    ALL,        // Show all routes
    CLIMBED,    // Show only climbed routes
    NOT_CLIMBED // Show only not climbed routes
}

/**
 * Grouping option for route lists
 */
enum class GroupingOption {
    NONE,       // No grouping
    BY_GRADE,   // Group by grade
    BY_SECTOR   // Group by sector
}
