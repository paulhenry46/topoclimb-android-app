package com.example.topoclimb.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.topoclimb.database.entities.ContestRankingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ContestRankingDao {
    @Query("SELECT * FROM contest_rankings WHERE contestId = :contestId AND stepId = 0 AND backendId = :backendId ORDER BY rank ASC")
    fun getContestRankingFlow(contestId: Int, backendId: String): Flow<List<ContestRankingEntity>>
    
    @Query("SELECT * FROM contest_rankings WHERE contestId = :contestId AND stepId = 0 AND backendId = :backendId ORDER BY rank ASC")
    suspend fun getContestRanking(contestId: Int, backendId: String): List<ContestRankingEntity>
    
    @Query("SELECT * FROM contest_rankings WHERE contestId = :contestId AND stepId = :stepId AND backendId = :backendId ORDER BY rank ASC")
    fun getStepRankingFlow(contestId: Int, stepId: Int, backendId: String): Flow<List<ContestRankingEntity>>
    
    @Query("SELECT * FROM contest_rankings WHERE contestId = :contestId AND stepId = :stepId AND backendId = :backendId ORDER BY rank ASC")
    suspend fun getStepRanking(contestId: Int, stepId: Int, backendId: String): List<ContestRankingEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRankings(rankings: List<ContestRankingEntity>)
    
    @Query("DELETE FROM contest_rankings WHERE contestId = :contestId AND stepId = 0 AND backendId = :backendId")
    suspend fun deleteContestRanking(contestId: Int, backendId: String)
    
    @Query("DELETE FROM contest_rankings WHERE contestId = :contestId AND stepId = :stepId AND backendId = :backendId")
    suspend fun deleteStepRanking(contestId: Int, stepId: Int, backendId: String)
    
    @Query("DELETE FROM contest_rankings WHERE backendId = :backendId")
    suspend fun deleteAllRankings(backendId: String)
}
