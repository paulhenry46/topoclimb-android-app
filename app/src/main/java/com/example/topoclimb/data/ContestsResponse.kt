package com.example.topoclimb.data

import com.google.gson.annotations.SerializedName

data class ContestsResponse(
    @SerializedName("data")
    val data: List<Contest>
)
