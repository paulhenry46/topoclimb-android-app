package com.example.topoclimb.ui.screens

import org.junit.Test
import org.junit.Assert.*

class ProfileScreenTest {
    
    @Test
    fun parseRoutesbyGrade_handlesEmptyList() {
        val result = parseRoutesbyGrade(emptyList<Any>())
        assertTrue("Empty list should return empty map", result.isEmpty())
    }
    
    @Test
    fun parseRoutesbyGrade_handlesNull() {
        val result = parseRoutesbyGrade(null)
        assertTrue("Null should return empty map", result.isEmpty())
    }
    
    @Test
    fun parseRoutesbyGrade_handlesMapWithStringKeys() {
        val input = mapOf(
            "6a" to 2,
            "5c" to 2,
            "4a" to 1
        )
        val result = parseRoutesbyGrade(input)
        
        assertEquals(3, result.size)
        assertEquals(2, result["6a"])
        assertEquals(2, result["5c"])
        assertEquals(1, result["4a"])
    }
    
    @Test
    fun parseRoutesbyGrade_handlesMapWithDoubleValues() {
        val input = mapOf(
            "6a" to 2.0,
            "5c" to 1.0
        )
        val result = parseRoutesbyGrade(input)
        
        assertEquals(2, result.size)
        assertEquals(2, result["6a"])
        assertEquals(1, result["5c"])
    }
    
    @Test
    fun parseRoutesbyGrade_handlesMapWithStringValues() {
        val input = mapOf(
            "6a" to "2",
            "5c" to "3"
        )
        val result = parseRoutesbyGrade(input)
        
        assertEquals(2, result.size)
        assertEquals(2, result["6a"])
        assertEquals(3, result["5c"])
    }
    
    @Test
    fun parseRoutesbyGrade_ignoresInvalidEntries() {
        val input = mapOf(
            "6a" to 2,
            "5c" to "invalid",
            123 to 4, // Invalid key type
            "4a" to 1
        )
        val result = parseRoutesbyGrade(input)
        
        // Should only include valid entries (6a and 4a)
        assertEquals(2, result.size)
        assertEquals(2, result["6a"])
        assertEquals(1, result["4a"])
    }
    
    @Test
    fun parseRoutesbyGrade_handlesUnexpectedType() {
        val result = parseRoutesbyGrade("unexpected string")
        assertTrue("Unexpected type should return empty map", result.isEmpty())
    }
    
    @Test
    fun parseRoutesbyGrade_handlesComplexScenario() {
        // Simulating a real API response
        val input = mapOf(
            "3a" to 1,
            "4a" to 2,
            "5a" to 5,
            "5b" to 3,
            "6a" to 2,
            "6b" to 1
        )
        val result = parseRoutesbyGrade(input)
        
        assertEquals(6, result.size)
        assertEquals(1, result["3a"])
        assertEquals(2, result["4a"])
        assertEquals(5, result["5a"])
        assertEquals(3, result["5b"])
        assertEquals(2, result["6a"])
        assertEquals(1, result["6b"])
    }
}
