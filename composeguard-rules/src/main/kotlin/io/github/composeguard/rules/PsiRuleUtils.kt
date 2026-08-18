package io.github.composeguard.rules

import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtFunctionLiteral
import org.jetbrains.kotlin.psi.KtNamedFunction

internal fun String.lineNumberLookup(): (Int) -> Int {
    val starts = mutableListOf(0)
    forEachIndexed { index, char ->
        if (char == '\n') starts += index + 1
    }
    return { offset ->
        val insertionPoint = starts.binarySearch(offset)
        if (insertionPoint >= 0) insertionPoint + 1 else -insertionPoint - 1
    }
}

internal inline fun <reified T : PsiElement> PsiElement.parentOfType(): T? {
    var current = parent
    while (current != null) {
        if (current is T) return current
        current = current.parent
    }
    return null
}

internal fun KtNamedFunction.isComposable(): Boolean =
    annotationEntries.any { entry ->
        entry.shortName?.asString() == "Composable" || entry.text.contains("Composable")
    }

internal fun KtCallExpression.calleeName(): String? {
    val callee = calleeExpression
    return when (callee) {
        is KtDotQualifiedExpression -> callee.selectorExpression?.text
        else -> callee?.text
    }
}

internal fun PsiElement.isInsideLambdaWithin(function: KtNamedFunction): Boolean {
    var current = parent
    while (current != null && current != function) {
        if (current is KtFunctionLiteral) return true
        current = current.parent
    }
    return false
}

internal fun PsiElement.isInsideCallLambdaWithin(function: KtNamedFunction, callNames: Set<String>): Boolean {
    var current = parent
    while (current != null && current != function) {
        if (current is KtFunctionLiteral) {
            val call = current.parentOfType<KtCallExpression>()
            if (call?.calleeName() in callNames) return true
        }
        current = current.parent
    }
    return false
}
