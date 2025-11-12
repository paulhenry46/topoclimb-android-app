package com.example.topoclimb.utils

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manager for offline mode settings and state
 */
class OfflineModeManager(context: Context) {
    
    private val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    private val _isOfflineModeEnabled = MutableStateFlow(getOfflineModeEnabled())
    val isOfflineModeEnabled: StateFlow<Boolean> = _isOfflineModeEnabled.asStateFlow()
    
    private val _offlineSites = MutableStateFlow(getOfflineSites())
    val offlineSites: StateFlow<Set<Int>> = _offlineSites.asStateFlow()
    
    /**
     * Enable or disable the offline mode feature globally
     */
    fun setOfflineModeEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_OFFLINE_MODE_ENABLED, enabled).apply()
        _isOfflineModeEnabled.value = enabled
    }
    
    /**
     * Get whether offline mode feature is enabled globally
     */
    fun getOfflineModeEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_OFFLINE_MODE_ENABLED, true)
    }
    
    /**
     * Add a site to offline mode (download its data for offline access)
     */
    fun addOfflineSite(siteId: Int) {
        val currentSites = getOfflineSites().toMutableSet()
        currentSites.add(siteId)
        saveOfflineSites(currentSites)
        _offlineSites.value = currentSites
    }
    
    /**
     * Remove a site from offline mode
     */
    fun removeOfflineSite(siteId: Int) {
        val currentSites = getOfflineSites().toMutableSet()
        currentSites.remove(siteId)
        saveOfflineSites(currentSites)
        _offlineSites.value = currentSites
    }
    
    /**
     * Check if a site is enabled for offline access
     */
    fun isSiteOfflineEnabled(siteId: Int): Boolean {
        return getOfflineSites().contains(siteId)
    }
    
    /**
     * Get all sites that are enabled for offline access
     */
    fun getOfflineSites(): Set<Int> {
        val sitesString = sharedPreferences.getString(KEY_OFFLINE_SITES, "") ?: ""
        return if (sitesString.isEmpty()) {
            emptySet()
        } else {
            sitesString.split(",").mapNotNull { it.toIntOrNull() }.toSet()
        }
    }
    
    /**
     * Save offline sites to SharedPreferences
     */
    private fun saveOfflineSites(sites: Set<Int>) {
        val sitesString = sites.joinToString(",")
        sharedPreferences.edit().putString(KEY_OFFLINE_SITES, sitesString).apply()
    }
    
    /**
     * Clear all offline sites
     */
    fun clearOfflineSites() {
        sharedPreferences.edit().remove(KEY_OFFLINE_SITES).apply()
        _offlineSites.value = emptySet()
    }
    
    companion object {
        private const val PREFS_NAME = "topoclimb_offline_mode"
        private const val KEY_OFFLINE_MODE_ENABLED = "offline_mode_enabled"
        private const val KEY_OFFLINE_SITES = "offline_sites"
    }
}
