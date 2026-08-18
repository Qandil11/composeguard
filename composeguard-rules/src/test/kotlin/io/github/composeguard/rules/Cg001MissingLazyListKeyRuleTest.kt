package io.github.composeguard.rules

import io.github.composeguard.core.SourceFile
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class Cg001MissingLazyListKeyRuleTest {
    private val rule = Cg001MissingLazyListKeyRule()

    @Test
    fun `reports items without stable key inside LazyColumn`() {
        val source = """
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

        val issues = rule.analyze(SourceFile("ProductList.kt", "/src/ProductList.kt", source))

        assertEquals(1, issues.size)
        assertEquals("CG001", issues.single().id)
        assertEquals(7, issues.single().line)
        assertEquals("items(products) { ... }", issues.single().detected)
    }

    @Test
    fun `does not report items with key parameter`() {
        val source = """
            @Composable
            fun ProductList(products: List<Product>) {
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

        val issues = rule.analyze(SourceFile("ProductList.kt", "/src/ProductList.kt", source))

        assertTrue(issues.isEmpty())
    }
}
