package io.github.composeguard.core

object ComposeGuardReport {
    fun render(
        issues: List<Issue>,
        filesAnalyzed: Int = 0,
        buildPolicy: BuildPolicy = BuildPolicy(
            enabled = true,
            minimumFailureSeverity = Severity.HIGH,
            shouldFail = issues.any { it.severity == Severity.HIGH }
        ),
        score: Int = scoreFor(issues)
    ): String = buildString {
        val highCount = issues.count { it.severity == Severity.HIGH }
        val mediumCount = issues.count { it.severity == Severity.MEDIUM }
        val lowCount = issues.count { it.severity == Severity.LOW }

        appendLine("ComposeGuard Report")
        appendLine("===================")
        appendLine()
        appendLine("Files analysed: $filesAnalyzed")
        appendLine("Total issues: ${issues.size}")
        appendLine("Score: $score/100")
        appendLine("Build policy: ${buildPolicy.describe()}")
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
        appendLine("High: $highCount")
        appendLine("Medium: $mediumCount")
        appendLine("Low: $lowCount")
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
