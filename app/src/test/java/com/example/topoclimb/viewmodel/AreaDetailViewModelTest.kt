package com.example.topoclimb.viewmodel

import org.junit.Test
import org.junit.Assert.*

class AreaDetailViewModelTest {
    
    @Test
    fun parseGrade_handlesBasicGrades() {
        val viewModel = AreaDetailViewModel()
        
        // Use reflection to access the private parseGrade method for testing
        val parseGradeMethod = viewModel.javaClass.getDeclaredMethod("parseGrade", String::class.java)
        parseGradeMethod.isAccessible = true
        
        val grade5a = parseGradeMethod.invoke(viewModel, "5a") as Int
        val grade5b = parseGradeMethod.invoke(viewModel, "5b") as Int
        val grade6a = parseGradeMethod.invoke(viewModel, "6a") as Int
        
        assertTrue("5a should be less than 5b", grade5a < grade5b)
        assertTrue("5b should be less than 6a", grade5b < grade6a)
    }
    
    @Test
    fun parseGrade_handlesModifiers() {
        val viewModel = AreaDetailViewModel()
        
        val parseGradeMethod = viewModel.javaClass.getDeclaredMethod("parseGrade", String::class.java)
        parseGradeMethod.isAccessible = true
        
        val grade6a = parseGradeMethod.invoke(viewModel, "6a") as Int
        val grade6aPlus = parseGradeMethod.invoke(viewModel, "6a+") as Int
        val grade6b = parseGradeMethod.invoke(viewModel, "6b") as Int
        
        assertTrue("6a should be less than 6a+", grade6a < grade6aPlus)
        assertTrue("6a+ should be less than 6b", grade6aPlus < grade6b)
    }
    
    @Test
    fun matchesGradeRange_worksWithMinOnly() {
        val viewModel = AreaDetailViewModel()
        
        val matchesGradeRangeMethod = viewModel.javaClass.getDeclaredMethod(
            "matchesGradeRange", 
            String::class.java, 
            String::class.java, 
            String::class.java
        )
        matchesGradeRangeMethod.isAccessible = true
        
        val matches6a = matchesGradeRangeMethod.invoke(viewModel, "6a", "5c", null) as Boolean
        val matches5a = matchesGradeRangeMethod.invoke(viewModel, "5a", "5c", null) as Boolean
        
        assertTrue("6a should match min 5c", matches6a)
        assertFalse("5a should not match min 5c", matches5a)
    }
    
    @Test
    fun matchesGradeRange_worksWithMaxOnly() {
        val viewModel = AreaDetailViewModel()
        
        val matchesGradeRangeMethod = viewModel.javaClass.getDeclaredMethod(
            "matchesGradeRange", 
            String::class.java, 
            String::class.java, 
            String::class.java
        )
        matchesGradeRangeMethod.isAccessible = true
        
        val matches5c = matchesGradeRangeMethod.invoke(viewModel, "5c", null, "6a") as Boolean
        val matches7a = matchesGradeRangeMethod.invoke(viewModel, "7a", null, "6a") as Boolean
        
        assertTrue("5c should match max 6a", matches5c)
        assertFalse("7a should not match max 6a", matches7a)
    }
    
    @Test
    fun matchesGradeRange_worksWithBothMinAndMax() {
        val viewModel = AreaDetailViewModel()
        
        val matchesGradeRangeMethod = viewModel.javaClass.getDeclaredMethod(
            "matchesGradeRange", 
            String::class.java, 
            String::class.java, 
            String::class.java
        )
        matchesGradeRangeMethod.isAccessible = true
        
        val matches6a = matchesGradeRangeMethod.invoke(viewModel, "6a", "5c", "7a") as Boolean
        val matches5a = matchesGradeRangeMethod.invoke(viewModel, "5a", "5c", "7a") as Boolean
        val matches8a = matchesGradeRangeMethod.invoke(viewModel, "8a", "5c", "7a") as Boolean
        
        assertTrue("6a should match range 5c-7a", matches6a)
        assertFalse("5a should not match range 5c-7a", matches5a)
        assertFalse("8a should not match range 5c-7a", matches8a)
    }
}
