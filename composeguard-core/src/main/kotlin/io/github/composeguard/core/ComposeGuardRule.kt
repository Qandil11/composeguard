package io.github.composeguard.core

interface ComposeGuardRule {
    val id: String
    fun analyze(file: SourceFile): List<Issue>
}
