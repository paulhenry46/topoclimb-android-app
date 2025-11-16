package com.example.topoclimb.data

import org.junit.Test
import org.junit.Assert.*

class CachedSectorSchemaTest {
    
    @Test
    fun toCached_convertsFromSectorSchema() {
        val schema = SectorSchema(
            id = 1,
            name = "Test Sector",
            paths = "https://example.com/paths.svg",
            bg = "https://example.com/bg.jpg"
        )
        
        val cached = schema.toCached()
        
        assertEquals(1, cached.id)
        assertEquals("Test Sector", cached.name)
        assertEquals("https://example.com/paths.svg", cached.pathsUrl)
        assertEquals("https://example.com/bg.jpg", cached.bgUrl)
        assertNull(cached.pathsContent)
        assertNull(cached.bgContent)
    }
    
    @Test
    fun toCached_convertsWithContent() {
        val schema = SectorSchema(
            id = 2,
            name = "Another Sector",
            paths = "https://example.com/paths2.svg",
            bg = "https://example.com/bg2.jpg"
        )
        
        val svgContent = "<svg>...</svg>"
        val bgContent = "data:image/jpeg;base64,/9j/..."
        
        val cached = schema.toCached(
            pathsContent = svgContent,
            bgContent = bgContent
        )
        
        assertEquals(2, cached.id)
        assertEquals("Another Sector", cached.name)
        assertEquals("https://example.com/paths2.svg", cached.pathsUrl)
        assertEquals("https://example.com/bg2.jpg", cached.bgUrl)
        assertEquals(svgContent, cached.pathsContent)
        assertEquals(bgContent, cached.bgContent)
    }
    
    @Test
    fun toCached_handlesNullUrls() {
        val schema = SectorSchema(
            id = 3,
            name = "Incomplete Sector",
            paths = null,
            bg = null
        )
        
        val cached = schema.toCached()
        
        assertEquals(3, cached.id)
        assertEquals("Incomplete Sector", cached.name)
        assertNull(cached.pathsUrl)
        assertNull(cached.bgUrl)
        assertNull(cached.pathsContent)
        assertNull(cached.bgContent)
    }
    
    @Test
    fun cachedSectorSchema_preservesAllFields() {
        val cached = CachedSectorSchema(
            id = 5,
            name = "Full Sector",
            pathsUrl = "https://example.com/full.svg",
            bgUrl = "https://example.com/full.jpg",
            pathsContent = "<svg viewBox='0 0 100 100'></svg>",
            bgContent = "data:image/jpeg;base64,ABC123"
        )
        
        assertEquals(5, cached.id)
        assertEquals("Full Sector", cached.name)
        assertEquals("https://example.com/full.svg", cached.pathsUrl)
        assertEquals("https://example.com/full.jpg", cached.bgUrl)
        assertEquals("<svg viewBox='0 0 100 100'></svg>", cached.pathsContent)
        assertEquals("data:image/jpeg;base64,ABC123", cached.bgContent)
    }
}
