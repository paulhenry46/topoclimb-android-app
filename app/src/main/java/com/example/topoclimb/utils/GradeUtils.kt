package com.example.topoclimb.utils

import com.example.topoclimb.data.GradingSystem

/**
 * Utility class for handling grade conversions and comparisons
 */
object GradeUtils {
    
    /**
     * Converts a grade string to a numeric value using the provided grading system.
     * Falls back to the default grade parsing if grading system is not available.
     *
     * @param grade The grade string (e.g., "6a", "7b+")
     * @param gradingSystem Optional grading system with points mapping
     * @return Numeric value representing the grade
     */
    fun gradeToPoints(grade: String, gradingSystem: GradingSystem?): Int {
        // First try to use the grading system if available
        gradingSystem?.points?.get(grade)?.let { return it }
        
        // Fall back to default grade parsing
        return parseGradeDefault(grade)
    }
    
    /**
     * Default grade parsing implementation for French grades.
     * Handles grades like 5a, 5b, 5c, 6a+, 7b-, etc.
     * 
     * Format: [number][letter][optional modifier]
     * - Number: 3-9 (main difficulty level)
     * - Letter: a, b, c (sub-level within the number)
     * - Modifier: + or - (fine-tuning)
     *
     * @param grade The grade string
     * @return Numeric value for comparison
     */
    private fun parseGradeDefault(grade: String): Int {
        val cleanGrade = grade.trim().lowercase()
        
        // Extract the number (first digit)
        val number = cleanGrade.firstOrNull()?.digitToIntOrNull() ?: return 0
        
        // Extract the letter (a=0, b=1, c=2)
        val letter = when {
            cleanGrade.contains("a") -> 0
            cleanGrade.contains("b") -> 1
            cleanGrade.contains("c") -> 2
            else -> 0
        }
        
        // Extract modifier (+=0.5, -=-0.5)
        val modifier = when {
            cleanGrade.contains("+") -> 0.5
            cleanGrade.contains("-") -> -0.5
            else -> 0.0
        }
        
        // Calculate final value: (number * 10 + letter) * 10 + modifier adjustment
        return ((number * 10 + letter) * 10 + (modifier * 10).toInt())
    }
    
    /**
     * Checks if a grade matches the specified range
     *
     * @param grade The grade to check
     * @param minGrade Optional minimum grade
     * @param maxGrade Optional maximum grade
     * @param gradingSystem Optional grading system for conversion
     * @return true if the grade is within the specified range
     */
    fun matchesGradeRange(
        grade: String,
        minGrade: String?,
        maxGrade: String?,
        gradingSystem: GradingSystem?
    ): Boolean {
        val gradeValue = gradeToPoints(grade, gradingSystem)
        val minValue = minGrade?.let { gradeToPoints(it, gradingSystem) }
        val maxValue = maxGrade?.let { gradeToPoints(it, gradingSystem) }
        
        return when {
            minValue != null && maxValue != null -> gradeValue in minValue..maxValue
            minValue != null -> gradeValue >= minValue
            maxValue != null -> gradeValue <= maxValue
            else -> true
        }
    }
}
