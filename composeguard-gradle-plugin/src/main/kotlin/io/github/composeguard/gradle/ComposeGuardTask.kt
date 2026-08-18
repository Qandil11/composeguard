package io.github.composeguard.gradle

import io.github.composeguard.core.ComposeGuardAnalyzer
import io.github.composeguard.core.ComposeGuardReport
import io.github.composeguard.core.Severity
import io.github.composeguard.core.SourceFile
import io.github.composeguard.rules.ComposeGuardRules
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.Directory
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

abstract class ComposeGuardTask : DefaultTask() {
    @get:Input
    abstract val failOnHigh: Property<Boolean>

    @get:InputFiles
    abstract val sourceDirectories: ListProperty<Directory>

    @get:OutputFile
    abstract val reportFile: RegularFileProperty

    @TaskAction
    fun run() {
        val files = sourceDirectories.get()
            .flatMap { directory ->
                directory.asFile.walkTopDown()
                    .filter { it.isFile && it.extension == "kt" && !it.path.contains("${java.io.File.separator}build${java.io.File.separator}") }
                    .map { file -> SourceFile(file.name, file.path, file.readText()) }
            }

        val issues = ComposeGuardAnalyzer(ComposeGuardRules.phaseTwo()).analyze(files)
        val report = ComposeGuardReport.render(
            issues = issues,
            filesAnalyzed = files.size,
            failOnHigh = failOnHigh.get()
        )
        val output = reportFile.get().asFile
        output.parentFile.mkdirs()
        output.writeText(report)

        logger.lifecycle(report)
        logger.lifecycle("Report written to ${output.path}")

        if (failOnHigh.get() && issues.any { it.severity == Severity.HIGH }) {
            throw GradleException("ComposeGuard found HIGH severity issues.")
        }
    }
}
