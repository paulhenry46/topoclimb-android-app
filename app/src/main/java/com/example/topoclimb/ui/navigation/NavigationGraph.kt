package com.example.topoclimb.ui.navigation

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.topoclimb.ui.BottomNavItem
import com.example.topoclimb.ui.screens.AreaDetailScreen
import com.example.topoclimb.ui.screens.FavoritesScreen
import com.example.topoclimb.ui.screens.ProfileScreen
import com.example.topoclimb.ui.screens.SiteDetailScreen
import com.example.topoclimb.ui.screens.SitesScreen
import com.example.topoclimb.viewmodel.BackendManagementViewModel

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
                    navController.navigate("site/$areaBackendId/$siteId/area/$areaId")
                }
            )
        }
        
        composable(
            route = "site/{backendId}/{siteId}/area/{areaId}",
            arguments = listOf(
                navArgument("backendId") {
                    type = NavType.StringType
                },
                navArgument("siteId") {
                    type = NavType.IntType
                },
                navArgument("areaId") {
                    type = NavType.IntType
                }
            )
        ) { backStackEntry ->
            val backendId = backStackEntry.arguments?.getString("backendId") ?: return@composable
            val siteId = backStackEntry.arguments?.getInt("siteId") ?: return@composable
            val areaId = backStackEntry.arguments?.getInt("areaId") ?: return@composable
            
            AreaDetailScreen(
                backendId = backendId,
                siteId = siteId,
                areaId = areaId,
                onBackClick = {
                    navController.popBackStack()
                },
                onStartLogging = { routeId, routeName, routeGrade, areaType ->
                    // Navigate to step 1 of route logging
                    navController.navigate("site/$backendId/$siteId/area/$areaId/logRoute/step1/$routeId/$routeName/${routeGrade ?: 0}/${areaType ?: ""}")
                }
            )
        }
        
        composable(BottomNavItem.Favorite.route) { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(navController.graph.findStartDestination().id)
            }
            FavoritesScreen(
                onSiteClick = { backendId, siteId ->
                    navController.navigate("site/$backendId/$siteId")
                },
                viewModel = viewModel(viewModelStoreOwner = parentEntry)
            )
        }
        
        composable(BottomNavItem.Profile.route) {
            ProfileScreen(
                onManageBackendsClick = {
                    navController.navigate("backends")
                }
            )
        }
        
        composable("backends") {
            com.example.topoclimb.ui.screens.BackendManagementScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onNavigateToLogin = { backendId, backendName ->
                    navController.navigate("login/$backendId/$backendName")
                }
            )
        }
        
        // Add the route logging sub-graph
        logRouteNavGraph(navController)
        
        composable(
            route = "login/{backendId}/{backendName}",
            arguments = listOf(
                navArgument("backendId") {
                    type = NavType.StringType
                },
                navArgument("backendName") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val backendId = backStackEntry.arguments?.getString("backendId") ?: return@composable
            val backendName = backStackEntry.arguments?.getString("backendName") ?: return@composable
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry("backends")
            }
            val viewModel: BackendManagementViewModel = viewModel(
                viewModelStoreOwner = parentEntry
            )
            val uiState by viewModel.uiState.collectAsState()
            
            var showLoginSuccess by remember { mutableStateOf(false) }
            var loggedInUserName by remember { mutableStateOf("") }
            
            // Fetch instance metadata for logo
            LaunchedEffect(backendId) {
                val backend = uiState.backends.find { it.id == backendId }
                backend?.let {
                    viewModel.fetchInstanceMeta(it.baseUrl)
                }
            }
            
            LaunchedEffect(uiState.successMessage) {
                if (uiState.successMessage?.contains("logged in") == true) {
                    // Extract user name from success message
                    val userName = uiState.successMessage?.substringAfter("as ")?.trim() ?: "User"
                    loggedInUserName = userName
                    showLoginSuccess = true
                }
            }
            
            com.example.topoclimb.ui.screens.LoginScreen(
                backendName = backendName,
                onBackClick = {
                    navController.popBackStack()
                },
                onLoginClick = { email, password ->
                    viewModel.login(backendId, email, password)
                },
                isLoading = uiState.loginInProgress,
                error = uiState.loginError,
                logoUrl = uiState.instanceMeta?.pictureUrl
            )
            
            // Show login success screen
            if (showLoginSuccess) {
                com.example.topoclimb.ui.components.LoginSuccessScreen(
                    onDismiss = {
                        showLoginSuccess = false
                        navController.popBackStack()
                    },
                    userName = loggedInUserName
                )
            }
        }
    }
}
