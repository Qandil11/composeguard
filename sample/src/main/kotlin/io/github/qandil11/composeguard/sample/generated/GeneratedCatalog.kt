package io.github.qandil11.composeguard.sample.generated

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import io.github.qandil11.composeguard.sample.model.Product

@Composable
fun GeneratedCatalog(products: List<Product>) {
    LazyColumn {
        items(products) { product ->
            Text(product.name)
        }
    }
}
