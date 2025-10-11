package com.example.topoclimb.ui

sealed class Screen(val route: String) {
    object Sites : Screen("sites")
    object Routes : Screen("routes/{siteId}") {
        fun createRoute(siteId: Int) = "routes/$siteId"
    }
    object RouteDetail : Screen("route/{routeId}") {
        fun createRoute(routeId: Int) = "route/$routeId"
    }
    object Areas : Screen("areas")
}
