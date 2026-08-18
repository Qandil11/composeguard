package io.github.composeguard.core

class ComposeGuardAnalyzer(
    private val rules: List<ComposeGuardRule>
) {
    fun analyze(files: List<SourceFile>): List<Issue> =
        files.flatMap { file -> rules.flatMap { rule -> rule.analyze(file) } }
            .sortedWith(compareBy<Issue> { it.file }.thenBy { it.line }.thenBy { it.id })
}
