package com.example.topoclimb.cache

import android.content.Context
import android.content.SharedPreferences

/**
 * Manages cache-related preferences
 */
class CachePreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "cache_preferences",
        Context.MODE_PRIVATE
    )

    companion object {
        private const val KEY_CACHE_ENABLED = "cache_enabled"
    }

    var isCacheEnabled: Boolean
        get() = prefs.getBoolean(KEY_CACHE_ENABLED, true) // Default enabled
        set(value) {
            prefs.edit().putBoolean(KEY_CACHE_ENABLED, value).apply()
        }
}
