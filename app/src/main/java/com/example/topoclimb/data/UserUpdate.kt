package com.example.topoclimb.data

import com.google.gson.annotations.SerializedName

/**
 * Request body for updating user information
 */
data class UserUpdateRequest(
    val name: String? = null, // Optional: max 255 characters
    @SerializedName("birth_date")
    val birthDate: String? = null, // Optional: date format
    val gender: String? = null // Optional: "male", "female", or "other"
)

/**
 * Response from /user/update endpoint
 */
data class UserUpdateResponse(
    val data: User
)
