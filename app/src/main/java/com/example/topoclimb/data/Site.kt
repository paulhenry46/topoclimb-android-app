package com.example.topoclimb.data

import androidx.compose.runtime.Stable
import com.google.gson.annotations.SerializedName

@Stable
data class Site(
    val id: Int,
    val name: String,
    val description: String?,
    val latitude: Double?,
    val longitude: Double?,
    val imageUrl: String?,
    val slug: String,
    val address: String?,
    @SerializedName("profile_picture") val profilePicture: String?,
    val banner: String?,
    @SerializedName("default_cotation") val defaultCotation: Boolean?,
    @SerializedName("grading_system") val gradingSystem: GradingSystem?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("updated_at") val updatedAt: String?,
    val email: String?,
    val phone: String?,
    val website: String?,
    val coordinates: String?
)
