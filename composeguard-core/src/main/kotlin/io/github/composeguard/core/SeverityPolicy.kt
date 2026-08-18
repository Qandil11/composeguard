package io.github.composeguard.core

object SeverityPolicy {
    fun includes(issueSeverity: Severity, minimumSeverity: Severity): Boolean =
        rank(issueSeverity) >= rank(minimumSeverity)

    private fun rank(severity: Severity): Int =
        when (severity) {
            Severity.LOW -> 1
            Severity.MEDIUM -> 2
            Severity.HIGH -> 3
        }
}
