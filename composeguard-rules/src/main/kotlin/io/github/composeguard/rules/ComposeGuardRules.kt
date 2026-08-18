package io.github.composeguard.rules

import io.github.composeguard.core.ComposeGuardRule

object ComposeGuardRules {
    fun phaseOne(): List<ComposeGuardRule> = listOf(Cg001MissingLazyListKeyRule())
    fun phaseTwo(): List<ComposeGuardRule> = listOf(
        Cg001MissingLazyListKeyRule(),
        Cg002CollectionOperationInCompositionRule(),
        Cg003MutableCollectionStateRule(),
        Cg004StateWriteDuringCompositionRule()
    )
}
