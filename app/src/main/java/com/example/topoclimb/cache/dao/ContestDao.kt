package com.example.topoclimb.cache.dao

import androidx.room.*
import com.example.topoclimb.cache.entity.ContestEntity

@Dao
interface ContestDao {
    @Query("SELECT * FROM contests WHERE siteId = :siteId AND backendId = :backendId")
    suspend fun getContestsBySite(siteId: Int, backendId: String): List<ContestEntity>

    @Query("SELECT * FROM contests WHERE id = :id AND backendId = :backendId")
    suspend fun getContestById(id: Int, backendId: String): ContestEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContest(contest: ContestEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContests(contests: List<ContestEntity>)

    @Query("DELETE FROM contests WHERE backendId = :backendId")
    suspend fun deleteAllContests(backendId: String)

    @Query("DELETE FROM contests WHERE id = :id AND backendId = :backendId")
    suspend fun deleteContest(id: Int, backendId: String)

    @Query("DELETE FROM contests")
    suspend fun deleteAll()
}
