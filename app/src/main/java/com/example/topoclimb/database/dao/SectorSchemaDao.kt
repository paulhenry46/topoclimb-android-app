package com.example.topoclimb.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.topoclimb.database.entities.SectorSchemaEntity

@Dao
interface SectorSchemaDao {
    @Query("SELECT * FROM sector_schemas WHERE areaId = :areaId AND backendId = :backendId")
    suspend fun getSchemasByArea(areaId: Int, backendId: String): List<SectorSchemaEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchemas(schemas: List<SectorSchemaEntity>)
    
    @Query("DELETE FROM sector_schemas WHERE areaId = :areaId AND backendId = :backendId")
    suspend fun deleteSchemasByArea(areaId: Int, backendId: String)
}
