package io.github.composeguard.rules

import io.github.composeguard.core.SourceFile
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class Cg002CollectionOperationInCompositionRuleTest {
    private val rule = Cg002CollectionOperationInCompositionRule()

    @Test
    fun `reports direct collection transformation in composable body`() {
        val source = """
            @Composable
            fun ProductList(products: List<Product>) {
                val sorted = products.sortedBy { it.name }
                LazyColumn {
                    items(sorted, key = { it.id }) { product ->
                        Text(product.name)
                    }
                }
            }
        """.trimIndent()

        val issues = rule.analyze(SourceFile("ProductList.kt", "/src/ProductList.kt", source))

        assertEquals(1, issues.size)
        assertEquals("CG002", issues.single().id)
        assertEquals(3, issues.single().line)
        assertEquals("products.sortedBy { it.name }", issues.single().detected)
    }

    @Test
    fun `does not report transformation cached in remember lambda`() {
        val source = """
            @Composable
            fun ProductList(products: List<Product>) {
                val sorted = remember(products) {
                    products.sortedBy { it.name }
                }
                Text(sorted.size.toString())
            }
        """.trimIndent()

        val issues = rule.analyze(SourceFile("ProductList.kt", "/src/ProductList.kt", source))

        assertTrue(issues.isEmpty())
    }
}
