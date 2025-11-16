package com.example.topoclimb.utils

import java.util.concurrent.TimeUnit

object CacheUtils {
    /**
     * Default cache TTL (Time To Live) - 24 hours in milliseconds
     */
    private val CACHE_TTL_MS = TimeUnit.HOURS.toMillis(24)
    
    /**
     * SVG map cache TTL - 1 week in milliseconds
     * SVG maps don't change frequently, so we cache them longer
     */
    private val SVG_MAP_CACHE_TTL_MS = TimeUnit.DAYS.toMillis(7)
    
    /**
     * Check if cached data is stale (older than TTL)
     * @param lastUpdated Timestamp when data was last updated (in milliseconds)
     * @return true if data is older than 24 hours, false otherwise
     */
    fun isCacheStale(lastUpdated: Long): Boolean {
        val currentTime = System.currentTimeMillis()
        val cacheAge = currentTime - lastUpdated
        return cacheAge > CACHE_TTL_MS
    }
    
    /**
     * Check if SVG map cache is stale (older than 1 week)
     * @param lastUpdated Timestamp when data was last updated (in milliseconds)
     * @return true if data is older than 1 week, false otherwise
     */
    fun isSvgMapCacheStale(lastUpdated: Long): Boolean {
        val currentTime = System.currentTimeMillis()
        val cacheAge = currentTime - lastUpdated
        return cacheAge > SVG_MAP_CACHE_TTL_MS
    }
    
    /**
     * Check if any item in the list has stale cache
     * @param items List of items with lastUpdated timestamp
     * @param getLastUpdated Function to extract lastUpdated from each item
     * @return true if any item is stale, false if all are fresh
     */
    fun <T> isAnyCacheStale(items: List<T>, getLastUpdated: (T) -> Long): Boolean {
        if (items.isEmpty()) return true // Empty cache is considered stale
        return items.any { isCacheStale(getLastUpdated(it)) }
    }
}
