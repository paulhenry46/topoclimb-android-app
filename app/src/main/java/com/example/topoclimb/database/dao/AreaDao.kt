package com.example.topoclimb.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.topoclimb.database.entities.AreaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AreaDao {
    @Query("SELECT * FROM areas WHERE backendId = :backendId ORDER BY name")
    fun getAllAreasFlow(backendId: String): Flow<List<AreaEntity>>
    
    @Query("SELECT * FROM areas WHERE backendId = :backendId ORDER BY name")
    suspend fun getAllAreas(backendId: String): List<AreaEntity>
    
    @Query("SELECT * FROM areas WHERE id = :id AND backendId = :backendId")
    fun getAreaFlow(id: Int, backendId: String): Flow<AreaEntity?>
    
    @Query("SELECT * FROM areas WHERE id = :id AND backendId = :backendId")
    suspend fun getArea(id: Int, backendId: String): AreaEntity?
    
    @Query("SELECT * FROM areas WHERE siteId = :siteId AND backendId = :backendId ORDER BY name")
    fun getAreasBySiteFlow(siteId: Int, backendId: String): Flow<List<AreaEntity>>
    
    @Query("SELECT * FROM areas WHERE siteId = :siteId AND backendId = :backendId ORDER BY name")
    suspend fun getAreasBySite(siteId: Int, backendId: String): List<AreaEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAreas(areas: List<AreaEntity>)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArea(area: AreaEntity)
    
    @Query("DELETE FROM areas WHERE backendId = :backendId")
    suspend fun deleteAllAreas(backendId: String)
}
