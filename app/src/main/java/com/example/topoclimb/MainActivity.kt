package com.example.topoclimb

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.topoclimb.network.RetrofitInstance
import com.example.topoclimb.ui.TopoClimbApp
import com.example.topoclimb.ui.theme.TopoClimbTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize OkHttp cache for offline-first architecture
        RetrofitInstance.initializeCache(applicationContext)
        
        enableEdgeToEdge()
        setContent {
            TopoClimbTheme {
                TopoClimbApp()
            }
        }
    }
}