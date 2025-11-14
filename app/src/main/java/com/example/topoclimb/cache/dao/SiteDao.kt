package com.example.topoclimb.cache.dao

import androidx.room.*
import com.example.topoclimb.cache.entity.SiteEntity

@Dao
interface SiteDao {
    @Query("SELECT * FROM sites WHERE backendId = :backendId")
    suspend fun getAllSites(backendId: String): List<SiteEntity>

    @Query("SELECT * FROM sites WHERE id = :id AND backendId = :backendId")
    suspend fun getSiteById(id: Int, backendId: String): SiteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSite(site: SiteEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSites(sites: List<SiteEntity>)

    @Query("DELETE FROM sites WHERE backendId = :backendId")
    suspend fun deleteAllSites(backendId: String)

    @Query("DELETE FROM sites WHERE id = :id AND backendId = :backendId")
    suspend fun deleteSite(id: Int, backendId: String)

    @Query("DELETE FROM sites")
    suspend fun deleteAll()
}
