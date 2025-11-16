package com.example.topoclimb.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.topoclimb.database.entities.SvgMapCacheEntity

@Dao
interface SvgMapCacheDao {
    @Query("SELECT * FROM svg_map_cache WHERE url = :url")
    suspend fun getSvgMapCache(url: String): SvgMapCacheEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSvgMapCache(svgMapCache: SvgMapCacheEntity)
    
    @Query("DELETE FROM svg_map_cache WHERE url = :url")
    suspend fun deleteSvgMapCache(url: String)
    
    @Query("DELETE FROM svg_map_cache WHERE lastUpdated < :timestamp")
    suspend fun deleteOldCaches(timestamp: Long)
}
