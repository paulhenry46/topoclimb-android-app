package com.example.topoclimb.data

data class Route(
    val id: Int,
    val name: String,
    val grade: String?,
    val type: String?, // e.g., "sport", "trad", "boulder"
    val description: String?,
    val height: Int?,
    val siteId: Int,
    val siteName: String?
)
