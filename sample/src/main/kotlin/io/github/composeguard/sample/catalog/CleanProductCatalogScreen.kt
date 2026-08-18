package io.github.composeguard.sample.catalog

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import io.github.composeguard.sample.model.Product

@Composable
fun CleanProductCatalogScreen(products: List<Product>) {
    val selectedId = remember { mutableStateOf<String?>(null) }

    LazyColumn {
        items(
            items = products,
            key = { it.id }
        ) { product ->
            Button(onClick = { selectedId.value = product.id }) {
                Text(product.name)
            }
        }
    }
}
