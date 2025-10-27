package com.example.topoclimb.utils

import com.example.topoclimb.data.GradingSystem

/**
 * Utility class for handling grade conversions and comparisons
 */
object GradeUtils {

    fun minGradePoints(gradingSystem: GradingSystem?): Int {
        return gradingSystem?.points?.values?.minOrNull() ?: 300
    }

    fun maxGradePoints(gradingSystem: GradingSystem?): Int {
        return gradingSystem?.points?.values?.maxOrNull() ?: 950
    }
    
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
        return 0
    }

    /**
     * Converts a grade point value back to a grade string.
     * Uses the grading system if available, otherwise reconstructs from default parsing logic.
     *
     * @param points The numeric grade value (e.g., 610)
     * @param gradingSystem Optional grading system with points mapping
     * @return Grade string (e.g., "6a+"), or null if points cannot be converted
     */
    fun pointsToGrade(points: Int, gradingSystem: GradingSystem?): String {
        // First try to find the grade in the grading system
        gradingSystem?.points?.entries?.find { it.value == points }?.let {
            return it.key
        }

        return points.toString()
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
