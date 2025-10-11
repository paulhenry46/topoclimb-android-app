package com.example.topoclimb.data

import com.google.gson.annotations.SerializedName

data class AreasResponse(
    @SerializedName("data")
    val data: List<Area>
)
