package io.github.composeguard.ide

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiRecursiveElementWalkingVisitor
import io.github.composeguard.rules.ComposeGuardRuleTexts
import org.jetbrains.kotlin.psi.KtCallExpression

class Cg001MissingLazyListKeyInspection : ComposeGuardInspection(
    shortName = "ComposeGuardCg001",
    ruleText = ComposeGuardRuleTexts.cg001
) {
    override fun inspect(element: PsiElement, holder: ProblemsHolder) {
        val call = element as? KtCallExpression ?: return
        if (call.calleeName() !in lazyContainers) return

        call.lambdaArguments.forEach { lambda ->
            lambda.accept(object : PsiRecursiveElementWalkingVisitor() {
                override fun visitElement(element: PsiElement) {
                    if (element is KtCallExpression && element.calleeName() in lazyItemCalls && !element.hasKeyArgument()) {
                        holder.registerComposeGuardProblem(element, element.detectedSnippet())
                    }
                    super.visitElement(element)
                }
            })
        }
    }

    private fun KtCallExpression.hasKeyArgument(): Boolean =
        valueArguments.any { it.getArgumentName()?.asName?.identifier == "key" }

    private fun KtCallExpression.detectedSnippet(): String {
        val callee = calleeName() ?: "items"
        val firstArgument = valueArguments.firstOrNull()?.text ?: "..."
        return "$callee($firstArgument) { ... }"
    }

    private companion object {
        val lazyContainers = setOf("LazyColumn", "LazyRow")
        val lazyItemCalls = setOf("items", "itemsIndexed")
    }
}
