package io.github.composeguard.rules

import io.github.composeguard.core.Severity

data class ComposeGuardRuleText(
    val id: String,
    val title: String,
    val severity: Severity,
    val why: String,
    val suggestion: String
) {
    val message: String = "$id: $title"
}

object ComposeGuardRuleTexts {
    val cg001 = ComposeGuardRuleText(
        id = "CG001",
        title = "Missing stable key in lazy list",
        severity = Severity.HIGH,
        why = "Without stable keys, Compose may perform unnecessary recomposition when list items change position.",
        suggestion = """
            items(
                items = products,
                key = { it.id }
            ) { product ->
                ...
            }
        """.trimIndent()
    )

    val cg002 = ComposeGuardRuleText(
        id = "CG002",
        title = "Collection transformation during composition",
        severity = Severity.MEDIUM,
        why = "Repeated collection work in composition can add avoidable UI-thread cost during recomposition.",
        suggestion = "Move the transformation outside composition, or cache UI-specific work with remember using appropriate keys."
    )

    val cg003 = ComposeGuardRuleText(
        id = "CG003",
        title = "Mutable collection stored in Compose state",
        severity = Severity.HIGH,
        why = "Mutating the collection itself may not trigger correct Compose observation and can make state stability harder to reason about.",
        suggestion = "Use immutable collections, SnapshotStateList when appropriate, or replace state values instead of mutating the underlying collection."
    )

    val cg004 = ComposeGuardRuleText(
        id = "CG004",
        title = "State write during composition",
        severity = Severity.HIGH,
        why = "Writing state while composing can trigger backwards writes and unstable recomposition loops.",
        suggestion = "Move state writes into an event handler or an effect such as LaunchedEffect, SideEffect, or DisposableEffect."
    )
}
