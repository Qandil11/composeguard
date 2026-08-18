package io.github.composeguard.rules

import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.com.intellij.psi.PsiRecursiveElementWalkingVisitor
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtFile

data class Cg003Finding(
    val line: Int,
    val startOffset: Int,
    val endOffset: Int,
    val detected: String
)

object Cg003MutableCollectionStateDetector {
    val mutableFactories = setOf(
        "mutableListOf",
        "arrayListOf",
        "mutableSetOf",
        "mutableMapOf",
        "hashMapOf",
        "linkedMapOf"
    )
    val mutableTypes = setOf(
        "MutableList",
        "ArrayList",
        "MutableSet",
        "HashSet",
        "MutableMap",
        "HashMap",
        "LinkedHashMap"
    )

    fun detect(file: KtFile, source: String): List<Cg003Finding> {
        val lines = source.lineNumberLookup()
        val findings = mutableListOf<Cg003Finding>()

        file.accept(object : PsiRecursiveElementWalkingVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element is KtCallExpression && element.calleeName() == "mutableStateOf" && element.hasMutableCollectionValue()) {
                    findings += Cg003Finding(
                        line = lines(element.textOffset),
                        startOffset = element.textOffset,
                        endOffset = element.textRange.endOffset,
                        detected = element.text
                    )
                }
                super.visitElement(element)
            }
        })

        return findings
    }

    private fun KtCallExpression.hasMutableCollectionValue(): Boolean {
        val argument = valueArguments.firstOrNull()?.getArgumentExpression()?.text ?: return false
        return mutableFactories.any { Regex("""\b$it\s*(<[^>]+>)?\s*\(""").containsMatchIn(argument) } ||
            mutableTypes.any { Regex("""\b$it\s*[<(]""").containsMatchIn(argument) }
    }
}
