package com.example.topoclimb.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Extension functions for Kotlin best practices
 */

/**
 * Safe launch that handles exceptions
 */
fun CoroutineScope.safeLaunch(
    onError: (Throwable) -> Unit = {},
    block: suspend CoroutineScope.() -> Unit
) = launch {
    try {
        block()
    } catch (e: Exception) {
        onError(e)
    }
}

/**
 * Extension to check if a string is not null or blank
 */
fun String?.isNotNullOrBlank(): Boolean = !this.isNullOrBlank()

/**
 * Extension to get a value or execute a block if null
 */
inline fun <T> T?.orElse(block: () -> T): T = this ?: block()

/**
 * Extension to transform a nullable value
 */
inline fun <T, R> T?.letIfNotNull(block: (T) -> R): R? = this?.let(block)
