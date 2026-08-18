package io.github.composeguard.rules

import io.github.composeguard.core.ComposeGuardRule
import io.github.composeguard.core.Issue
import io.github.composeguard.core.SourceFile

class Cg002CollectionOperationInCompositionRule : ComposeGuardRule {
    override val id: String = "CG002"
    private val text = ComposeGuardRuleTexts.cg002

    override fun analyze(file: SourceFile): List<Issue> = KotlinPsiParser().use { parser ->
        val ktFile = parser.parse(file.name, file.content)
        Cg002CollectionOperationDetector.detect(ktFile, file.content).map { finding ->
            Issue(
                id = id,
                severity = text.severity,
                file = file.name,
                line = finding.line,
                message = "Collection transformation executes during composition.",
                why = text.why,
                detected = finding.detected,
                suggestion = text.suggestion,
                path = file.path
            )
        }
    }
}
