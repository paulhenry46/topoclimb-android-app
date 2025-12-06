package com.example.topoclimb.repository

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Repository for managing theme preferences
 * Uses SharedPreferences for persistence
 * Singleton to ensure consistent state across the app
 */
class ThemePreferencesRepository private constructor(context: Context) {
    
    private val sharedPreferences: SharedPreferences = 
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    private val _useOledDark = MutableStateFlow<Boolean>(false)
    val useOledDark: StateFlow<Boolean> = _useOledDark.asStateFlow()
    
    init {
        loadPreferences()
    }
    
    /**
     * Load preferences from SharedPreferences
     */
    private fun loadPreferences() {
        _useOledDark.value = sharedPreferences.getBoolean(KEY_USE_OLED_DARK, false)
    }
    
    /**
     * Set whether to use true OLED dark theme
     */
    fun setUseOledDark(useOled: Boolean) {
        _useOledDark.value = useOled
        sharedPreferences.edit().putBoolean(KEY_USE_OLED_DARK, useOled).apply()
    }
    
    companion object {
        private const val PREFS_NAME = "theme_preferences"
        private const val KEY_USE_OLED_DARK = "use_oled_dark"
        
        @Volatile
        private var INSTANCE: ThemePreferencesRepository? = null
        
        fun getInstance(context: Context): ThemePreferencesRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ThemePreferencesRepository(context).also { INSTANCE = it }
            }
        }
    }
}
