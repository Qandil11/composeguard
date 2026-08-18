package io.github.composeguard.ide

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import io.github.composeguard.rules.ComposeGuardRuleText
import org.jetbrains.kotlin.psi.KtVisitorVoid

abstract class ComposeGuardInspection(
    private val shortName: String,
    protected val ruleText: ComposeGuardRuleText
) : LocalInspectionTool() {
    override fun getShortName(): String = shortName

    override fun getID(): String = ruleText.id

    override fun getAlternativeID(): String = ruleText.id

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor =
        object : KtVisitorVoid() {
            override fun visitElement(element: PsiElement) {
                inspect(element, holder)
                super.visitElement(element)
            }
        }

    protected abstract fun inspect(element: PsiElement, holder: ProblemsHolder)

    protected fun ProblemsHolder.registerComposeGuardProblem(element: PsiElement, detected: String) {
        if (isSuppressedFor(element)) return
        registerProblem(
            element,
            "${ruleText.id}: ${ruleText.title}\n\n" +
                "Detected: $detected\n\n" +
                "Why this matters: ${ruleText.why}\n\n" +
                "Suggested remediation: ${ruleText.suggestion}"
        )
    }
}
