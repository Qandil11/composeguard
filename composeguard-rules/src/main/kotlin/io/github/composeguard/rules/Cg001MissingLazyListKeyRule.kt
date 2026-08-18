package io.github.composeguard.rules

import io.github.composeguard.core.ComposeGuardRule
import io.github.composeguard.core.Issue
import io.github.composeguard.core.Severity
import io.github.composeguard.core.SourceFile

class Cg001MissingLazyListKeyRule : ComposeGuardRule {
    override val id: String = "CG001"

    override fun analyze(file: SourceFile): List<Issue> =
        Cg001LazyListKeyDetector.detect(file.content).map { finding ->
            Issue(
                id = id,
                severity = Severity.HIGH,
                file = file.name,
                line = finding.line,
                message = "Missing stable key in ${finding.container}.",
                why = "Without stable keys, Compose may perform unnecessary recomposition when list items change position.",
                detected = finding.detected,
                suggestion = """
                    items(
                        items = products,
                        key = { it.id }
                    ) { product ->
                        ...
                    }
                """.trimIndent(),
                path = file.path
            )
        }
}
