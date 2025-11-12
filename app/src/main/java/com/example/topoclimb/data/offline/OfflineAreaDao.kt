package com.example.topoclimb.data.offline

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface OfflineAreaDao {
    
    @Query("SELECT * FROM offline_areas WHERE siteId = :siteId")
    suspend fun getAreasBySite(siteId: Int): List<OfflineAreaEntity>
    
    @Query("SELECT * FROM offline_areas WHERE id = :areaId")
    suspend fun getAreaById(areaId: Int): OfflineAreaEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArea(area: OfflineAreaEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAreas(areas: List<OfflineAreaEntity>)
    
    @Query("DELETE FROM offline_areas WHERE siteId = :siteId")
    suspend fun deleteAreasBySite(siteId: Int)
    
    @Query("DELETE FROM offline_areas")
    suspend fun deleteAllAreas()
}
