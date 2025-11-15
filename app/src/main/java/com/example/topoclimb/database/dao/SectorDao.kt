package com.example.topoclimb.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.topoclimb.database.entities.SectorEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SectorDao {
    @Query("SELECT * FROM sectors WHERE areaId = :areaId AND backendId = :backendId")
    suspend fun getSectorsByArea(areaId: Int, backendId: String): List<SectorEntity>
    
    @Query("SELECT * FROM sectors WHERE areaId = :areaId AND backendId = :backendId")
    fun getSectorsByAreaFlow(areaId: Int, backendId: String): Flow<List<SectorEntity>>
    
    @Query("SELECT * FROM sectors WHERE id = :sectorId AND backendId = :backendId")
    suspend fun getSector(sectorId: Int, backendId: String): SectorEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSectors(sectors: List<SectorEntity>)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSector(sector: SectorEntity)
    
    @Query("DELETE FROM sectors WHERE areaId = :areaId AND backendId = :backendId")
    suspend fun deleteSectorsByArea(areaId: Int, backendId: String)
    
    @Query("DELETE FROM sectors")
    suspend fun deleteAll()
}
