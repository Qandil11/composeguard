package io.github.composeguard.rules

import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.com.intellij.psi.PsiRecursiveElementWalkingVisitor
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtProperty

data class Cg002Finding(
    val line: Int,
    val startOffset: Int,
    val endOffset: Int,
    val detected: String
)

object Cg002CollectionOperationDetector {
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

    fun detect(file: KtFile, source: String): List<Cg002Finding> {
        val lines = source.lineNumberLookup()
        val findings = mutableListOf<Cg002Finding>()

        file.accept(object : PsiRecursiveElementWalkingVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element is KtNamedFunction && element.isComposable()) {
                    element.bodyExpression?.accept(object : PsiRecursiveElementWalkingVisitor() {
                        override fun visitElement(bodyElement: PsiElement) {
                            if (bodyElement is KtProperty && bodyElement.isDirectCollectionTransformation(element)) {
                                val expression = bodyElement.initializer as KtDotQualifiedExpression
                                findings += Cg002Finding(
                                    line = lines(expression.textOffset),
                                    startOffset = expression.textOffset,
                                    endOffset = expression.textRange.endOffset,
                                    detected = expression.text
                                )
                            }
                            super.visitElement(bodyElement)
                        }
                    })
                }
                super.visitElement(element)
            }
        })

        return findings
    }

    private fun KtProperty.isDirectCollectionTransformation(function: KtNamedFunction): Boolean {
        if (parentOfType<KtNamedFunction>() != function) return false
        if (isInsideLambdaWithin(function)) return false
        val expression = initializer as? KtDotQualifiedExpression ?: return false
        return expression.selectorExpression
            ?.firstChild
            ?.text in collectionOperations
    }
}
