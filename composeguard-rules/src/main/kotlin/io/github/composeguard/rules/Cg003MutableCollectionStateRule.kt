package io.github.composeguard.rules

import io.github.composeguard.core.ComposeGuardRule
import io.github.composeguard.core.Issue
import io.github.composeguard.core.Severity
import io.github.composeguard.core.SourceFile
import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.com.intellij.psi.PsiRecursiveElementWalkingVisitor
import org.jetbrains.kotlin.psi.KtCallExpression

class Cg003MutableCollectionStateRule : ComposeGuardRule {
    override val id: String = "CG003"

    override fun analyze(file: SourceFile): List<Issue> = KotlinPsiParser().use { parser ->
        val ktFile = parser.parse(file.name, file.content)
        val lines = file.content.lineNumberLookup()
        val issues = mutableListOf<Issue>()

        ktFile.accept(object : PsiRecursiveElementWalkingVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element is KtCallExpression && element.calleeExpression?.text == "mutableStateOf" && element.hasMutableCollectionValue()) {
                    issues += Issue(
                        id = id,
                        severity = Severity.HIGH,
                        file = file.name,
                        line = lines(element.textOffset),
                        message = "Mutable collection stored in Compose state.",
                        why = "Mutating the collection itself may not trigger correct Compose observation and can make state stability harder to reason about.",
                        detected = element.text,
                        suggestion = "Use immutable collections, SnapshotStateList when appropriate, or replace state values instead of mutating the underlying collection."
                    )
                }
                super.visitElement(element)
            }
        })

        issues
    }

    private fun KtCallExpression.hasMutableCollectionValue(): Boolean {
        val argument = valueArguments.firstOrNull()?.getArgumentExpression()?.text ?: return false
        return mutableFactories.any { Regex("""\b$it\s*(<[^>]+>)?\s*\(""").containsMatchIn(argument) } ||
            mutableTypes.any { Regex("""\b$it\s*[<(]""").containsMatchIn(argument) }
    }

    private companion object {
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
    }
}
