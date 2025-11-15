package com.example.topoclimb.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.topoclimb.database.entities.ContestEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ContestDao {
    @Query("SELECT * FROM contests WHERE backendId = :backendId ORDER BY startDate DESC")
    fun getAllContestsFlow(backendId: String): Flow<List<ContestEntity>>
    
    @Query("SELECT * FROM contests WHERE backendId = :backendId ORDER BY startDate DESC")
    suspend fun getAllContests(backendId: String): List<ContestEntity>
    
    @Query("SELECT * FROM contests WHERE id = :id AND backendId = :backendId")
    fun getContestFlow(id: Int, backendId: String): Flow<ContestEntity?>
    
    @Query("SELECT * FROM contests WHERE id = :id AND backendId = :backendId")
    suspend fun getContest(id: Int, backendId: String): ContestEntity?
    
    @Query("SELECT * FROM contests WHERE siteId = :siteId AND backendId = :backendId ORDER BY startDate DESC")
    fun getContestsBySiteFlow(siteId: Int, backendId: String): Flow<List<ContestEntity>>
    
    @Query("SELECT * FROM contests WHERE siteId = :siteId AND backendId = :backendId ORDER BY startDate DESC")
    suspend fun getContestsBySite(siteId: Int, backendId: String): List<ContestEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContests(contests: List<ContestEntity>)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContest(contest: ContestEntity)
    
    @Query("DELETE FROM contests WHERE backendId = :backendId")
    suspend fun deleteAllContests(backendId: String)
}
