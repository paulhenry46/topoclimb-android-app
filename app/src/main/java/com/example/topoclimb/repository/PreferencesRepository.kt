package com.example.topoclimb.repository

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Repository for managing simple key-value preferences
 * Provides type-safe methods for storing and retrieving data
 */
class PreferencesRepository(
    private val context: Context,
    private val prefsName: String = "app_preferences"
) {
    private val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
    }
    
    private val gson = Gson()
    
    /**
     * Save an integer value
     */
    fun putInt(key: String, value: Int) {
        sharedPreferences.edit().putInt(key, value).apply()
    }
    
    /**
     * Get an integer value
     */
    fun getInt(key: String, defaultValue: Int = 0): Int {
        return sharedPreferences.getInt(key, defaultValue)
    }
    
    /**
     * Save a string value
     */
    fun putString(key: String, value: String) {
        sharedPreferences.edit().putString(key, value).apply()
    }
    
    /**
     * Get a string value
     */
    fun getString(key: String, defaultValue: String? = null): String? {
        return sharedPreferences.getString(key, defaultValue)
    }
    
    /**
     * Save a boolean value
     */
    fun putBoolean(key: String, value: Boolean) {
        sharedPreferences.edit().putBoolean(key, value).apply()
    }
    
    /**
     * Get a boolean value
     */
    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return sharedPreferences.getBoolean(key, defaultValue)
    }
    
    /**
     * Save a long value
     */
    fun putLong(key: String, value: Long) {
        sharedPreferences.edit().putLong(key, value).apply()
    }
    
    /**
     * Get a long value
     */
    fun getLong(key: String, defaultValue: Long = 0L): Long {
        return sharedPreferences.getLong(key, defaultValue)
    }
    
    /**
     * Save any object as JSON
     */
    fun <T> putObject(key: String, value: T) {
        val json = gson.toJson(value)
        putString(key, json)
    }
    
    /**
     * Get an object from JSON
     */
    fun <T> getObject(key: String, type: java.lang.reflect.Type): T? {
        val json = getString(key) ?: return null
        return try {
            gson.fromJson(json, type)
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Save data with timestamp for cache management
     */
    fun <T> putCachedData(dataKey: String, timeKey: String, data: T) {
        sharedPreferences.edit().apply {
            putString(dataKey, gson.toJson(data))
            putLong(timeKey, System.currentTimeMillis())
            apply()
        }
    }
    
    /**
     * Get cached data with its timestamp
     */
    fun <T> getCachedData(dataKey: String, timeKey: String, type: java.lang.reflect.Type): Pair<T?, Long>? {
        val json = getString(dataKey) ?: return null
        val timestamp = getLong(timeKey, 0L)
        
        return try {
            val data: T = gson.fromJson(json, type)
            data to timestamp
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Remove a key from preferences
     */
    fun remove(key: String) {
        sharedPreferences.edit().remove(key).apply()
    }
    
    /**
     * Clear all preferences
     */
    fun clear() {
        sharedPreferences.edit().clear().apply()
    }
    
    /**
     * Check if a key exists
     */
    fun contains(key: String): Boolean {
        return sharedPreferences.contains(key)
    }
}
