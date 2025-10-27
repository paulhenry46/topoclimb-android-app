package com.example.topoclimb.ui.navigation

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.topoclimb.repository.BackendConfigRepository
import com.example.topoclimb.ui.screens.LogRouteStep1Screen
import com.example.topoclimb.ui.screens.LogRouteStep2Screen
import com.example.topoclimb.ui.screens.LogRouteStep3Screen
import com.example.topoclimb.viewmodel.LogRouteViewModel
import kotlinx.coroutines.launch

/**
 * Navigation sub-graph for the route logging flow
 * This isolates the logging routes and UI state from the main navigation
 */
fun NavGraphBuilder.logRouteNavGraph(
    navController: NavHostController
) {
    // Route logging - Step 1: Select climbing type
    composable(
        route = "site/{backendId}/{siteId}/area/{areaId}/logRoute/step1/{routeId}/{routeName}/{routeGrade}/{areaType}",
        arguments = listOf(
            navArgument("backendId") { type = NavType.StringType },
            navArgument("siteId") { type = NavType.IntType },
            navArgument("areaId") { type = NavType.IntType },
            navArgument("routeId") { type = NavType.IntType },
            navArgument("routeName") { type = NavType.StringType },
            navArgument("routeGrade") { type = NavType.IntType },
            navArgument("areaType") { type = NavType.StringType }
        )
    ) { backStackEntry ->
        val backendId = backStackEntry.arguments?.getString("backendId") ?: return@composable
        val siteId = backStackEntry.arguments?.getInt("siteId") ?: return@composable
        val areaId = backStackEntry.arguments?.getInt("areaId") ?: return@composable
        val routeId = backStackEntry.arguments?.getInt("routeId") ?: return@composable
        val routeName = backStackEntry.arguments?.getString("routeName") ?: return@composable
        val routeGrade = backStackEntry.arguments?.getInt("routeGrade")?.takeIf { it != 0 }
        val areaType = backStackEntry.arguments?.getString("areaType")?.takeIf { it.isNotEmpty() }
        
        LogRouteStep1Screen(
            routeName = routeName,
            onBackClick = { navController.popBackStack() },
            onTypeSelected = { climbingType ->
                navController.navigate("site/$backendId/$siteId/area/$areaId/logRoute/step2/$routeId/$routeName/${routeGrade ?: 0}/${areaType ?: ""}/$climbingType")
            }
        )
    }
    
    // Route logging - Step 2: Select climbing way
    composable(
        route = "site/{backendId}/{siteId}/area/{areaId}/logRoute/step2/{routeId}/{routeName}/{routeGrade}/{areaType}/{climbingType}",
        arguments = listOf(
            navArgument("backendId") { type = NavType.StringType },
            navArgument("siteId") { type = NavType.IntType },
            navArgument("areaId") { type = NavType.IntType },
            navArgument("routeId") { type = NavType.IntType },
            navArgument("routeName") { type = NavType.StringType },
            navArgument("routeGrade") { type = NavType.IntType },
            navArgument("areaType") { type = NavType.StringType },
            navArgument("climbingType") { type = NavType.StringType }
        )
    ) { backStackEntry ->
        val backendId = backStackEntry.arguments?.getString("backendId") ?: return@composable
        val siteId = backStackEntry.arguments?.getInt("siteId") ?: return@composable
        val areaId = backStackEntry.arguments?.getInt("areaId") ?: return@composable
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
                navController.navigate("site/$backendId/$siteId/area/$areaId/logRoute/step3/$routeId/$routeName/${routeGrade ?: 0}/$climbingType/$climbingWay")
            }
        )
    }
    
    // Route logging - Step 3: Enter details and submit
    composable(
        route = "site/{backendId}/{siteId}/area/{areaId}/logRoute/step3/{routeId}/{routeName}/{routeGrade}/{climbingType}/{climbingWay}",
        arguments = listOf(
            navArgument("backendId") { type = NavType.StringType },
            navArgument("siteId") { type = NavType.IntType },
            navArgument("areaId") { type = NavType.IntType },
            navArgument("routeId") { type = NavType.IntType },
            navArgument("routeName") { type = NavType.StringType },
            navArgument("routeGrade") { type = NavType.IntType },
            navArgument("climbingType") { type = NavType.StringType },
            navArgument("climbingWay") { type = NavType.StringType }
        )
    ) { backStackEntry ->
        val backendId = backStackEntry.arguments?.getString("backendId") ?: return@composable
        val siteId = backStackEntry.arguments?.getInt("siteId") ?: return@composable
        val routeId = backStackEntry.arguments?.getInt("routeId") ?: return@composable
        val routeName = backStackEntry.arguments?.getString("routeName") ?: return@composable
        val routeGrade = backStackEntry.arguments?.getInt("routeGrade")?.takeIf { it != 0 }
        val climbingType = backStackEntry.arguments?.getString("climbingType") ?: return@composable
        val climbingWay = backStackEntry.arguments?.getString("climbingWay") ?: return@composable
        
        val context = LocalContext.current
        val viewModel: LogRouteViewModel = viewModel()
        val uiState by viewModel.uiState.collectAsState()
        var showHeroMoment by remember { mutableStateOf(false) }
        
        // Load grading system when screen is first composed
        LaunchedEffect(backendId, siteId) {
            viewModel.loadGradingSystem(backendId, siteId)
        }
        
        // Show hero moment when log is successfully created
        LaunchedEffect(uiState.isSuccess) {
            if (uiState.isSuccess) {
                showHeroMoment = true
                viewModel.resetSuccessState()
            }
        }
        
        LogRouteStep3Screen(
            routeName = routeName,
            routeGrade = routeGrade,
            gradingSystem = uiState.gradingSystem,
            onBackClick = { navController.popBackStack() },
            onSubmit = { grade, comment, videoUrl ->
                viewModel.createRouteLog(
                    routeId = routeId,
                    grade = grade,
                    climbingType = climbingType,
                    climbingWay = climbingWay,
                    comment = comment,
                    videoUrl = videoUrl
                )
            },
            isLoading = uiState.isLoading,
            error = uiState.error
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
}
