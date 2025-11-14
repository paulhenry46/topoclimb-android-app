package com.example.topoclimb.cache.dao

import androidx.room.*
import com.example.topoclimb.cache.entity.AreaEntity

@Dao
interface AreaDao {
    @Query("SELECT * FROM areas WHERE backendId = :backendId")
    suspend fun getAllAreas(backendId: String): List<AreaEntity>

    @Query("SELECT * FROM areas WHERE id = :id AND backendId = :backendId")
    suspend fun getAreaById(id: Int, backendId: String): AreaEntity?

    @Query("SELECT * FROM areas WHERE siteId = :siteId AND backendId = :backendId")
    suspend fun getAreasBySite(siteId: Int, backendId: String): List<AreaEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArea(area: AreaEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAreas(areas: List<AreaEntity>)

    @Query("DELETE FROM areas WHERE backendId = :backendId")
    suspend fun deleteAllAreas(backendId: String)

    @Query("DELETE FROM areas WHERE id = :id AND backendId = :backendId")
    suspend fun deleteArea(id: Int, backendId: String)

    @Query("DELETE FROM areas")
    suspend fun deleteAll()
}
