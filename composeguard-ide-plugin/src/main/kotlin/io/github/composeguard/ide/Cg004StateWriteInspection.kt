package io.github.composeguard.ide

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiRecursiveElementWalkingVisitor
import io.github.composeguard.rules.Cg004StateWriteDetector
import io.github.composeguard.rules.ComposeGuardRuleTexts
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtUnaryExpression

class Cg004StateWriteInspection : ComposeGuardInspection(
    shortName = "ComposeGuardCg004",
    ruleText = ComposeGuardRuleTexts.cg004
) {
    override fun inspect(element: PsiElement, holder: ProblemsHolder) {
        val function = element.parentOfType<KtNamedFunction>()?.takeIf { it.isComposable() } ?: return
        if (element.parentOfType<KtNamedFunction>() != function) return

        val state = function.collectStateNames()
        when {
            element is KtUnaryExpression && element.isDirectCompositionStateMutation(function, state.delegatedState) -> {
                holder.registerComposeGuardProblem(element, element.text)
            }
            element is KtBinaryExpression && element.isDirectCompositionStateMutation(function, state.delegatedState, state.stateHolders) -> {
                holder.registerComposeGuardProblem(element, element.text)
            }
        }
    }

    private fun KtNamedFunction.collectStateNames(): StateNames {
        val delegatedState = mutableSetOf<String>()
        val stateHolders = mutableSetOf<String>()
        bodyExpression?.accept(object : PsiRecursiveElementWalkingVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element is KtProperty && element.parentOfType<KtNamedFunction>() == this@collectStateNames) {
                    when {
                        element.delegateExpression?.text?.contains("mutableStateOf") == true -> {
                            element.name?.let(delegatedState::add)
                        }
                        element.initializer?.text?.contains("mutableStateOf") == true -> {
                            element.name?.let(stateHolders::add)
                        }
                    }
                }
                super.visitElement(element)
            }
        })
        return StateNames(delegatedState, stateHolders)
    }

    private fun KtUnaryExpression.isDirectCompositionStateMutation(
        function: KtNamedFunction,
        delegatedState: Set<String>
    ): Boolean {
        if (isInsideLambdaWithin(function)) return false
        val token = operationReference.text
        val target = baseExpression?.text
        return token in setOf("++", "--") && target in delegatedState
    }

    private fun KtBinaryExpression.isDirectCompositionStateMutation(
        function: KtNamedFunction,
        delegatedState: Set<String>,
        stateHolders: Set<String>
    ): Boolean {
        if (isInsideLambdaWithin(function)) return false
        if (operationReference.text !in Cg004StateWriteDetector.assignmentOperators) return false

        val leftText = left?.text ?: return false
        if (leftText in delegatedState) return true

        val qualifiedLeft = left as? KtDotQualifiedExpression ?: return false
        val receiver = qualifiedLeft.receiverExpression.text
        val selector = qualifiedLeft.selectorExpression?.text
        return receiver in stateHolders && selector == "value"
    }

    private data class StateNames(
        val delegatedState: Set<String>,
        val stateHolders: Set<String>
    )
}
