package io.github.composeguard.core

data class Issue(
    val id: String,
    val severity: Severity,
    val file: String,
    val line: Int,
    val message: String,
    val detected: String? = null,
    val suggestion: String? = null,
    val why: String? = null,
    val path: String = file
)
