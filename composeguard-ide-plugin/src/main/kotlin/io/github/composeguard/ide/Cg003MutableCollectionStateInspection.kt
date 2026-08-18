package io.github.composeguard.ide

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import io.github.composeguard.rules.Cg003MutableCollectionStateDetector
import io.github.composeguard.rules.ComposeGuardRuleTexts
import org.jetbrains.kotlin.psi.KtCallExpression

class Cg003MutableCollectionStateInspection : ComposeGuardInspection(
    shortName = "ComposeGuardCg003",
    ruleText = ComposeGuardRuleTexts.cg003
) {
    override fun inspect(element: PsiElement, holder: ProblemsHolder) {
        val call = element as? KtCallExpression ?: return
        if (call.calleeName() != "mutableStateOf") return
        if (!call.hasMutableCollectionValue()) return

        holder.registerComposeGuardProblem(call, call.text)
    }

    private fun KtCallExpression.hasMutableCollectionValue(): Boolean {
        val argument = valueArguments.firstOrNull()?.getArgumentExpression()?.text ?: return false
        return Cg003MutableCollectionStateDetector.mutableFactories.any {
            Regex("""\b$it\s*(<[^>]+>)?\s*\(""").containsMatchIn(argument)
        } || Cg003MutableCollectionStateDetector.mutableTypes.any {
            Regex("""\b$it\s*[<(]""").containsMatchIn(argument)
        }
    }
}
