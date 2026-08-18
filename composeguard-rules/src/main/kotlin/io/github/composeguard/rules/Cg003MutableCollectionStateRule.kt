package io.github.composeguard.rules

import io.github.composeguard.core.ComposeGuardRule
import io.github.composeguard.core.Issue
import io.github.composeguard.core.SourceFile

class Cg003MutableCollectionStateRule : ComposeGuardRule {
    override val id: String = "CG003"
    private val text = ComposeGuardRuleTexts.cg003

    override fun analyze(file: SourceFile): List<Issue> = KotlinPsiParser().use { parser ->
        val ktFile = parser.parse(file.name, file.content)
        Cg003MutableCollectionStateDetector.detect(ktFile, file.content).map { finding ->
            Issue(
                id = id,
                severity = text.severity,
                file = file.name,
                line = finding.line,
                message = "${text.title}.",
                why = text.why,
                detected = finding.detected,
                suggestion = text.suggestion,
                path = file.path
            )
        }
    }
}
