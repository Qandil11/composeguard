package io.github.composeguard.rules

import io.github.composeguard.core.SourceFile
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class Cg004StateWriteDuringCompositionRuleTest {
    private val rule = Cg004StateWriteDuringCompositionRule()

    @Test
    fun `reports delegated state increment in composable body`() {
        val source = """
            @Composable
            fun Counter() {
                var count by remember { mutableStateOf(0) }
                Text("${'$'}count")
                count++
            }
        """.trimIndent()

        val issues = rule.analyze(SourceFile("Counter.kt", "/src/Counter.kt", source))

        assertEquals(1, issues.size)
        assertEquals("CG004", issues.single().id)
        assertEquals(5, issues.single().line)
        assertEquals("count++", issues.single().detected)
    }

    @Test
    fun `reports mutable state value assignment in composable body`() {
        val source = """
            @Composable
            fun Counter() {
                val count = remember { mutableStateOf(0) }
                Text("${'$'}{count.value}")
                count.value = 1
            }
        """.trimIndent()

        val issues = rule.analyze(SourceFile("Counter.kt", "/src/Counter.kt", source))

        assertEquals(1, issues.size)
        assertEquals("CG004", issues.single().id)
        assertEquals("count.value = 1", issues.single().detected)
    }

    @Test
    fun `does not report state write inside event handler lambda`() {
        val source = """
            @Composable
            fun Counter() {
                var count by remember { mutableStateOf(0) }
                Button(onClick = { count++ }) {
                    Text("${'$'}count")
                }
            }
        """.trimIndent()

        val issues = rule.analyze(SourceFile("Counter.kt", "/src/Counter.kt", source))

        assertTrue(issues.isEmpty())
    }

    @Test
    fun `does not report state write inside LaunchedEffect`() {
        val source = """
            @Composable
            fun Counter() {
                var count by remember { mutableStateOf(0) }
                LaunchedEffect(Unit) {
                    count = 1
                }
            }
        """.trimIndent()

        val issues = rule.analyze(SourceFile("Counter.kt", "/src/Counter.kt", source))

        assertTrue(issues.isEmpty())
    }
}
