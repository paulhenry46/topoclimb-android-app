package com.example.topoclimb.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.topoclimb.database.entities.LogEntity

@Dao
interface LogDao {
    @Query("SELECT * FROM logs WHERE routeId = :routeId AND backendId = :backendId ORDER BY createdAt DESC")
    suspend fun getLogsByRoute(routeId: Int, backendId: String): List<LogEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLogs(logs: List<LogEntity>)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: LogEntity)
    
    @Query("DELETE FROM logs WHERE routeId = :routeId AND backendId = :backendId")
    suspend fun deleteLogsByRoute(routeId: Int, backendId: String)
    
    @Query("DELETE FROM logs WHERE backendId = :backendId")
    suspend fun deleteAllLogs(backendId: String)
}
