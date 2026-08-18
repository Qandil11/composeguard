package io.github.composeguard.rules

import io.github.composeguard.core.ComposeGuardRule
import io.github.composeguard.core.Issue
import io.github.composeguard.core.Severity
import io.github.composeguard.core.SourceFile
import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.com.intellij.psi.PsiRecursiveElementWalkingVisitor
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtProperty

class Cg002CollectionOperationInCompositionRule : ComposeGuardRule {
    override val id: String = "CG002"

    override fun analyze(file: SourceFile): List<Issue> = KotlinPsiParser().use { parser ->
        val ktFile = parser.parse(file.name, file.content)
        val lines = file.content.lineNumberLookup()
        val issues = mutableListOf<Issue>()

        ktFile.accept(object : PsiRecursiveElementWalkingVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element is KtNamedFunction && element.isComposable()) {
                    element.bodyExpression?.accept(object : PsiRecursiveElementWalkingVisitor() {
                        override fun visitElement(bodyElement: PsiElement) {
                            if (bodyElement is KtProperty && bodyElement.isDirectCollectionTransformation(element)) {
                                val expression = bodyElement.initializer as KtDotQualifiedExpression
                                issues += Issue(
                                    id = id,
                                    severity = Severity.MEDIUM,
                                    file = file.name,
                                    line = lines(expression.textOffset),
                                    message = "Collection transformation executes during composition.",
                                    why = "Repeated collection work in composition can add avoidable UI-thread cost during recomposition.",
                                    detected = expression.text,
                                    suggestion = "Move the transformation outside composition, or cache UI-specific work with remember using appropriate keys."
                                )
                            }
                            super.visitElement(bodyElement)
                        }
                    })
                }
                super.visitElement(element)
            }
        })

        issues
    }

    private fun KtProperty.isDirectCollectionTransformation(function: KtNamedFunction): Boolean {
        if (isInsideLambdaWithin(function)) return false
        val expression = initializer as? KtDotQualifiedExpression ?: return false
        return expression.selectorExpression
            ?.firstChild
            ?.text in collectionOperations
    }

    private companion object {
        val collectionOperations = setOf(
            "sortedBy",
            "sortedByDescending",
            "sorted",
            "filter",
            "map",
            "groupBy",
            "associate",
            "distinct",
            "toList"
        )
    }
}
