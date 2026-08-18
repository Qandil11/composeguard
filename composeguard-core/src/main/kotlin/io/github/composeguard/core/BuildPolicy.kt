package io.github.composeguard.core

data class BuildPolicy(
    val enabled: Boolean,
    val minimumFailureSeverity: Severity?,
    val shouldFail: Boolean
) {
    fun describe(): String {
        if (!enabled || minimumFailureSeverity == null) return "PASS (failure disabled)"
        return "${if (shouldFail) "FAIL" else "PASS"} (failOnSeverity=$minimumFailureSeverity)"
    }
}
