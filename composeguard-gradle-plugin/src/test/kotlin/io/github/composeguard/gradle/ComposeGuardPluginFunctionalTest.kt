package io.github.composeguard.gradle

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class ComposeGuardPluginFunctionalTest {
    @Test
    fun `composeGuard reports CG001 and fails build for high severity issue`() {
        val projectDir = createTempDirectory(prefix = "composeguard-functional-").toFile()
        projectDir.resolve("settings.gradle.kts").writeText("""pluginManagement { repositories { gradlePluginPortal(); mavenCentral(); google() } }""")
        projectDir.resolve("build.gradle.kts").writeText("""plugins { id("io.github.composeguard") }""")
        projectDir.resolve("src/main/kotlin").mkdirs()
        projectDir.resolve("src/main/kotlin/ProductList.kt").writeText(
            """
                import androidx.compose.foundation.lazy.LazyColumn
                import androidx.compose.foundation.lazy.items
                
                @Composable
                fun ProductList(products: List<Product>) {
                    LazyColumn {
                        items(products) { product ->
                            Text(product.name)
                        }
                    }
                }
            """.trimIndent()
        )

        val result = GradleRunner.create()
            .withProjectDir(projectDir)
            .withArguments("composeGuard", "--stacktrace")
            .withPluginClasspath()
            .buildAndFail()

        assertEquals(TaskOutcome.FAILED, result.task(":composeGuard")?.outcome)
        assertContains(result.output, "CG001 HIGH")
        assertContains(result.output, "ProductList.kt:7")
        assertContains(result.output, "Missing stable key in LazyColumn.")
        assertContains(result.output, "items(products) { ... }")

        val report = projectDir.resolve("build/reports/composeguard/composeguard.txt").readText()
        assertContains(report, "CG001 HIGH")
        assertContains(report, "ProductList.kt:7")
    }

    private fun File.resolve(path: String): File = File(this, path)
}
