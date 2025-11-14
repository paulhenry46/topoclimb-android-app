package com.example.topoclimb.cache.dao

import androidx.room.*
import com.example.topoclimb.cache.entity.LineEntity

@Dao
interface LineDao {
    @Query("SELECT * FROM lines WHERE sectorId = :sectorId AND backendId = :backendId")
    suspend fun getLinesBySector(sectorId: Int, backendId: String): List<LineEntity>

    @Query("SELECT * FROM lines WHERE id = :id AND backendId = :backendId")
    suspend fun getLineById(id: Int, backendId: String): LineEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLine(line: LineEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLines(lines: List<LineEntity>)

    @Query("DELETE FROM lines WHERE backendId = :backendId")
    suspend fun deleteAllLines(backendId: String)

    @Query("DELETE FROM lines WHERE id = :id AND backendId = :backendId")
    suspend fun deleteLine(id: Int, backendId: String)

    @Query("DELETE FROM lines")
    suspend fun deleteAll()
}
