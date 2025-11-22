package com.example.topoclimb.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.topoclimb.database.entities.ContestStepEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ContestStepDao {
    @Query("SELECT * FROM contest_steps WHERE contestId = :contestId AND backendId = :backendId ORDER BY id ASC")
    fun getContestStepsFlow(contestId: Int, backendId: String): Flow<List<ContestStepEntity>>
    
    @Query("SELECT * FROM contest_steps WHERE contestId = :contestId AND backendId = :backendId ORDER BY id ASC")
    suspend fun getContestSteps(contestId: Int, backendId: String): List<ContestStepEntity>
    
    @Query("SELECT * FROM contest_steps WHERE id = :stepId AND contestId = :contestId AND backendId = :backendId")
    suspend fun getContestStep(stepId: Int, contestId: Int, backendId: String): ContestStepEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSteps(steps: List<ContestStepEntity>)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStep(step: ContestStepEntity)
    
    @Query("DELETE FROM contest_steps WHERE contestId = :contestId AND backendId = :backendId")
    suspend fun deleteContestSteps(contestId: Int, backendId: String)
    
    @Query("DELETE FROM contest_steps WHERE backendId = :backendId")
    suspend fun deleteAllSteps(backendId: String)
}
