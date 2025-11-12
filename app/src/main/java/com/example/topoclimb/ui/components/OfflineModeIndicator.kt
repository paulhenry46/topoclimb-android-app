package com.example.topoclimb.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.topoclimb.viewmodel.OfflineModeViewModel

/**
 * Offline mode indicator for TopAppBar
 * Shows a cloud-off icon when the app is in offline mode
 */
@Composable
fun OfflineModeIndicator(
    onClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val offlineModeViewModel: OfflineModeViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return OfflineModeViewModel.getInstance(context.applicationContext as android.app.Application) as T
            }
        }
    )
    val isOfflineMode by offlineModeViewModel.isOfflineModeActive.collectAsState()
    
    if (isOfflineMode) {
        IconButton(
            onClick = onClick ?: { offlineModeViewModel.showOfflineDialog() }
        ) {
            Icon(
                imageVector = Icons.Default.CloudOff,
                contentDescription = "Offline mode",
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}
