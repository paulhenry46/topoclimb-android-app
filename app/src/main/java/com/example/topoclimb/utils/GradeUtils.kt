package com.example.topoclimb.utils

import com.example.topoclimb.data.GradingSystem

/**
 * Utility class for handling grade conversions and comparisons
 */
object GradeUtils {
    
    /**
     * Minimum valid grade point value.
     * Corresponds to approximately grade 3a in the French grading system.
     */
    const val MIN_GRADE_POINTS = 300
    
    /**
     * Maximum valid grade point value.
     * Corresponds to approximately grade 9c in the French grading system.
     */
    const val MAX_GRADE_POINTS = 950
    
    /**
     * Converts a grade string to a numeric value using the provided grading system.
     * Falls back to the default grade parsing if grading system is not available.
     *
     * @param grade The grade string (e.g., "6a", "7b+")
     * @param gradingSystem Optional grading system with points mapping
     * @return Numeric value representing the grade, or 0 if invalid
     */
    fun gradeToPoints(grade: String, gradingSystem: GradingSystem?): Int {
        // Validate input
        if (grade.isBlank()) {
            return 0
        }
        
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
     * Converts a grade point value back to a grade string.
     * Uses the grading system if available, otherwise reconstructs from default parsing logic.
     *
     * @param points The numeric grade value (e.g., 610)
     * @param gradingSystem Optional grading system with points mapping
     * @return Grade string (e.g., "6a+"), or null if points cannot be converted
     */
    fun pointsToGrade(points: Int, gradingSystem: GradingSystem?): String? {
        // First try to find the grade in the grading system
        gradingSystem?.points?.entries?.find { it.value == points }?.let {
            return it.key
        }
        
        // Fall back to reconstructing from default parsing logic
        return reconstructGradeFromPoints(points)
    }
    
    /**
     * Reconstructs a grade string from point value using the inverse of default parsing.
     * 
     * @param points The numeric grade value
     * @return Grade string, or null if points are invalid
     */
    private fun reconstructGradeFromPoints(points: Int): String? {
        if (points <= 0) return null
        
        // Reverse the formula: points = (number * 10 + letter) * 10 + modifier adjustment
        val modifierValue = points % 10
        val baseValue = points / 10
        
        val letter = baseValue % 10
        val number = baseValue / 10
        
        // Validate ranges
        if (number < 3 || number > 9 || letter < 0 || letter > 2) {
            return null
        }
        
        val letterChar = when (letter) {
            0 -> "a"
            1 -> "b"
            2 -> "c"
            else -> return null
        }
        
        val modifier = when (modifierValue) {
            5 -> "+"
            -5, 995 -> "-" // Handle both positive and wrapped negative values
            0 -> ""
            else -> ""
        }
        
        return "$number$letterChar$modifier"
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
