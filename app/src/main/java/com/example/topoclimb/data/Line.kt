package com.example.topoclimb.data

import com.google.gson.annotations.SerializedName

data class Line(
    val id: Int,
    val name: String,
    val description: String?,
    val sectorId: Int,
    @SerializedName("local_id")
    val localId: String?
)
