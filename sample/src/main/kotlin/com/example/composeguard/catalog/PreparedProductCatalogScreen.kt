package com.example.composeguard.catalog

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.example.composeguard.model.Product

@Composable
fun PreparedProductCatalogScreen(preparedProducts: List<Product>) {
    LazyColumn {
        items(
            items = preparedProducts,
            key = { it.id }
        ) { product ->
            Text(product.name)
        }
    }
}
