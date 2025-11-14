package com.example.topoclimb.cache.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.topoclimb.data.Contest

@Entity(tableName = "contests")
data class ContestEntity(
    @PrimaryKey val id: Int,
    val backendId: String,
    val name: String,
    val description: String?,
    val siteId: Int?,
    val startDate: String?,
    val endDate: String?,
    val createdAt: String?,
    val updatedAt: String?,
    val cachedAt: Long
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
        fun fromContest(contest: Contest, backendId: String): ContestEntity {
            return ContestEntity(
                id = contest.id,
                backendId = backendId,
                name = contest.name,
                description = contest.description,
                siteId = contest.siteId,
                startDate = contest.startDate,
                endDate = contest.endDate,
                createdAt = contest.createdAt,
                updatedAt = contest.updatedAt,
                cachedAt = System.currentTimeMillis()
            )
        }
    }
}
