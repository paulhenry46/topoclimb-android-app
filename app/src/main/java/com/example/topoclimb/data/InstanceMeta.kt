package com.example.topoclimb.data

import com.google.gson.annotations.SerializedName

/**
 * Metadata about a TopoClimb instance from the /meta endpoint
 */
data class InstanceMeta(
    val name: String,
    val description: String,
    val version: String,
    @SerializedName("picture_url")
    val pictureUrl: String?
)
