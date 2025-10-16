package com.example.topoclimb.data

import com.google.gson.annotations.SerializedName

data class Line(
    val id: Int,
    val name: String,
    val description: String?,
    val sectorId: Int
)

data class LineResponse(
    @SerializedName("data")
    val data: Line
)

data class LinesResponse(
    @SerializedName("data")
    val data: List<Line>
)
