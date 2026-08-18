package io.github.composeguard.gradle

import org.gradle.api.Project
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property

abstract class ComposeGuardExtension(project: Project) {
    val failOnHigh: Property<Boolean> = project.objects.property(Boolean::class.java).convention(true)
    val failOnSeverity: Property<String> = project.objects.property(String::class.java).convention("HIGH")
    val minimumSeverity: Property<String> = project.objects.property(String::class.java).convention("LOW")
    val sourceDirs: ListProperty<String> = project.objects.listProperty(String::class.java).convention(listOf("src"))
    val excludes: ListProperty<String> = project.objects.listProperty(String::class.java).convention(emptyList())
}
