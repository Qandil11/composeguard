package io.github.composeguard.ide

import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class Cg001MissingLazyListKeyInspectionTest : BasePlatformTestCase() {
    fun testReportsMissingStableKey() {
        myFixture.enableInspections(Cg001MissingLazyListKeyInspection())
        myFixture.configureByText(
            "ProductList.kt",
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

        val composeGuardWarnings = myFixture.doHighlighting(HighlightSeverity.WARNING)
            .filter { it.description?.startsWith("CG001: Missing stable key in lazy list") == true }

        assertEquals(1, composeGuardWarnings.size)
        assertEquals(
            "items(products) { product ->\n            Text(product.name)\n        }",
            composeGuardWarnings.single().text
        )
    }

    fun testDoesNotReportKeyedLazyList() {
        myFixture.enableInspections(Cg001MissingLazyListKeyInspection())
        myFixture.configureByText(
            "ProductList.kt",
            """
                import androidx.compose.foundation.lazy.LazyColumn
                import androidx.compose.foundation.lazy.items

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
        )

        val composeGuardWarnings = myFixture.doHighlighting(HighlightSeverity.WARNING)
            .filter { it.description?.startsWith("CG001: Missing stable key in lazy list") == true }

        assertEmpty(composeGuardWarnings)
    }
}
