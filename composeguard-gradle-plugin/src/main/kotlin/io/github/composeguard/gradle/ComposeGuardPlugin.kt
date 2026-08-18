package io.github.composeguard.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project

class ComposeGuardPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.create(
            "composeGuard",
            ComposeGuardExtension::class.java,
            project
        )

        project.tasks.register("composeGuard", ComposeGuardTask::class.java) {
            it.group = "verification"
            it.description = "Runs ComposeGuard static performance checks."
            it.failOnHigh.set(extension.failOnHigh)
            it.failOnSeverity.set(extension.failOnSeverity)
            it.minimumSeverity.set(extension.minimumSeverity)
            it.excludes.set(extension.excludes)
            it.reportFile.set(project.layout.buildDirectory.file("reports/composeguard/composeguard.txt"))
            it.sourceDirectories.set(extension.sourceDirs.map { dirs ->
                dirs.map { dir -> project.layout.projectDirectory.dir(dir) }
            })
        }
    }
}
