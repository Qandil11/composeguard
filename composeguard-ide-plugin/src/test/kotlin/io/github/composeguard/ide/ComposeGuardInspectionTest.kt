package io.github.composeguard.ide

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class ComposeGuardInspectionTest : BasePlatformTestCase() {
    override fun setUp() {
        super.setUp()
        myFixture.enableInspections(
            Cg001MissingLazyListKeyInspection(),
            Cg002CollectionOperationInspection(),
            Cg003MutableCollectionStateInspection(),
            Cg004StateWriteInspection()
        )
    }

    fun testCg001ReportsMissingStableKey() {
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

        val problems = composeGuardProblems("CG001")

        assertEquals(1, problems.size)
        assertEquals(
            "items(products) { product ->\n            Text(product.name)\n        }",
            problems.single().text
        )
    }

    fun testCg001DoesNotReportKeyedLazyList() {
        myFixture.configureByText(
            "ProductList.kt",
            """
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

        assertEmpty(composeGuardProblems("CG001"))
    }

    fun testCg002ReportsCollectionTransformationInComposableBody() {
        myFixture.configureByText(
            "ProductList.kt",
            """
                @Composable
                fun ProductList(products: List<Product>) {
                    val sorted = products.sortedBy { it.name }
                    Text(sorted.size.toString())
                }
            """.trimIndent()
        )

        val problems = composeGuardProblems("CG002")

        assertEquals(1, problems.size)
        assertEquals("products.sortedBy { it.name }", problems.single().text)
    }

    fun testCg002DoesNotReportRememberLambdaTransformation() {
        myFixture.configureByText(
            "ProductList.kt",
            """
                @Composable
                fun ProductList(products: List<Product>) {
                    val sorted = remember(products) {
                        products.sortedBy { it.name }
                    }
                    Text(sorted.size.toString())
                }
            """.trimIndent()
        )

        assertEmpty(composeGuardProblems("CG002"))
    }

    fun testCg003ReportsMutableCollectionState() {
        myFixture.configureByText(
            "Users.kt",
            """
                @Composable
                fun Screen() {
                    var users by remember {
                        mutableStateOf(mutableListOf<User>())
                    }
                }
            """.trimIndent()
        )

        val problems = composeGuardProblems("CG003")

        assertEquals(1, problems.size)
        assertEquals("mutableStateOf(mutableListOf<User>())", problems.single().text)
    }

    fun testCg003DoesNotReportImmutableCollectionState() {
        myFixture.configureByText(
            "Users.kt",
            """
                @Composable
                fun Screen() {
                    var users by remember {
                        mutableStateOf(emptyList<User>())
                    }
                }
            """.trimIndent()
        )

        assertEmpty(composeGuardProblems("CG003"))
    }

    fun testCg004ReportsStateWriteDuringComposition() {
        myFixture.configureByText(
            "Counter.kt",
            """
                @Composable
                fun Counter() {
                    var count by remember { mutableStateOf(0) }
                    Text("${'$'}count")
                    count++
                }
            """.trimIndent()
        )

        val problems = composeGuardProblems("CG004")

        assertEquals(1, problems.size)
        assertEquals("count++", problems.single().text)
    }

    fun testCg004DoesNotReportEventHandlerStateWrite() {
        myFixture.configureByText(
            "Counter.kt",
            """
                @Composable
                fun Counter() {
                    var count by remember { mutableStateOf(0) }
                    Button(onClick = { count++ }) {
                        Text("${'$'}count")
                    }
                }
            """.trimIndent()
        )

        assertEmpty(composeGuardProblems("CG004"))
    }

    fun testSuppressionByRuleId() {
        myFixture.configureByText(
            "Counter.kt",
            """
                @Suppress("CG004")
                @Composable
                fun Counter() {
                    var count by remember { mutableStateOf(0) }
                    count++
                }
            """.trimIndent()
        )

        assertEmpty(composeGuardProblems("CG004"))
    }

    private fun composeGuardProblems(ruleId: String) =
        myFixture.doHighlighting()
            .filter { it.description?.startsWith("$ruleId:") == true }
}
