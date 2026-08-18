package io.github.composeguard.core

object ComposeGuardJsonReport {
    fun render(
        toolVersion: String,
        issues: List<Issue>,
        filesAnalyzed: Int,
        buildPolicy: BuildPolicy,
        score: Int = ComposeGuardReport.scoreFor(issues)
    ): String = buildString {
        appendLine("{")
        appendLine("  \"toolVersion\": ${toolVersion.jsonString()},")
        appendLine("  \"filesAnalyzed\": $filesAnalyzed,")
        appendLine("  \"score\": $score,")
        appendLine("  \"buildPolicy\": {")
        appendLine("    \"enabled\": ${buildPolicy.enabled},")
        appendLine("    \"minimumFailureSeverity\": ${buildPolicy.minimumFailureSeverity?.name.jsonNullableString()},")
        appendLine("    \"shouldFail\": ${buildPolicy.shouldFail},")
        appendLine("    \"result\": ${(if (buildPolicy.shouldFail) "FAIL" else "PASS").jsonString()}")
        appendLine("  },")
        appendLine("  \"issues\": [")
        issues.forEachIndexed { index, issue ->
            appendLine("    {")
            appendLine("      \"ruleId\": ${issue.id.jsonString()},")
            appendLine("      \"severity\": ${issue.severity.name.jsonString()},")
            appendLine("      \"path\": ${issue.path.jsonString()},")
            appendLine("      \"line\": ${issue.line},")
            appendLine("      \"description\": ${issue.message.jsonString()},")
            appendLine("      \"suggestion\": ${issue.suggestion.jsonNullableString()}")
            append("    }")
            if (index != issues.lastIndex) append(",")
            appendLine()
        }
        appendLine("  ]")
        appendLine("}")
    }.trimEnd()

    private fun String?.jsonNullableString(): String =
        this?.jsonString() ?: "null"

    private fun String.jsonString(): String =
        buildString {
            append('"')
            this@jsonString.forEach { char ->
                when (char) {
                    '\\' -> append("\\\\")
                    '"' -> append("\\\"")
                    '\n' -> append("\\n")
                    '\r' -> append("\\r")
                    '\t' -> append("\\t")
                    else -> append(char)
                }
            }
            append('"')
        }
}
