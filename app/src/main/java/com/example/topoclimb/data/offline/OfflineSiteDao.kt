package com.example.topoclimb.data.offline

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface OfflineSiteDao {
    
    @Query("SELECT * FROM offline_sites")
    suspend fun getAllSites(): List<OfflineSiteEntity>
    
    @Query("SELECT * FROM offline_sites WHERE id = :siteId")
    suspend fun getSiteById(siteId: Int): OfflineSiteEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSite(site: OfflineSiteEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSites(sites: List<OfflineSiteEntity>)
    
    @Query("DELETE FROM offline_sites WHERE id = :siteId")
    suspend fun deleteSite(siteId: Int)
    
    @Query("DELETE FROM offline_sites")
    suspend fun deleteAllSites()
    
    @Query("SELECT COUNT(*) FROM offline_sites WHERE id = :siteId")
    suspend fun isSiteCached(siteId: Int): Int
}
