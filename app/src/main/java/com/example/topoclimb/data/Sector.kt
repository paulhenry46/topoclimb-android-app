package com.example.topoclimb.data

import com.google.gson.annotations.SerializedName

data class Sector(
    val id: Int,
    val name: String,
    val description: String?,
    val areaId: Int,
    @SerializedName("local_id")
    val localId: String?
)
