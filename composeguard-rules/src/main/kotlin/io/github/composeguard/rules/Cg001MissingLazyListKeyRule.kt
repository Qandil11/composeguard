package io.github.composeguard.rules

import io.github.composeguard.core.ComposeGuardRule
import io.github.composeguard.core.Issue
import io.github.composeguard.core.SourceFile

class Cg001MissingLazyListKeyRule : ComposeGuardRule {
    override val id: String = "CG001"
    private val text = ComposeGuardRuleTexts.cg001

    override fun analyze(file: SourceFile): List<Issue> =
        Cg001LazyListKeyDetector.detect(file.content).map { finding ->
            Issue(
                id = id,
                severity = text.severity,
                file = file.name,
                line = finding.line,
                message = "Missing stable key in ${finding.container}.",
                why = text.why,
                detected = finding.detected,
                suggestion = text.suggestion,
                path = file.path
            )
        }
}
