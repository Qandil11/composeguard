package io.github.composeguard.ide

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import io.github.composeguard.rules.Cg002CollectionOperationDetector
import io.github.composeguard.rules.ComposeGuardRuleTexts
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtProperty

class Cg002CollectionOperationInspection : ComposeGuardInspection(
    shortName = "ComposeGuardCg002",
    ruleText = ComposeGuardRuleTexts.cg002
) {
    override fun inspect(element: PsiElement, holder: ProblemsHolder) {
        val property = element as? KtProperty ?: return
        val function = property.parentOfType<KtNamedFunction>() ?: return
        if (!function.isComposable()) return
        if (property.parentOfType<KtNamedFunction>() != function) return
        if (property.isInsideLambdaWithin(function)) return

        val expression = property.initializer as? KtDotQualifiedExpression ?: return
        val operation = expression.selectorExpression?.firstChild?.text ?: return
        if (operation !in Cg002CollectionOperationDetector.collectionOperations) return

        holder.registerComposeGuardProblem(expression, expression.text)
    }
}
