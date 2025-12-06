package com.example.topoclimb.data

import androidx.compose.runtime.Stable
import com.google.gson.annotations.SerializedName

@Stable
data class Sector(
    val id: Int,
    val name: String,
    val description: String?,
    val areaId: Int,
    @SerializedName("local_id")
    val localId: String?
)
