package com.example.topoclimb.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
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
import com.example.topoclimb.ui.screens.ProfileScreen
import com.example.topoclimb.ui.screens.SiteDetailScreen
import com.example.topoclimb.ui.screens.SitesScreen

sealed class BottomNavItem(
    val route: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val label: String
) {
    object Sites : BottomNavItem("sites", Icons.Default.Place, "Sites")
    object Favorite : BottomNavItem("favorite", Icons.Default.Star, "Favorite")
    object Profile : BottomNavItem("profile", Icons.Default.Person, "You")
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
        BottomNavItem.Favorite,
        BottomNavItem.Profile
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
        composable(BottomNavItem.Sites.route) { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(navController.graph.findStartDestination().id)
            }
            SitesScreen(
                onSiteClick = { backendId, siteId ->
                    navController.navigate("site/$backendId/$siteId")
                },
                viewModel = viewModel(viewModelStoreOwner = parentEntry)
            )
        }
        
        composable(
            route = "site/{backendId}/{siteId}",
            arguments = listOf(
                navArgument("backendId") {
                    type = NavType.StringType
                },
                navArgument("siteId") {
                    type = NavType.IntType
                }
            )
        ) { backStackEntry ->
            val backendId = backStackEntry.arguments?.getString("backendId") ?: return@composable
            val siteId = backStackEntry.arguments?.getInt("siteId") ?: return@composable
            
            SiteDetailScreen(
                backendId = backendId,
                siteId = siteId,
                onBackClick = {
                    navController.popBackStack()
                },
                onAreaClick = { areaBackendId, areaId ->
                    navController.navigate("area/$areaBackendId/$areaId")
                }
            )
        }
        
        composable(
            route = "area/{backendId}/{areaId}",
            arguments = listOf(
                navArgument("backendId") {
                    type = NavType.StringType
                },
                navArgument("areaId") {
                    type = NavType.IntType
                }
            )
        ) { backStackEntry ->
            val backendId = backStackEntry.arguments?.getString("backendId") ?: return@composable
            val areaId = backStackEntry.arguments?.getInt("areaId") ?: return@composable
            
            AreaDetailScreen(
                backendId = backendId,
                areaId = areaId,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(BottomNavItem.Favorite.route) { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(navController.graph.findStartDestination().id)
            }
            SitesScreen(
                onSiteClick = { backendId, siteId ->
                    navController.navigate("site/$backendId/$siteId")
                },
                favoriteOnly = true,
                viewModel = viewModel(viewModelStoreOwner = parentEntry)
            )
        }
        
        composable(BottomNavItem.Profile.route) {
            ProfileScreen()
        }
    }
}
