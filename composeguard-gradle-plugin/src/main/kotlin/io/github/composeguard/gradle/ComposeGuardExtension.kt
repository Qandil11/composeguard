package io.github.composeguard.gradle

import org.gradle.api.Project
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property

abstract class ComposeGuardExtension(project: Project) {
    val failOnHigh: Property<Boolean> = project.objects.property(Boolean::class.java).convention(true)
    val sourceDirs: ListProperty<String> = project.objects.listProperty(String::class.java).convention(listOf("src"))
}
