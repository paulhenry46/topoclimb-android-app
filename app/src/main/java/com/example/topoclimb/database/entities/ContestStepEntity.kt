package com.example.topoclimb.database.entities

import androidx.room.Entity
import com.example.topoclimb.data.ContestStep

@Entity(
    tableName = "contest_steps",
    primaryKeys = ["id", "contestId", "backendId"]
)
data class ContestStepEntity(
    val id: Int,
    val contestId: Int,
    val name: String,
    val startTime: String,
    val endTime: String,
    val routes: List<Int>,
    val backendId: String,
    val lastUpdated: Long = System.currentTimeMillis()
)

fun ContestStepEntity.toContestStep(): ContestStep = ContestStep(
    id = id,
    name = name,
    startTime = startTime,
    endTime = endTime,
    routes = routes
)

fun ContestStep.toEntity(contestId: Int, backendId: String): ContestStepEntity = ContestStepEntity(
    id = id,
    contestId = contestId,
    name = name,
    startTime = startTime,
    endTime = endTime,
    routes = routes,
    backendId = backendId
)
