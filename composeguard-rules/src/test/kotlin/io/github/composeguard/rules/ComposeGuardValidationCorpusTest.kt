package io.github.composeguard.rules

import io.github.composeguard.core.ComposeGuardAnalyzer
import io.github.composeguard.core.SourceFile
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ComposeGuardValidationCorpusTest {
    private val analyzer = ComposeGuardAnalyzer(ComposeGuardRules.phaseTwo())

    @Test
    fun `validates CG001 positives and negatives across realistic lazy list syntax`() {
        val source = """
            @Composable
            fun ProductList(products: List<Product>) {
                LazyColumn {
                    items(products) { product ->
                        Text(product.name)
                    }
                    itemsIndexed(products) { index, product ->
                        Text("${'$'}index ${'$'}{product.name}")
                    }
                    items(
                        items = products,
                        key = { it.id }
                    ) { product ->
                        Text(product.name)
                    }
                }
            }
            
            @Composable
            fun QualifiedList(products: List<Product>) {
                androidx.compose.foundation.lazy.LazyRow {
                    androidx.compose.foundation.lazy.items(products) { product ->
                        Text(product.name)
                    }
                }
            }
            
            fun unrelatedItems(products: List<Product>) {
                items(products)
            }
        """.trimIndent()

        val issues = analyzer.analyze(listOf(SourceFile("LazyLists.kt", "/src/LazyLists.kt", source)))
        val cg001 = issues.filter { it.id == "CG001" }

        assertEquals(3, cg001.size)
        assertTrue(cg001.any { it.detected == "items(products) { ... }" })
        assertTrue(cg001.any { it.detected == "itemsIndexed(products) { ... }" })
        assertFalse(cg001.any { it.line == 11 })
    }

    @Test
    fun `validates CG002 direct composition work while avoiding lambdas and unrelated functions`() {
        val source = """
            @Composable
            fun ProductSummary(products: List<Product>) {
                val sorted = products.sortedBy { it.name }
                val available =
                    products
                        .filter { it.available }
                val cached = remember(products) {
                    products.map { it.name }
                }
                Button(onClick = {
                    val clicked = products.sortedBy { it.name }
                    println(clicked)
                }) {
                    Text(sorted.first().name)
                }
            }
            
            @Composable
            fun Parent(products: List<Product>) {
                @Composable
                fun Nested(items: List<Product>) {
                    val grouped = items.groupBy { it.category }
                    Text(grouped.size.toString())
                }
                Nested(products)
            }
            
            fun sortOutsideComposition(products: List<Product>) {
                val sorted = products.sortedBy { it.name }
                println(sorted)
            }
        """.trimIndent()

        val issues = analyzer.analyze(listOf(SourceFile("Collections.kt", "/src/Collections.kt", source)))
        val cg002 = issues.filter { it.id == "CG002" }

        assertEquals(3, cg002.size)
        assertTrue(cg002.any { it.detected == "products.sortedBy { it.name }" })
        assertTrue(cg002.any { it.detected?.contains(".filter { it.available }") == true })
        assertTrue(cg002.any { it.detected == "items.groupBy { it.category }" })
        assertFalse(cg002.any { it.detected?.contains("products.map") == true })
    }

    @Test
    fun `validates CG003 mutable state factories and clean immutable state`() {
        val source = """
            @Composable
            fun UsersScreen() {
                var users by remember {
                    mutableStateOf(mutableListOf<User>())
                }
                val cache = remember {
                    mutableStateOf(HashMap<String, User>())
                }
                val clean = remember {
                    mutableStateOf(emptyList<User>())
                }
                Text(clean.value.size.toString())
            }
            
            fun unrelatedFactory() {
                val users = mutableListOf<User>()
                println(users)
            }
        """.trimIndent()

        val issues = analyzer.analyze(listOf(SourceFile("MutableState.kt", "/src/MutableState.kt", source)))
        val cg003 = issues.filter { it.id == "CG003" }

        assertEquals(2, cg003.size)
        assertTrue(cg003.any { it.detected == "mutableStateOf(mutableListOf<User>())" })
        assertTrue(cg003.any { it.detected == "mutableStateOf(HashMap<String, User>())" })
        assertFalse(cg003.any { it.detected?.contains("emptyList") == true })
    }

    @Test
    fun `validates CG004 direct writes while avoiding event handlers effects and unrelated functions`() {
        val source = """
            @Composable
            fun Counter() {
                var count by remember { mutableStateOf(0) }
                val holder = remember { mutableStateOf(0) }
                count++
                holder.value =
                    2
                Button(onClick = { count++ }) {
                    Text(count.toString())
                }
                LaunchedEffect(Unit) {
                    holder.value = 3
                }
            }
            
            @Composable
            fun Parent() {
                @Composable
                fun Nested() {
                    var nested by remember { mutableStateOf(0) }
                    nested += 1
                }
                Nested()
            }
            
            fun updateCounter() {
                var count = 0
                count++
            }
        """.trimIndent()

        val issues = analyzer.analyze(listOf(SourceFile("Writes.kt", "/src/Writes.kt", source)))
        val cg004 = issues.filter { it.id == "CG004" }

        assertEquals(3, cg004.size)
        assertTrue(cg004.any { it.detected == "count++" })
        assertTrue(cg004.any { it.detected?.contains("holder.value") == true })
        assertTrue(cg004.any { it.detected == "nested += 1" })
        assertFalse(cg004.any { it.detected == "holder.value = 3" })
    }
}
