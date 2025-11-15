package com.example.topoclimb.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.topoclimb.database.entities.SiteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SiteDao {
    @Query("SELECT * FROM sites WHERE backendId = :backendId ORDER BY name")
    fun getAllSitesFlow(backendId: String): Flow<List<SiteEntity>>
    
    @Query("SELECT * FROM sites WHERE backendId = :backendId ORDER BY name")
    suspend fun getAllSites(backendId: String): List<SiteEntity>
    
    @Query("SELECT * FROM sites WHERE id = :id AND backendId = :backendId")
    fun getSiteFlow(id: Int, backendId: String): Flow<SiteEntity?>
    
    @Query("SELECT * FROM sites WHERE id = :id AND backendId = :backendId")
    suspend fun getSite(id: Int, backendId: String): SiteEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSites(sites: List<SiteEntity>)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSite(site: SiteEntity)
    
    @Query("DELETE FROM sites WHERE backendId = :backendId")
    suspend fun deleteAllSites(backendId: String)
}
