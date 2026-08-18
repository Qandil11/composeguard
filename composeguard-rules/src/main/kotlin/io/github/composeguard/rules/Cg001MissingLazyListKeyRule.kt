package io.github.composeguard.rules

import io.github.composeguard.core.ComposeGuardRule
import io.github.composeguard.core.Issue
import io.github.composeguard.core.Severity
import io.github.composeguard.core.SourceFile
import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.com.intellij.psi.PsiRecursiveElementWalkingVisitor
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtLambdaArgument

class Cg001MissingLazyListKeyRule : ComposeGuardRule {
    override val id: String = "CG001"

    override fun analyze(file: SourceFile): List<Issue> = KotlinPsiParser().use { parser ->
        val ktFile = parser.parse(file.name, file.content)
        val lines = file.content.lineNumberLookup()
        val issues = mutableListOf<Issue>()

        ktFile.accept(object : PsiRecursiveElementWalkingVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element is KtCallExpression && element.calleeName() in lazyContainers) {
                    val container = element.calleeName() ?: "LazyColumn"
                    element.lambdaArguments.forEach { lambda ->
                        issues += findLazyItemCalls(lambda, file, lines, container)
                    }
                }
                super.visitElement(element)
            }
        })

        issues
    }

    private fun findLazyItemCalls(
        lambda: KtLambdaArgument,
        file: SourceFile,
        lines: (Int) -> Int,
        container: String
    ): List<Issue> {
        val issues = mutableListOf<Issue>()
        lambda.accept(object : PsiRecursiveElementWalkingVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element is KtCallExpression && element.calleeName() in lazyItemCalls && !element.hasKeyArgument()) {
                    issues += Issue(
                        id = id,
                        severity = Severity.HIGH,
                        file = file.name,
                        line = lines(element.textOffset),
                        message = "Missing stable key in $container.",
                        why = "Without stable keys, Compose may perform unnecessary recomposition when list items change position.",
                        detected = element.detectedSnippet(),
                        suggestion = """
                            items(
                                items = products,
                                key = { it.id }
                            ) { product ->
                                ...
                            }
                        """.trimIndent(),
                        path = file.path
                    )
                }
                super.visitElement(element)
            }
        })
        return issues
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
