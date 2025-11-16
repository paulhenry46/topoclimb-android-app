package com.example.topoclimb.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.topoclimb.database.entities.SchemaBgCacheEntity

@Dao
interface SchemaBgCacheDao {
    @Query("SELECT * FROM schema_bg_cache WHERE url = :url")
    suspend fun getSchemaBgCache(url: String): SchemaBgCacheEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchemaBgCache(cache: SchemaBgCacheEntity)
    
    @Query("DELETE FROM schema_bg_cache WHERE url = :url")
    suspend fun deleteSchemaBgCache(url: String)
    
    @Query("DELETE FROM schema_bg_cache WHERE lastUpdated < :timestamp")
    suspend fun deleteOldCaches(timestamp: Long)
}
