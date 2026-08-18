package io.github.composeguard.gradle

import io.github.composeguard.core.BuildPolicy
import io.github.composeguard.core.ComposeGuardAnalyzer
import io.github.composeguard.core.ComposeGuardJsonReport
import io.github.composeguard.core.ComposeGuardReport
import io.github.composeguard.core.Severity
import io.github.composeguard.core.SeverityPolicy
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

    @get:Input
    abstract val failOnSeverity: Property<String>

    @get:Input
    abstract val minimumSeverity: Property<String>

    @get:Input
    abstract val excludes: ListProperty<String>

    @get:Input
    abstract val toolVersion: Property<String>

    @get:InputFiles
    abstract val sourceDirectories: ListProperty<Directory>

    @get:OutputFile
    abstract val reportFile: RegularFileProperty

    @get:OutputFile
    abstract val jsonReportFile: RegularFileProperty

    @TaskAction
    fun run() {
        val minimumReportSeverity = minimumSeverity.get().toSeverity("minimumSeverity")
        val minimumFailureSeverity = if (failOnHigh.get()) {
            failOnSeverity.get().toSeverity("failOnSeverity")
        } else {
            null
        }

        val files = sourceDirectories.get()
            .flatMap { directory ->
                directory.asFile.walkTopDown()
                    .filter { it.isFile && it.extension == "kt" && !it.path.contains("${java.io.File.separator}build${java.io.File.separator}") }
                    .filterNot { file -> excludes.get().any { pattern -> file.inExcludedPath(pattern) } }
                    .map { file -> SourceFile(file.name, file.path, file.readText()) }
            }

        val issues = ComposeGuardAnalyzer(ComposeGuardRules.phaseTwo()).analyze(files)
            .filter { SeverityPolicy.includes(it.severity, minimumReportSeverity) }
        val shouldFail = minimumFailureSeverity?.let { threshold ->
            issues.any { SeverityPolicy.includes(it.severity, threshold) }
        } ?: false
        val buildPolicy = BuildPolicy(
            enabled = minimumFailureSeverity != null,
            minimumFailureSeverity = minimumFailureSeverity,
            shouldFail = shouldFail
        )
        val report = ComposeGuardReport.render(
            issues = issues,
            filesAnalyzed = files.size,
            buildPolicy = buildPolicy
        )
        val output = reportFile.get().asFile
        output.parentFile.mkdirs()
        output.writeText(report)

        val jsonOutput = jsonReportFile.get().asFile
        jsonOutput.parentFile.mkdirs()
        jsonOutput.writeText(
            ComposeGuardJsonReport.render(
                toolVersion = toolVersion.get(),
                issues = issues,
                filesAnalyzed = files.size,
                buildPolicy = buildPolicy
            )
        )

        logger.lifecycle(report)
        logger.lifecycle("Report written to ${output.path}")
        logger.lifecycle("JSON report written to ${jsonOutput.path}")

        if (buildPolicy.shouldFail) {
            throw GradleException("ComposeGuard found issues at or above ${buildPolicy.minimumFailureSeverity}.")
        }
    }

    private fun String.toSeverity(propertyName: String): Severity =
        runCatching { Severity.valueOf(uppercase()) }
            .getOrElse {
                throw GradleException("$propertyName must be one of: HIGH, MEDIUM, LOW.")
            }

    private fun java.io.File.inExcludedPath(pattern: String): Boolean {
        val normalizedPath = path.replace(java.io.File.separatorChar, '/')
        val normalizedPattern = pattern.replace(java.io.File.separatorChar, '/')
        return normalizedPath.contains(normalizedPattern)
    }
}
