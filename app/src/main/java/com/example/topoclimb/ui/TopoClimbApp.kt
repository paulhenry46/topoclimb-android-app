package com.example.topoclimb.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.topoclimb.ui.screens.AreaDetailScreen
import com.example.topoclimb.ui.screens.AreasScreen
import com.example.topoclimb.ui.screens.RoutesScreen
import com.example.topoclimb.ui.screens.SiteDetailScreen
import com.example.topoclimb.ui.screens.SitesScreen

sealed class BottomNavItem(
    val route: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val label: String
) {
    object Sites : BottomNavItem("sites", Icons.Default.Place, "Sites")
    object AllRoutes : BottomNavItem("routes/all", Icons.AutoMirrored.Filled.List, "Routes")
    object Areas : BottomNavItem("areas", Icons.Default.Home, "Areas")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopoClimbApp() {
    val navController = rememberNavController()
    
    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController)
        }
    ) { innerPadding ->
        NavigationGraph(
            navController = navController,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val items = listOf(
        BottomNavItem.Sites,
        BottomNavItem.AllRoutes,
        BottomNavItem.Areas
    )
    
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                onClick = {
                    navController.navigate(item.route) {
                        // Pop up to the start destination of the graph to
                        // avoid building up a large stack of destinations
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        // Avoid multiple copies of the same destination
                        launchSingleTop = true
                        // Restore state when reselecting a previously selected item
                        restoreState = true
                    }
                }
            )
        }
    }
}

@Composable
fun NavigationGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = BottomNavItem.Sites.route,
        modifier = modifier
    ) {
        composable(BottomNavItem.Sites.route) {
            SitesScreen(
                onSiteClick = { siteId ->
                    navController.navigate("site/$siteId")
                }
            )
        }
        
        composable(
            route = "site/{siteId}",
            arguments = listOf(
                navArgument("siteId") {
                    type = NavType.IntType
                }
            )
        ) { backStackEntry ->
            val siteId = backStackEntry.arguments?.getInt("siteId") ?: return@composable
            
            SiteDetailScreen(
                siteId = siteId,
                onBackClick = {
                    navController.popBackStack()
                },
                onAreaClick = { areaId ->
                    navController.navigate("area/$areaId")
                }
            )
        }
        
        composable(
            route = "area/{areaId}",
            arguments = listOf(
                navArgument("areaId") {
                    type = NavType.IntType
                }
            )
        ) { backStackEntry ->
            val areaId = backStackEntry.arguments?.getInt("areaId") ?: return@composable
            
            AreaDetailScreen(
                areaId = areaId,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(
            route = "routes/{siteId}",
            arguments = listOf(
                navArgument("siteId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val siteIdString = backStackEntry.arguments?.getString("siteId")
            val siteId = if (siteIdString == "all") null else siteIdString?.toIntOrNull()
            
            RoutesScreen(
                siteId = siteId,
                onRouteClick = { routeId ->
                    // Can add route detail screen here in the future
                }
            )
        }
        
        composable(BottomNavItem.Areas.route) {
            AreasScreen()
        }
    }
}
