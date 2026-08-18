package io.github.composeguard.ide

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtFunctionLiteral
import org.jetbrains.kotlin.psi.KtNamedFunction

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
