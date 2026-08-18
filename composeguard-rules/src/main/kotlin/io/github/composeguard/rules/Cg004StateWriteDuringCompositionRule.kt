package io.github.composeguard.rules

import io.github.composeguard.core.ComposeGuardRule
import io.github.composeguard.core.Issue
import io.github.composeguard.core.Severity
import io.github.composeguard.core.SourceFile
import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.com.intellij.psi.PsiRecursiveElementWalkingVisitor
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtUnaryExpression

class Cg004StateWriteDuringCompositionRule : ComposeGuardRule {
    override val id: String = "CG004"

    override fun analyze(file: SourceFile): List<Issue> = KotlinPsiParser().use { parser ->
        val ktFile = parser.parse(file.name, file.content)
        val lines = file.content.lineNumberLookup()
        val issues = mutableListOf<Issue>()

        ktFile.accept(object : PsiRecursiveElementWalkingVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element is KtNamedFunction && element.isComposable()) {
                    issues += analyzeComposableFunction(element, file, lines)
                }
                super.visitElement(element)
            }
        })

        issues
    }

    private fun analyzeComposableFunction(
        function: KtNamedFunction,
        file: SourceFile,
        lines: (Int) -> Int
    ): List<Issue> {
        val delegatedState = mutableSetOf<String>()
        val stateHolders = mutableSetOf<String>()
        val issues = mutableListOf<Issue>()

        function.bodyExpression?.accept(object : PsiRecursiveElementWalkingVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element is KtProperty && element.parentOfType<KtNamedFunction>() == function) {
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

        function.bodyExpression?.accept(object : PsiRecursiveElementWalkingVisitor() {
            override fun visitElement(element: PsiElement) {
                when {
                    element is KtUnaryExpression && element.isDirectCompositionStateMutation(function, delegatedState) -> {
                        issues += element.toIssue(file, lines)
                    }
                    element is KtBinaryExpression && element.isDirectCompositionStateMutation(function, delegatedState, stateHolders) -> {
                        issues += element.toIssue(file, lines)
                    }
                }
                super.visitElement(element)
            }
        })

        return issues
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
        if (operationReference.text !in assignmentOperators) return false

        val leftText = left?.text ?: return false
        if (leftText in delegatedState) return true

        val left = left as? KtDotQualifiedExpression ?: return false
        val receiver = left.receiverExpression.text
        val selector = left.selectorExpression?.text
        return receiver in stateHolders && selector == "value"
    }

    private fun PsiElement.toIssue(file: SourceFile, lines: (Int) -> Int): Issue =
        Issue(
            id = id,
            severity = Severity.HIGH,
            file = file.name,
            line = lines(textOffset),
            message = "Compose state is written during composition.",
            why = "Writing state while composing can trigger backwards writes and unstable recomposition loops.",
            detected = text,
            suggestion = "Move state writes into an event handler or an effect such as LaunchedEffect, SideEffect, or DisposableEffect."
        )

    private companion object {
        val assignmentOperators = setOf("=", "+=", "-=", "*=", "/=", "%=")
    }
}
