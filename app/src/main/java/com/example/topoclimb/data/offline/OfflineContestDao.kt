package com.example.topoclimb.data.offline

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface OfflineContestDao {
    
    @Query("SELECT * FROM offline_contests WHERE siteId = :siteId")
    suspend fun getContestsBySite(siteId: Int): List<OfflineContestEntity>
    
    @Query("SELECT * FROM offline_contests WHERE id = :contestId")
    suspend fun getContestById(contestId: Int): OfflineContestEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContest(contest: OfflineContestEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContests(contests: List<OfflineContestEntity>)
    
    @Query("DELETE FROM offline_contests WHERE siteId = :siteId")
    suspend fun deleteContestsBySite(siteId: Int)
    
    @Query("DELETE FROM offline_contests")
    suspend fun deleteAllContests()
}
