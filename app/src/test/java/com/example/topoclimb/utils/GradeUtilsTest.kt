package com.example.topoclimb.utils

import com.example.topoclimb.data.GradingSystem
import org.junit.Test
import org.junit.Assert.*

class GradeUtilsTest {
    
    @Test
    fun gradeToPoints_usesGradingSystemWhenAvailable() {
        val gradingSystem = GradingSystem(
            free = false,
            hint = "Test system",
            points = mapOf(
                "3a" to 300,
                "4a" to 400,
                "5a" to 500,
                "6a" to 600,
                "6a+" to 610,
                "6b" to 620,
                "7a" to 700
            )
        )
        
        assertEquals(300, GradeUtils.gradeToPoints("3a", gradingSystem))
        assertEquals(500, GradeUtils.gradeToPoints("5a", gradingSystem))
        assertEquals(610, GradeUtils.gradeToPoints("6a+", gradingSystem))
        assertEquals(700, GradeUtils.gradeToPoints("7a", gradingSystem))
    }
    
    @Test
    fun gradeToPoints_fallsBackToDefaultParsing() {
        // No grading system provided
        val points5a = GradeUtils.gradeToPoints("5a", null)
        val points5b = GradeUtils.gradeToPoints("5b", null)
        val points6a = GradeUtils.gradeToPoints("6a", null)
        
        assertTrue("5a should be less than 5b", points5a < points5b)
        assertTrue("5b should be less than 6a", points5b < points6a)
    }
    
    @Test
    fun gradeToPoints_fallsBackWhenGradeNotInSystem() {
        val gradingSystem = GradingSystem(
            free = false,
            hint = "Limited system",
            points = mapOf(
                "6a" to 600,
                "6b" to 620
            )
        )
        
        // Grade not in system, should fall back to default parsing
        val points5a = GradeUtils.gradeToPoints("5a", gradingSystem)
        val points7a = GradeUtils.gradeToPoints("7a", gradingSystem)
        
        assertTrue("5a should have a value", points5a > 0)
        assertTrue("7a should have a value", points7a > 0)
        assertTrue("5a should be less than 7a", points5a < points7a)
    }
    
    @Test
    fun gradeToPoints_handlesModifiersInDefaultParsing() {
        val points6a = GradeUtils.gradeToPoints("6a", null)
        val points6aPlus = GradeUtils.gradeToPoints("6a+", null)
        val points6b = GradeUtils.gradeToPoints("6b", null)
        
        assertTrue("6a should be less than 6a+", points6a < points6aPlus)
        assertTrue("6a+ should be less than 6b", points6aPlus < points6b)
    }
    
    @Test
    fun matchesGradeRange_worksWithGradingSystem() {
        val gradingSystem = GradingSystem(
            free = false,
            hint = "Test system",
            points = mapOf(
                "5a" to 500,
                "5b" to 520,
                "5c" to 540,
                "6a" to 600,
                "6b" to 620,
                "7a" to 700
            )
        )
        
        // Test with min only
        assertTrue(GradeUtils.matchesGradeRange("6a", "5c", null, gradingSystem))
        assertFalse(GradeUtils.matchesGradeRange("5a", "5c", null, gradingSystem))
        
        // Test with max only
        assertTrue(GradeUtils.matchesGradeRange("5c", null, "6a", gradingSystem))
        assertFalse(GradeUtils.matchesGradeRange("7a", null, "6a", gradingSystem))
        
        // Test with both min and max
        assertTrue(GradeUtils.matchesGradeRange("6a", "5c", "7a", gradingSystem))
        assertFalse(GradeUtils.matchesGradeRange("5a", "5c", "7a", gradingSystem))
    }
    
    @Test
    fun matchesGradeRange_worksWithoutGradingSystem() {
        // Test with min only
        assertTrue(GradeUtils.matchesGradeRange("6a", "5c", null, null))
        assertFalse(GradeUtils.matchesGradeRange("5a", "5c", null, null))
        
        // Test with max only
        assertTrue(GradeUtils.matchesGradeRange("5c", null, "6a", null))
        assertFalse(GradeUtils.matchesGradeRange("7a", null, "6a", null))
        
        // Test with both min and max
        assertTrue(GradeUtils.matchesGradeRange("6a", "5c", "7a", null))
        assertFalse(GradeUtils.matchesGradeRange("5a", "5c", "7a", null))
        assertFalse(GradeUtils.matchesGradeRange("8a", "5c", "7a", null))
    }
    
    @Test
    fun matchesGradeRange_worksWithNoLimits() {
        assertTrue(GradeUtils.matchesGradeRange("6a", null, null, null))
        assertTrue(GradeUtils.matchesGradeRange("3a", null, null, null))
        assertTrue(GradeUtils.matchesGradeRange("9a", null, null, null))
    }
    
    @Test
    fun gradeToPoints_handlesEmptyAndBlankStrings() {
        // Empty string should return 0
        assertEquals(0, GradeUtils.gradeToPoints("", null))
        
        // Blank string should return 0
        assertEquals(0, GradeUtils.gradeToPoints("   ", null))
        
        // Also with grading system
        val gradingSystem = GradingSystem(
            free = false,
            hint = "Test",
            points = mapOf("6a" to 600)
        )
        assertEquals(0, GradeUtils.gradeToPoints("", gradingSystem))
        assertEquals(0, GradeUtils.gradeToPoints("  ", gradingSystem))
    }
}
