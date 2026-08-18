package io.github.composeguard.ide

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElementVisitor
import io.github.composeguard.rules.Cg001LazyListKeyDetector
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtVisitorVoid

class Cg001MissingLazyListKeyInspection : LocalInspectionTool() {
    override fun getShortName(): String = "ComposeGuardCg001"

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor =
        object : KtVisitorVoid() {
            override fun visitKtFile(file: KtFile) {
                Cg001LazyListKeyDetector.detect(file.text).forEach { finding ->
                    holder.registerProblem(
                        file,
                        TextRange(finding.startOffset, finding.endOffset),
                        "CG001: Missing stable key in lazy list\n\n" +
                            "Detected: ${finding.detected}\n\n" +
                            "Why this matters: Without stable keys, Compose may perform unnecessary recomposition when list items change position.\n\n" +
                            "Suggested remediation: add a stable key, for example key = { it.id }."
                    )
                }
            }
        }
}
