package com.example.topoclimb

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.example.topoclimb.network.RetrofitInstance
import com.example.topoclimb.repository.ThemePreferencesRepository
import com.example.topoclimb.ui.TopoClimbApp
import com.example.topoclimb.ui.theme.TopoClimbTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize OkHttp cache for offline-first architecture
        RetrofitInstance.initializeCache(applicationContext)
        
        enableEdgeToEdge()
        setContent {
            val themePreferencesRepository = remember { ThemePreferencesRepository.getInstance(applicationContext) }
            val useOledDark by themePreferencesRepository.useOledDark.collectAsState()
            
            TopoClimbTheme(useOledDark = useOledDark) {
                TopoClimbApp()
            }
        }
    }
}