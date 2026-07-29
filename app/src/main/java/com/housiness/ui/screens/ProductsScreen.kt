package com.housiness.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.housiness.HousinessApp
import com.housiness.data.db.entity.Product
import com.housiness.ui.screens.product.AddProductScreen
import com.housiness.ui.screens.product.BulkAddScreen
import com.housiness.ui.screens.product.ProductEditScreen
import com.housiness.util.CurrencyFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsScreen() {
    val app = LocalContext.current.applicationContext as HousinessApp
    val repo = app.serviceLocator.productRepository
    val products by repo.getAllFlow().collectAsStateWithLifecycle(initialValue = emptyList())

    var showAddScreen by remember { mutableStateOf(false) }
    var showBulkAdd by remember { mutableStateOf(false) }
    var editProductId by remember { mutableStateOf<Long?>(null) }

    if (showAddScreen) {
        AddProductScreen(
            onSaved = { showAddScreen = false },
            onBack = { showAddScreen = false }
        )
        return
    }

    if (showBulkAdd) {
        BulkAddScreen(
            productRepository = repo,
            onBack = { showBulkAdd = false }
        )
        return
    }

    if (editProductId != null) {
        ProductEditScreen(
            productId = editProductId!!,
            onBack = { editProductId = null },
            onSaved = { editProductId = null }
        )
        return
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddScreen = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Add product")
            }
        }
    ) { padding ->
        if (products.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "\uD83D\uDCE6",
                    style = MaterialTheme.typography.headlineLarge
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Add Your First Product",
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Tell us what you make and what it costs",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(24.dp))
                FilledTonalButton(onClick = { showBulkAdd = true }) {
                    Icon(Icons.Filled.PlaylistAdd, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Bulk add products")
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Your Products",
                            style = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        FilledTonalButton(onClick = { showBulkAdd = true }) {
                            Icon(Icons.Filled.PlaylistAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Bulk", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
                items(products, key = { it.id }) { product ->
                    ProductCard(
                        product = product,
                        onEdit = { editProductId = product.id }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProductCard(product: Product, onEdit: () -> Unit) {
    Card(
        onClick = onEdit,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = product.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Selling price", style = MaterialTheme.typography.labelMedium)
                    Text(
                        CurrencyFormatter.format(product.sellingPrice),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Making cost", style = MaterialTheme.typography.labelMedium)
                    Text(
                        CurrencyFormatter.format(product.makingCost),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            val profit = product.sellingPrice - product.makingCost
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (profit >= 0) "\u2705 Profit: ${CurrencyFormatter.format(profit)}" else "\u26A0\uFE0F Loss: ${CurrencyFormatter.format(-profit)}",
                    style = MaterialTheme.typography.labelLarge,
                    color = if (profit >= 0) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
