package com.example.topoclimb.cache.dao

import androidx.room.*
import com.example.topoclimb.cache.entity.SectorSchemaEntity

@Dao
interface SectorSchemaDao {
    @Query("SELECT * FROM sector_schemas WHERE areaId = :areaId AND backendId = :backendId")
    suspend fun getSchemasByArea(areaId: Int, backendId: String): List<SectorSchemaEntity>

    @Query("SELECT * FROM sector_schemas WHERE id = :id AND backendId = :backendId")
    suspend fun getSchemaById(id: Int, backendId: String): SectorSchemaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchema(schema: SectorSchemaEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchemas(schemas: List<SectorSchemaEntity>)

    @Query("DELETE FROM sector_schemas WHERE backendId = :backendId")
    suspend fun deleteAllSchemas(backendId: String)

    @Query("DELETE FROM sector_schemas WHERE id = :id AND backendId = :backendId")
    suspend fun deleteSchema(id: Int, backendId: String)

    @Query("DELETE FROM sector_schemas")
    suspend fun deleteAll()
}
