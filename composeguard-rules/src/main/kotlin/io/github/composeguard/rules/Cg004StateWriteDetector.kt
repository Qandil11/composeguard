package io.github.composeguard.rules

import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.com.intellij.psi.PsiRecursiveElementWalkingVisitor
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtUnaryExpression

data class Cg004Finding(
    val line: Int,
    val startOffset: Int,
    val endOffset: Int,
    val detected: String
)

object Cg004StateWriteDetector {
    val assignmentOperators = setOf("=", "+=", "-=", "*=", "/=", "%=")

    fun detect(file: KtFile, source: String): List<Cg004Finding> {
        val lines = source.lineNumberLookup()
        val findings = mutableListOf<Cg004Finding>()

        file.accept(object : PsiRecursiveElementWalkingVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element is KtNamedFunction && element.isComposable()) {
                    findings += analyzeComposableFunction(element, lines)
                }
                super.visitElement(element)
            }
        })

        return findings
    }

    private fun analyzeComposableFunction(
        function: KtNamedFunction,
        lines: (Int) -> Int
    ): List<Cg004Finding> {
        val delegatedState = mutableSetOf<String>()
        val stateHolders = mutableSetOf<String>()
        val findings = mutableListOf<Cg004Finding>()

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
                    element is KtUnaryExpression &&
                        element.parentOfType<KtNamedFunction>() == function &&
                        element.isDirectCompositionStateMutation(function, delegatedState) -> {
                        findings += element.toFinding(lines)
                    }
                    element is KtBinaryExpression &&
                        element.parentOfType<KtNamedFunction>() == function &&
                        element.isDirectCompositionStateMutation(function, delegatedState, stateHolders) -> {
                        findings += element.toFinding(lines)
                    }
                }
                super.visitElement(element)
            }
        })

        return findings
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

    private fun PsiElement.toFinding(lines: (Int) -> Int): Cg004Finding =
        Cg004Finding(
            line = lines(textOffset),
            startOffset = textOffset,
            endOffset = textRange.endOffset,
            detected = text
        )
}
