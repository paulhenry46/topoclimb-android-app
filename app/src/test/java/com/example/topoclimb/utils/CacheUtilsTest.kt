package com.example.topoclimb.utils

import org.junit.Test
import org.junit.Assert.*
import java.util.concurrent.TimeUnit

class CacheUtilsTest {
    
    @Test
    fun isCacheStale_returnsFalseForFreshCache() {
        val now = System.currentTimeMillis()
        val oneHourAgo = now - TimeUnit.HOURS.toMillis(1)
        assertFalse("Cache from 1 hour ago should be fresh", CacheUtils.isCacheStale(oneHourAgo))
    }
    
    @Test
    fun isCacheStale_returnsTrueForStaleCache() {
        val now = System.currentTimeMillis()
        val twoDaysAgo = now - TimeUnit.DAYS.toMillis(2)
        assertTrue("Cache from 2 days ago should be stale", CacheUtils.isCacheStale(twoDaysAgo))
    }
    
    @Test
    fun isCacheStale_returnsFalseForCacheExactly24Hours() {
        val now = System.currentTimeMillis()
        val exactlyOneDayAgo = now - TimeUnit.HOURS.toMillis(24)
        // Should be false because we check if age > TTL, not >=
        assertFalse("Cache exactly 24 hours old should still be fresh", CacheUtils.isCacheStale(exactlyOneDayAgo))
    }
    
    @Test
    fun isSvgMapCacheStale_returnsFalseForFreshCache() {
        val now = System.currentTimeMillis()
        val threeDaysAgo = now - TimeUnit.DAYS.toMillis(3)
        assertFalse("SVG cache from 3 days ago should be fresh", CacheUtils.isSvgMapCacheStale(threeDaysAgo))
    }
    
    @Test
    fun isSvgMapCacheStale_returnsTrueForStaleCache() {
        val now = System.currentTimeMillis()
        val eightDaysAgo = now - TimeUnit.DAYS.toMillis(8)
        assertTrue("SVG cache from 8 days ago should be stale", CacheUtils.isSvgMapCacheStale(eightDaysAgo))
    }
    
    @Test
    fun isSvgMapCacheStale_returnsFalseForCacheExactlyOneWeek() {
        val now = System.currentTimeMillis()
        val exactlyOneWeekAgo = now - TimeUnit.DAYS.toMillis(7)
        assertFalse("SVG cache exactly 7 days old should still be fresh", CacheUtils.isSvgMapCacheStale(exactlyOneWeekAgo))
    }
    
    @Test
    fun isSchemaBgCacheStale_returnsFalseForFreshCache() {
        val now = System.currentTimeMillis()
        val sevenDaysAgo = now - TimeUnit.DAYS.toMillis(7)
        assertFalse("Schema background cache from 7 days ago should be fresh", CacheUtils.isSchemaBgCacheStale(sevenDaysAgo))
    }
    
    @Test
    fun isSchemaBgCacheStale_returnsTrueForStaleCache() {
        val now = System.currentTimeMillis()
        val fifteenDaysAgo = now - TimeUnit.DAYS.toMillis(15)
        assertTrue("Schema background cache from 15 days ago should be stale", CacheUtils.isSchemaBgCacheStale(fifteenDaysAgo))
    }
    
    @Test
    fun isSchemaBgCacheStale_returnsFalseForCacheExactlyTwoWeeks() {
        val now = System.currentTimeMillis()
        val exactlyTwoWeeksAgo = now - TimeUnit.DAYS.toMillis(14)
        assertFalse("Schema background cache exactly 14 days old should still be fresh", CacheUtils.isSchemaBgCacheStale(exactlyTwoWeeksAgo))
    }
    
    @Test
    fun isAnyCacheStale_returnsTrueForEmptyList() {
        assertTrue("Empty list should be considered stale", 
            CacheUtils.isAnyCacheStale(emptyList<Long>()) { it })
    }
    
    @Test
    fun isAnyCacheStale_returnsTrueIfAnyItemIsStale() {
        val now = System.currentTimeMillis()
        val items = listOf(
            now - TimeUnit.HOURS.toMillis(1),  // Fresh
            now - TimeUnit.DAYS.toMillis(2),   // Stale
            now - TimeUnit.HOURS.toMillis(12)  // Fresh
        )
        assertTrue("Should return true if any item is stale", 
            CacheUtils.isAnyCacheStale(items) { it })
    }
    
    @Test
    fun isAnyCacheStale_returnsFalseIfAllItemsAreFresh() {
        val now = System.currentTimeMillis()
        val items = listOf(
            now - TimeUnit.HOURS.toMillis(1),
            now - TimeUnit.HOURS.toMillis(5),
            now - TimeUnit.HOURS.toMillis(12)
        )
        assertFalse("Should return false if all items are fresh", 
            CacheUtils.isAnyCacheStale(items) { it })
    }
}
