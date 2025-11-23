package com.example.topoclimb.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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
import com.example.topoclimb.ui.screens.ContestDetailScreen
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
                viewModel = viewModel(viewModelStoreOwner = parentEntry),
                onManageInstancesClick = {
                    navController.navigate("backends")
                }
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
                },
                onContestClick = { contestBackendId, contestId ->
                    navController.navigate("site/$contestBackendId/$siteId/contest/$contestId")
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
            
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(navController.graph.findStartDestination().id)
            }
            
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
                },
                favoriteRoutesViewModel = viewModel(viewModelStoreOwner = parentEntry)
            )
        }
        
        composable(
            route = "site/{backendId}/{siteId}/contest/{contestId}",
            arguments = listOf(
                navArgument("backendId") {
                    type = NavType.StringType
                },
                navArgument("siteId") {
                    type = NavType.IntType
                },
                navArgument("contestId") {
                    type = NavType.IntType
                }
            )
        ) @OptIn(ExperimentalMaterial3Api::class) { backStackEntry ->
            val backendId = backStackEntry.arguments?.getString("backendId") ?: return@composable
            val siteId = backStackEntry.arguments?.getInt("siteId") ?: return@composable
            val contestId = backStackEntry.arguments?.getInt("contestId") ?: return@composable
            
            // Get the site detail view model to access contest data
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry("site/$backendId/$siteId")
            }
            val siteDetailViewModel: com.example.topoclimb.viewmodel.SiteDetailViewModel = viewModel(
                viewModelStoreOwner = parentEntry
            )
            val siteDetailUiState by siteDetailViewModel.uiState.collectAsState()
            
            // Find the contest in the cached data
            val contest = siteDetailUiState.contests.find { it.data.id == contestId }?.data
            
            if (contest != null) {
                ContestDetailScreen(
                    backendId = backendId,
                    contestId = contestId,
                    contest = contest,
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onStepRoutesClick = { stepId, routeIds ->
                        // Navigate to area with filter by step routes
                        // For now, just pop back - we'll implement route filtering next
                        navController.popBackStack()
                    }
                )
            } else {
                // Fallback if contest not found
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("Contest") },
                            navigationIcon = {
                                IconButton(onClick = { navController.popBackStack() }) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back"
                                    )
                                }
                            }
                        )
                    }
                ) { padding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Contest not found")
                    }
                }
            }
        }
        
        composable(BottomNavItem.Favorite.route) { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(navController.graph.findStartDestination().id)
            }
            FavoritesScreen(
                onSiteClick = { backendId, siteId ->
                    navController.navigate("site/$backendId/$siteId")
                },
                viewModel = viewModel(viewModelStoreOwner = parentEntry),
                favoriteRoutesViewModel = viewModel(viewModelStoreOwner = parentEntry)
            )
        }
        
        composable(BottomNavItem.Profile.route) {
            ProfileScreen(
                onManageBackendsClick = {
                    navController.navigate("backends")
                },
                onNavigateToQRCode = { backendId ->
                    navController.navigate("qrcode/$backendId")
                },
                onNavigateToFriends = {
                    navController.navigate("friends")
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
                onRegisterClick = {
                    navController.navigate("register/$backendId/$backendName")
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
        
        composable(
            route = "register/{backendId}/{backendName}",
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
            
            var showRegisterSuccess by remember { mutableStateOf(false) }
            var registeredUserName by remember { mutableStateOf("") }
            
            // Fetch instance metadata for logo
            LaunchedEffect(backendId) {
                val backend = uiState.backends.find { it.id == backendId }
                backend?.let {
                    viewModel.fetchInstanceMeta(it.baseUrl)
                }
            }
            
            LaunchedEffect(uiState.successMessage) {
                if (uiState.successMessage?.contains("registered") == true) {
                    // Extract user name from success message
                    val userName = uiState.successMessage?.substringAfter("as ")?.trim() ?: "User"
                    registeredUserName = userName
                    showRegisterSuccess = true
                }
            }
            
            com.example.topoclimb.ui.screens.RegisterScreen(
                backendName = backendName,
                onBackClick = {
                    navController.popBackStack()
                },
                onRegisterClick = { name, email, password ->
                    viewModel.register(backendId, name, email, password)
                },
                isLoading = uiState.registerInProgress,
                error = uiState.registerError,
                logoUrl = uiState.instanceMeta?.pictureUrl
            )
            
            // Show register success screen
            if (showRegisterSuccess) {
                com.example.topoclimb.ui.components.LoginSuccessScreen(
                    onDismiss = {
                        showRegisterSuccess = false
                        navController.popBackStack()
                        navController.popBackStack()
                    },
                    userName = registeredUserName
                )
            }
        }
        
        composable(
            route = "qrcode/{backendId}",
            arguments = listOf(
                navArgument("backendId") {
                    type = NavType.StringType
                }
            )
        ) @OptIn(ExperimentalMaterial3Api::class) { backStackEntry ->
            val backendId = backStackEntry.arguments?.getString("backendId") ?: return@composable
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(BottomNavItem.Profile.route)
            }
            val viewModel: com.example.topoclimb.viewmodel.ProfileViewModel = viewModel(
                viewModelStoreOwner = parentEntry
            )
            val uiState by viewModel.uiState.collectAsState()
            
            // Fetch QR code when screen is displayed
            LaunchedEffect(backendId) {
                viewModel.fetchQRCode(backendId)
            }
            
            // Clear QR code when leaving the screen
            DisposableEffect(Unit) {
                onDispose {
                    viewModel.clearQRCode()
                }
            }
            
            // Get backend info
            val backend = uiState.authenticatedBackends.find { it.id == backendId }
            
            if (backend != null && backend.user != null) {
                com.example.topoclimb.ui.screens.QRCodeScreen(
                    userName = backend.user.name,
                    userGender = backend.user.gender,
                    userBirthDate = backend.user.birthDate,
                    instanceName = backend.name,
                    qrCodeUrl = uiState.qrCodeUrl,
                    isLoading = uiState.isLoadingQRCode,
                    error = uiState.qrCodeError,
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            } else {
                // Fallback if backend not found
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("QR Code") },
                            navigationIcon = {
                                IconButton(onClick = { navController.popBackStack() }) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back"
                                    )
                                }
                            }
                        )
                    }
                ) { padding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Instance not found or not authenticated")
                    }
                }
            }
        }
        
        composable("friends") {
            com.example.topoclimb.ui.screens.FriendsScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}
