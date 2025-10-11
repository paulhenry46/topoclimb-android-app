package com.example.topoclimb.data

import com.google.gson.annotations.SerializedName

data class Site(
    val id: Long,
    val name: String,
    val slug: String,
    val address: String?,
    val description: String?,
    @SerializedName("profile_picture") val profilePicture: String?,
    val banner: String?,
    @SerializedName("default_cotation") val defaultCotation: Int?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("updated_at") val updatedAt: String?
)
