package com.example.topoclimb.data

import androidx.compose.runtime.Stable
import com.google.gson.annotations.SerializedName

/**
 * Represents a grading system with a mapping of grade strings to point values
 * 
 * @property free Whether the grading system is free (boolean)
 * @property hint Hint or description about the grading system
 * @property points Map of grade strings (e.g., "6a", "7b+") to their point values
 */
@Stable
data class GradingSystem(
    val free: Boolean?,
    val hint: String?,
    val points: Map<String, Int>?
)
