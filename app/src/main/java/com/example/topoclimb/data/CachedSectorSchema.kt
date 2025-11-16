package com.example.topoclimb.data

/**
 * Cached version of SectorSchema with downloaded content
 * Used to store both the URLs and the actual cached content
 */
data class CachedSectorSchema(
    val id: Int,
    val name: String,
    val pathsUrl: String?,
    val bgUrl: String?,
    val pathsContent: String?,      // Cached SVG overlay content
    val bgContent: String?          // Cached background image content (base64 with data URI)
)

/**
 * Convert SectorSchema to CachedSectorSchema with content
 */
fun SectorSchema.toCached(
    pathsContent: String? = null,
    bgContent: String? = null
): CachedSectorSchema {
    return CachedSectorSchema(
        id = this.id,
        name = this.name,
        pathsUrl = this.paths,
        bgUrl = this.bg,
        pathsContent = pathsContent,
        bgContent = bgContent
    )
}
