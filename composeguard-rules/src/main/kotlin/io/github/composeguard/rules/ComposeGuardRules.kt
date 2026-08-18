package io.github.composeguard.rules

import io.github.composeguard.core.ComposeGuardRule

object ComposeGuardRules {
    fun phaseOne(): List<ComposeGuardRule> = listOf(Cg001MissingLazyListKeyRule())
}
