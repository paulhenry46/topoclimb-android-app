package com.example.topoclimb.database.entities

import androidx.room.Entity
import com.example.topoclimb.data.ContestRankEntry

@Entity(
    tableName = "contest_rankings",
    primaryKeys = ["contestId", "stepId", "userId", "backendId"]
)
data class ContestRankingEntity(
    val contestId: Int,
    val stepId: Int, // 0 for global contest ranking
    val userId: Int,
    val userName: String,
    val routesCount: Int,
    val totalPoints: Int,
    val rank: Int,
    val backendId: String,
    val lastUpdated: Long = System.currentTimeMillis()
)

fun ContestRankingEntity.toContestRankEntry(): ContestRankEntry = ContestRankEntry(
    userId = userId,
    userName = userName,
    routesCount = routesCount,
    totalPoints = totalPoints,
    rank = rank
)

fun ContestRankEntry.toEntity(
    contestId: Int,
    stepId: Int, // 0 for global contest ranking
    backendId: String
): ContestRankingEntity = ContestRankingEntity(
    contestId = contestId,
    stepId = stepId,
    userId = userId,
    userName = userName,
    routesCount = routesCount,
    totalPoints = totalPoints,
    rank = rank,
    backendId = backendId
)
