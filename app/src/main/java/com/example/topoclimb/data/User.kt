package com.example.topoclimb.data

import com.google.gson.annotations.SerializedName

/**
 * User data model
 */
data class User(
    val id: Int,
    val name: String,
    val email: String,
    @SerializedName("email_verified_at")
    val emailVerifiedAt: String?,
    @SerializedName("current_team_id")
    val currentTeamId: Int?,
    @SerializedName("profile_photo_path")
    val profilePhotoPath: String?,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String,
    @SerializedName("google_id")
    val googleId: String?,
    @SerializedName("two_factor_confirmed_at")
    val twoFactorConfirmedAt: String?,
    @SerializedName("birth_date")
    val birthDate: String?,
    val gender: String?,
    @SerializedName("profile_photo_url")
    val profilePhotoUrl: String
)

/**
 * Login request body
 */
data class LoginRequest(
    val email: String,
    val password: String
)

/**
 * Auth response from login endpoint
 */
data class AuthResponse(
    val user: User,
    val token: String
)

/**
 * Register request body
 */
data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String
)

/**
 * QR Code response
 */
data class QRCodeResponse(
    val url: String
)
