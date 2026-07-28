package com.housiness.ui.screens.product

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.housiness.HousinessApp
import com.housiness.data.db.entity.Product
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductEditScreen(
    productId: Long,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    val app = LocalContext.current.applicationContext as HousinessApp
    val repo = app.serviceLocator.productRepository
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var sellingPrice by remember { mutableStateOf("") }
    var makingCost by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }
    var loaded by remember { mutableStateOf(false) }

    LaunchedEffect(productId) {
        val product = repo.getById(productId) ?: return@LaunchedEffect
        name = product.name
        sellingPrice = product.sellingPrice.toString()
        makingCost = product.makingCost.toString()
        loaded = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Product") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            val price = sellingPrice.toLongOrNull() ?: return@TextButton
                            val cost = makingCost.toLongOrNull() ?: return@TextButton
                            if (name.isBlank()) return@TextButton
                            scope.launch {
                                isSaving = true
                                repo.update(
                                    Product(
                                        id = productId,
                                        name = name,
                                        sellingPrice = price,
                                        makingCost = cost,
                                        costSource = com.housiness.data.db.entity.CostSource.MANUAL,
                                        createdAt = System.currentTimeMillis()
                                    )
                                )
                                isSaving = false
                                onSaved()
                            }
                        },
                        enabled = name.isNotBlank() && sellingPrice.toLongOrNull() != null && makingCost.toLongOrNull() != null && !isSaving
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        } else {
                            Text("Save")
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (!loaded) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = androidx.compose.ui.Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Product name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = sellingPrice,
                    onValueChange = { v -> sellingPrice = v.filter { c -> c.isDigit() } },
                    label = { Text("Selling price") },
                    singleLine = true,
                    prefix = { Text("\u20B9 ") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = makingCost,
                    onValueChange = { v -> makingCost = v.filter { c -> c.isDigit() } },
                    label = { Text("Making cost") },
                    singleLine = true,
                    prefix = { Text("\u20B9 ") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
