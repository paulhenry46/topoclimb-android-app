package com.example.topoclimb.data

import com.google.gson.annotations.SerializedName

data class SitesResponse(
    @SerializedName("data")
    val data: List<Site>
)