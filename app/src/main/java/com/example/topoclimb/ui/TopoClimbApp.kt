package com.example.topoclimb.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.topoclimb.ui.navigation.NavigationGraph

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
