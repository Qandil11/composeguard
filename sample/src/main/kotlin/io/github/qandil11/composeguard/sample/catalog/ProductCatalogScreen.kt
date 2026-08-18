package io.github.qandil11.composeguard.sample.catalog

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import io.github.qandil11.composeguard.sample.model.Product
import io.github.qandil11.composeguard.sample.model.User

@Composable
fun ProductCatalogScreen(products: List<Product>) {
    var selectedUsers by remember {
        mutableStateOf(mutableListOf<User>())
    }
    var refreshCount by remember { mutableStateOf(0) }
    val availableProducts = products.filter { it.isAvailable }

    LazyColumn {
        items(availableProducts) { product ->
            Text(product.name)
        }
    }

    refreshCount++
    Text(selectedUsers.size.toString())
}
