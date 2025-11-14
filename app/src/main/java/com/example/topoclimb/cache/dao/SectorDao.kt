package com.example.topoclimb.cache.dao

import androidx.room.*
import com.example.topoclimb.cache.entity.SectorEntity

@Dao
interface SectorDao {
    @Query("SELECT * FROM sectors WHERE areaId = :areaId AND backendId = :backendId")
    suspend fun getSectorsByArea(areaId: Int, backendId: String): List<SectorEntity>

    @Query("SELECT * FROM sectors WHERE id = :id AND backendId = :backendId")
    suspend fun getSectorById(id: Int, backendId: String): SectorEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSector(sector: SectorEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSectors(sectors: List<SectorEntity>)

    @Query("DELETE FROM sectors WHERE backendId = :backendId")
    suspend fun deleteAllSectors(backendId: String)

    @Query("DELETE FROM sectors WHERE id = :id AND backendId = :backendId")
    suspend fun deleteSector(id: Int, backendId: String)

    @Query("DELETE FROM sectors")
    suspend fun deleteAll()
}
