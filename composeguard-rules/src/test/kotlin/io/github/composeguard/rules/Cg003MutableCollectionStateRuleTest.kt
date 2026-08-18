package io.github.composeguard.rules

import io.github.composeguard.core.SourceFile
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class Cg003MutableCollectionStateRuleTest {
    private val rule = Cg003MutableCollectionStateRule()

    @Test
    fun `reports mutable list inside mutableStateOf`() {
        val source = """
            @Composable
            fun Screen() {
                var users by remember {
                    mutableStateOf(mutableListOf<User>())
                }
            }
        """.trimIndent()

        val issues = rule.analyze(SourceFile("Users.kt", "/src/Users.kt", source))

        assertEquals(1, issues.size)
        assertEquals("CG003", issues.single().id)
        assertEquals(4, issues.single().line)
    }

    @Test
    fun `does not report immutable list inside mutableStateOf`() {
        val source = """
            @Composable
            fun Screen() {
                var users by remember {
                    mutableStateOf(emptyList<User>())
                }
            }
        """.trimIndent()

        val issues = rule.analyze(SourceFile("Users.kt", "/src/Users.kt", source))

        assertTrue(issues.isEmpty())
    }
}
