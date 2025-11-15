package com.example.topoclimb.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.topoclimb.database.entities.LineEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LineDao {
    @Query("SELECT * FROM lines WHERE sectorId = :sectorId AND backendId = :backendId")
    suspend fun getLinesBySector(sectorId: Int, backendId: String): List<LineEntity>
    
    @Query("SELECT * FROM lines WHERE sectorId = :sectorId AND backendId = :backendId")
    fun getLinesBySectorFlow(sectorId: Int, backendId: String): Flow<List<LineEntity>>
    
    @Query("SELECT * FROM lines WHERE id = :lineId AND backendId = :backendId")
    suspend fun getLine(lineId: Int, backendId: String): LineEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLines(lines: List<LineEntity>)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLine(line: LineEntity)
    
    @Query("DELETE FROM lines WHERE sectorId = :sectorId AND backendId = :backendId")
    suspend fun deleteLinesBySector(sectorId: Int, backendId: String)
    
    @Query("DELETE FROM lines")
    suspend fun deleteAll()
}
