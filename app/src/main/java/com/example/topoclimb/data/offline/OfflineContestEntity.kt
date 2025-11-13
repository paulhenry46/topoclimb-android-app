package com.example.topoclimb.data.offline

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.topoclimb.data.Contest

@Entity(tableName = "offline_contests")
data class OfflineContestEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val description: String?,
    val siteId: Int?,
    val startDate: String?,
    val endDate: String?,
    val createdAt: String?,
    val updatedAt: String?,
    val lastSyncTimestamp: Long = System.currentTimeMillis()
) {
    fun toContest(): Contest {
        return Contest(
            id = id,
            name = name,
            description = description,
            siteId = siteId,
            startDate = startDate,
            endDate = endDate,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
    
    companion object {
        fun fromContest(contest: Contest): OfflineContestEntity {
            return OfflineContestEntity(
                id = contest.id,
                name = contest.name,
                description = contest.description,
                siteId = contest.siteId,
                startDate = contest.startDate,
                endDate = contest.endDate,
                createdAt = contest.createdAt,
                updatedAt = contest.updatedAt
            )
        }
    }
}
