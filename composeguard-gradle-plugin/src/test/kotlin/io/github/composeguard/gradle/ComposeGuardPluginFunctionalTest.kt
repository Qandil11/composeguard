package io.github.composeguard.gradle

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class ComposeGuardPluginFunctionalTest {
    @Test
    fun `composeGuard reports CG001 and fails build for high severity issue`() {
        val projectDir = fixture("composeguard-functional-")
        projectDir.writeBuildFile()
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

        val jsonReport = projectDir.resolve("build/reports/composeguard/composeguard.json").readText()
        assertContains(jsonReport, "\"toolVersion\": \"0.1.0\"")
        assertContains(jsonReport, "\"filesAnalyzed\": 1")
        assertContains(jsonReport, "\"ruleId\": \"CG001\"")
        assertContains(jsonReport, "\"severity\": \"HIGH\"")
        assertContains(jsonReport, "\"path\":")
        assertContains(jsonReport, "\"description\": \"Missing stable key in LazyColumn.\"")
        assertContains(jsonReport, "\"shouldFail\": true")
    }

    @Test
    fun `composeGuard reports CG002 through CG004 and report summary metadata`() {
        val projectDir = fixture("composeguard-phase-two-")
        projectDir.writeBuildFile(
            """
                plugins { id("io.github.composeguard") }
                
                composeGuard {
                    failOnHigh = false
                }
            """.trimIndent()
        )
        projectDir.resolve("src/main/kotlin").mkdirs()
        projectDir.resolve("src/main/kotlin/PhaseTwo.kt").writeText(
            """
                @Composable
                fun PhaseTwo(products: List<Product>) {
                    var users by remember {
                        mutableStateOf(mutableListOf<User>())
                    }
                    var count by remember { mutableStateOf(0) }
                    val sorted = products.sortedBy { it.name }
                
                    Text(sorted.size.toString())
                    count++
                }
            """.trimIndent()
        )

        val result = GradleRunner.create()
            .withProjectDir(projectDir)
            .withArguments("composeGuard", "--stacktrace")
            .withPluginClasspath()
            .build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":composeGuard")?.outcome)

        val report = projectDir.resolve("build/reports/composeguard/composeguard.txt").readText()
        assertContains(report, "Files analysed: 1")
        assertContains(report, "Total issues: 3")
        assertContains(report, "Build policy: PASS (failure disabled)")
        assertContains(report, "CG002 MEDIUM")
        assertContains(report, "CG003 HIGH")
        assertContains(report, "CG004 HIGH")
        assertContains(report, "High: 2")
        assertContains(report, "Medium: 1")
        assertContains(report, "Low: 0")
    }

    @Test
    fun `composeGuard succeeds for a clean project`() {
        val projectDir = fixture("composeguard-clean-")
        projectDir.writeBuildFile()
        projectDir.resolve("src/main/kotlin").mkdirs()
        projectDir.resolve("src/main/kotlin/CleanList.kt").writeText(
            """
                @Composable
                fun CleanList(products: List<Product>) {
                    LazyColumn {
                        items(
                            items = products,
                            key = { it.id }
                        ) { product ->
                            Text(product.name)
                        }
                    }
                }
            """.trimIndent()
        )

        val result = projectDir.runGradle("composeGuard").build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":composeGuard")?.outcome)
        val report = projectDir.report()
        assertContains(report, "Files analysed: 1")
        assertContains(report, "Total issues: 0")
        assertContains(report, "Build policy: PASS (failOnSeverity=HIGH)")
    }

    @Test
    fun `composeGuard respects source exclusions`() {
        val projectDir = fixture("composeguard-exclusions-")
        projectDir.writeBuildFile(
            """
                plugins { id("io.github.composeguard") }
                
                composeGuard {
                    excludes.add("generated")
                }
            """.trimIndent()
        )
        projectDir.resolve("src/main/kotlin/generated").mkdirs()
        projectDir.resolve("src/main/kotlin/generated/GeneratedList.kt").writeText(
            """
                @Composable
                fun GeneratedList(products: List<Product>) {
                    LazyColumn {
                        items(products) { product ->
                            Text(product.name)
                        }
                    }
                }
            """.trimIndent()
        )

        projectDir.runGradle("composeGuard").build()

        val report = projectDir.report()
        assertContains(report, "Files analysed: 0")
        assertContains(report, "Total issues: 0")
        assertFalse(report.contains("CG001 HIGH"))
    }

    @Test
    fun `composeGuard can fail on medium threshold`() {
        val projectDir = fixture("composeguard-medium-threshold-")
        projectDir.writeBuildFile(
            """
                plugins { id("io.github.composeguard") }
                
                composeGuard {
                    failOnSeverity = "MEDIUM"
                }
            """.trimIndent()
        )
        projectDir.resolve("src/main/kotlin").mkdirs()
        projectDir.resolve("src/main/kotlin/SortScreen.kt").writeText(
            """
                @Composable
                fun SortScreen(products: List<Product>) {
                    val sorted = products.sortedBy { it.name }
                    Text(sorted.size.toString())
                }
            """.trimIndent()
        )

        val result = projectDir.runGradle("composeGuard").buildAndFail()

        assertEquals(TaskOutcome.FAILED, result.task(":composeGuard")?.outcome)
        val report = projectDir.report()
        assertContains(report, "CG002 MEDIUM")
        assertContains(report, "Build policy: FAIL (failOnSeverity=MEDIUM)")
    }

    @Test
    fun `composeGuard can suppress medium findings from report`() {
        val projectDir = fixture("composeguard-minimum-severity-")
        projectDir.writeBuildFile(
            """
                plugins { id("io.github.composeguard") }
                
                composeGuard {
                    minimumSeverity = "HIGH"
                }
            """.trimIndent()
        )
        projectDir.resolve("src/main/kotlin").mkdirs()
        projectDir.resolve("src/main/kotlin/SortScreen.kt").writeText(
            """
                @Composable
                fun SortScreen(products: List<Product>) {
                    val sorted = products.sortedBy { it.name }
                    Text(sorted.size.toString())
                }
            """.trimIndent()
        )

        projectDir.runGradle("composeGuard").build()

        val report = projectDir.report()
        assertContains(report, "Total issues: 0")
        assertFalse(report.contains("CG002 MEDIUM"))
    }

    private fun File.resolve(path: String): File = File(this, path)

    private fun fixture(prefix: String): File {
        val projectDir = createTempDirectory(prefix = prefix).toFile()
        projectDir.resolve("settings.gradle.kts").writeText(
            """pluginManagement { repositories { gradlePluginPortal(); mavenCentral(); google() } }"""
        )
        return projectDir
    }

    private fun File.writeBuildFile(contents: String = """plugins { id("io.github.composeguard") }""") {
        resolve("build.gradle.kts").writeText(contents)
    }

    private fun File.runGradle(vararg arguments: String): GradleRunner =
        GradleRunner.create()
            .withProjectDir(this)
            .withArguments(*arguments, "--stacktrace")
            .withPluginClasspath()

    private fun File.report(): String =
        resolve("build/reports/composeguard/composeguard.txt").readText()
}
