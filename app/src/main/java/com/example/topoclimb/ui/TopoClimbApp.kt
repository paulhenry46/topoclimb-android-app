package com.example.topoclimb.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.topoclimb.ui.components.OfflineModeDialog
import com.example.topoclimb.ui.navigation.NavigationGraph
import com.example.topoclimb.viewmodel.OfflineModeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopoClimbApp() {
    val navController = rememberNavController()
    val context = LocalContext.current
    
    // Get or create OfflineModeViewModel instance
    val offlineModeViewModel: OfflineModeViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return OfflineModeViewModel.getInstance(context.applicationContext as android.app.Application) as T
            }
        }
    )
    val showOfflineDialog by offlineModeViewModel.showOfflineDialog.collectAsState()
    val isOfflineModeActive by offlineModeViewModel.isOfflineModeActive.collectAsState()
    
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
    
    // Show offline dialog when going offline
    LaunchedEffect(isOfflineModeActive) {
        if (isOfflineModeActive) {
            offlineModeViewModel.showOfflineDialog()
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
        
        // Show offline mode dialog if needed
        if (showOfflineDialog) {
            OfflineModeDialog(
                onDismiss = { offlineModeViewModel.dismissOfflineDialog() },
                onRetryConnection = { offlineModeViewModel.retryConnection() }
            )
        }
    }
}
