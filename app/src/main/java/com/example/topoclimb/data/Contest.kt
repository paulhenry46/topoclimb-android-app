package com.example.topoclimb.data

import com.google.gson.annotations.SerializedName

data class Contest(
    val id: Int,
    val name: String,
    val description: String?,
    @SerializedName("site_id") val siteId: Int?,
    @SerializedName("start_date") val startDate: String?,
    @SerializedName("end_date") val endDate: String?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("updated_at") val updatedAt: String?
)
