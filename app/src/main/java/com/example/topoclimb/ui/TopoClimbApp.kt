package com.example.topoclimb.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import com.example.topoclimb.ui.screens.LogRouteStep1Screen
import com.example.topoclimb.ui.screens.LogRouteStep2Screen
import com.example.topoclimb.ui.screens.LogRouteStep3Screen
import com.example.topoclimb.ui.screens.ProfileScreen
import com.example.topoclimb.ui.screens.SiteDetailScreen
import com.example.topoclimb.ui.screens.SitesScreen
import kotlinx.coroutines.launch

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
    val context = androidx.compose.ui.platform.LocalContext.current
    
    // Load user's logged routes when app starts
    LaunchedEffect(Unit) {
        try {
            val repository = com.example.topoclimb.repository.BackendConfigRepository(context)
            val authToken = repository.getDefaultBackend()?.authToken
            if (authToken != null) {
                val response = com.example.topoclimb.network.RetrofitInstance.api.getUserLogs("Bearer $authToken")
                com.example.topoclimb.viewmodel.RouteDetailViewModel.updateSharedLoggedRoutes(response.data.toSet())
            }
        } catch (e: Exception) {
            // Silently fail - user might not be authenticated
            println("Failed to load user logged routes on app start: ${e.message}")
        }
    }
    
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
    
    // Check if current destination is a detail screen (site or area detail)
    val isOnDetailScreen = currentDestination?.route?.startsWith("site/") == true || 
                          currentDestination?.route?.startsWith("area/") == true
    
    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                onClick = {
                    // If we're on a detail screen and clicking Sites, pop back to Sites instead of restoring state
                    if (isOnDetailScreen && item.route == BottomNavItem.Sites.route) {
                        navController.navigate(item.route) {
                            // Pop all the way back to the Sites screen
                            popUpTo(BottomNavItem.Sites.route) {
                                inclusive = false
                            }
                            launchSingleTop = true
                        }
                    } else {
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
                },
                onStartLogging = { routeId, routeName, routeGrade, areaType ->
                    // Navigate to step 1 of route logging
                    navController.navigate("logRoute/step1/$routeId/$routeName/${routeGrade ?: 0}/${areaType ?: ""}")
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
        
        // Route logging - Step 1: Select climbing type
        composable(
            route = "logRoute/step1/{routeId}/{routeName}/{routeGrade}/{areaType}",
            arguments = listOf(
                navArgument("routeId") { type = NavType.IntType },
                navArgument("routeName") { type = NavType.StringType },
                navArgument("routeGrade") { type = NavType.IntType },
                navArgument("areaType") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val routeId = backStackEntry.arguments?.getInt("routeId") ?: return@composable
            val routeName = backStackEntry.arguments?.getString("routeName") ?: return@composable
            val routeGrade = backStackEntry.arguments?.getInt("routeGrade")?.takeIf { it != 0 }
            val areaType = backStackEntry.arguments?.getString("areaType")?.takeIf { it.isNotEmpty() }
            
            LogRouteStep1Screen(
                routeName = routeName,
                onBackClick = { navController.popBackStack() },
                onTypeSelected = { climbingType ->
                    navController.navigate("logRoute/step2/$routeId/$routeName/${routeGrade ?: 0}/${areaType ?: ""}/$climbingType")
                }
            )
        }
        
        // Route logging - Step 2: Select climbing way
        composable(
            route = "logRoute/step2/{routeId}/{routeName}/{routeGrade}/{areaType}/{climbingType}",
            arguments = listOf(
                navArgument("routeId") { type = NavType.IntType },
                navArgument("routeName") { type = NavType.StringType },
                navArgument("routeGrade") { type = NavType.IntType },
                navArgument("areaType") { type = NavType.StringType },
                navArgument("climbingType") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val routeId = backStackEntry.arguments?.getInt("routeId") ?: return@composable
            val routeName = backStackEntry.arguments?.getString("routeName") ?: return@composable
            val routeGrade = backStackEntry.arguments?.getInt("routeGrade")?.takeIf { it != 0 }
            val areaType = backStackEntry.arguments?.getString("areaType")?.takeIf { it.isNotEmpty() }
            val climbingType = backStackEntry.arguments?.getString("climbingType") ?: return@composable
            
            LogRouteStep2Screen(
                routeName = routeName,
                areaType = areaType,
                onBackClick = { navController.popBackStack() },
                onWaySelected = { climbingWay ->
                    navController.navigate("logRoute/step3/$routeId/$routeName/${routeGrade ?: 0}/$climbingType/$climbingWay")
                }
            )
        }
        
        // Route logging - Step 3: Enter details and submit
        composable(
            route = "logRoute/step3/{routeId}/{routeName}/{routeGrade}/{climbingType}/{climbingWay}",
            arguments = listOf(
                navArgument("routeId") { type = NavType.IntType },
                navArgument("routeName") { type = NavType.StringType },
                navArgument("routeGrade") { type = NavType.IntType },
                navArgument("climbingType") { type = NavType.StringType },
                navArgument("climbingWay") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val routeId = backStackEntry.arguments?.getInt("routeId") ?: return@composable
            val routeName = backStackEntry.arguments?.getString("routeName") ?: return@composable
            val routeGrade = backStackEntry.arguments?.getInt("routeGrade")?.takeIf { it != 0 }
            val climbingType = backStackEntry.arguments?.getString("climbingType") ?: return@composable
            val climbingWay = backStackEntry.arguments?.getString("climbingWay") ?: return@composable
            
            val context = androidx.compose.ui.platform.LocalContext.current
            val repository = remember { com.example.topoclimb.repository.BackendConfigRepository(context) }
            val coroutineScope = rememberCoroutineScope()
            var isLoading by remember { mutableStateOf(false) }
            var error by remember { mutableStateOf<String?>(null) }
            var showHeroMoment by remember { mutableStateOf(false) }
            
            LogRouteStep3Screen(
                routeName = routeName,
                routeGrade = routeGrade,
                gradingSystem = null, // Will use default grade parsing
                onBackClick = { navController.popBackStack() },
                onSubmit = { grade, comment, videoUrl ->
                    isLoading = true
                    error = null
                    
                    // Create the log
                    coroutineScope.launch {
                        try {
                            val backend = repository.getDefaultBackend()
                            if (backend?.authToken != null) {
                                val request = com.example.topoclimb.data.CreateLogRequest(
                                    grade = grade,
                                    type = climbingType,
                                    way = climbingWay,
                                    comment = comment,
                                    videoUrl = videoUrl
                                )
                                com.example.topoclimb.network.RetrofitInstance.api.createRouteLog(
                                    routeId = routeId,
                                    request = request,
                                    authToken = "Bearer ${backend.authToken}"
                                )
                                
                                // Update shared logged routes
                                val response = com.example.topoclimb.network.RetrofitInstance.api.getUserLogs("Bearer ${backend.authToken}")
                                com.example.topoclimb.viewmodel.RouteDetailViewModel.updateSharedLoggedRoutes(response.data.toSet())
                                
                                // Show hero moment and navigate back
                                isLoading = false
                                showHeroMoment = true
                            } else {
                                isLoading = false
                                error = "Not authenticated"
                            }
                        } catch (e: Exception) {
                            isLoading = false
                            error = e.message ?: "Failed to create log"
                        }
                    }
                },
                isLoading = isLoading,
                error = error
            )
            
            // Show hero moment screen
            if (showHeroMoment) {
                com.example.topoclimb.ui.components.HeroMomentScreen(
                    onDismiss = {
                        showHeroMoment = false
                        // Navigate back to the area that we came from
                        // Pop back multiple times to get to the area detail screen
                        var poppedCount = 0
                        while (poppedCount < 3 && navController.popBackStack()) {
                            poppedCount++
                        }
                    },
                    routeName = routeName,
                    routeColor = null // We don't have the route color here
                )
            }
        }
        
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
            val viewModel: com.example.topoclimb.viewmodel.BackendManagementViewModel = viewModel(
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
