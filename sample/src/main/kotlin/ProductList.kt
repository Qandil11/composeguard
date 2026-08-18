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
