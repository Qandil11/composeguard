package io.github.composeguard.core

object ComposeGuardReport {
    fun render(issues: List<Issue>, score: Int = scoreFor(issues)): String = buildString {
        appendLine("ComposeGuard Report")
        appendLine("===================")
        appendLine()
        appendLine("Score: $score/100")
        appendLine()

        if (issues.isEmpty()) {
            appendLine("No issues found.")
            appendLine()
        } else {
            issues.forEach { issue ->
                appendLine("${issue.id} ${issue.severity}")
                appendLine("${issue.file}:${issue.line}")
                appendLine()
                appendLine(issue.message)
                appendLine()
                issue.why?.let {
                    appendLine("Why this matters:")
                    appendLine(it)
                    appendLine()
                }
                issue.detected?.let {
                    appendLine("Detected:")
                    appendLine(it)
                    appendLine()
                }
                issue.suggestion?.let {
                    appendLine("Consider:")
                    appendLine(it)
                    appendLine()
                }
            }
        }

        appendLine("Summary:")
        appendLine("High: ${issues.count { it.severity == Severity.HIGH }}")
        appendLine("Medium: ${issues.count { it.severity == Severity.MEDIUM }}")
        appendLine("Low: ${issues.count { it.severity == Severity.LOW }}")
    }.trimEnd()

    fun scoreFor(issues: List<Issue>): Int {
        val penalty = issues.map { issue ->
            when (issue.severity) {
                Severity.HIGH -> 20
                Severity.MEDIUM -> 10
                Severity.LOW -> 5
            }
        }.sum()
        return (100 - penalty).coerceAtLeast(0)
    }
}
