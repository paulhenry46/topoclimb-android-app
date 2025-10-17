package com.example.topoclimb.ui

sealed class Screen(val route: String) {
    object Sites : Screen("sites")
    object Areas : Screen("areas")
    object AreaDetail : Screen("area/{areaId}") {
        fun createRoute(areaId: Int) = "area/$areaId"
    }
}
