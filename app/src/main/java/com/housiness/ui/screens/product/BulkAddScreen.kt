package com.housiness.ui.screens.product

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.housiness.data.db.entity.CostSource
import com.housiness.data.db.entity.Product
import com.housiness.data.repository.ProductRepository
import kotlinx.coroutines.launch
import java.util.UUID

data class BulkProductEntry(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val sellingPrice: String = "",
    val makingCost: String = ""
) {
    val isValid: Boolean get() = name.isNotBlank() && sellingPrice.toLongOrNull() != null && makingCost.toLongOrNull() != null
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BulkAddScreen(
    productRepository: ProductRepository,
    onBack: () -> Unit
) {
    var entries by remember { mutableStateOf(listOf(BulkProductEntry())) }
    val scope = rememberCoroutineScope()
    var isSaving by remember { mutableStateOf(false) }
    var savedCount by remember { mutableStateOf(0) }
    var error by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bulk Add Products") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Add multiple products at once",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (savedCount > 0) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Text(
                        text = "$savedCount product(s) saved successfully!",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            if (error != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Text(
                        text = error!!,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            entries.forEach { entry ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Product ${entries.indexOf(entry) + 1}", style = MaterialTheme.typography.titleSmall)
                            if (entries.size > 1) {
                                IconButton(onClick = { entries = entries.filter { it.id != entry.id } }) {
                                    Icon(Icons.Filled.Close, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                        OutlinedTextField(
                            value = entry.name,
                            onValueChange = { v ->
                                entries = entries.map { if (it.id == entry.id) it.copy(name = v) else it }
                            },
                            label = { Text("Product name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = entry.sellingPrice,
                                onValueChange = { v ->
                                    val digits = v.filter { c -> c.isDigit() }
                                    entries = entries.map { if (it.id == entry.id) it.copy(sellingPrice = digits) else it }
                                },
                                label = { Text("Selling price") },
                                singleLine = true,
                                prefix = { Text("\u20B9 ") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                                )
                            )
                            OutlinedTextField(
                                value = entry.makingCost,
                                onValueChange = { v ->
                                    val digits = v.filter { c -> c.isDigit() }
                                    entries = entries.map { if (it.id == entry.id) it.copy(makingCost = digits) else it }
                                },
                                label = { Text("Making cost") },
                                singleLine = true,
                                prefix = { Text("\u20B9 ") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                                )
                            )
                        }
                    }
                }
            }

            FilledTonalButton(
                onClick = { entries = entries + BulkProductEntry() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text("Add another product")
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    val valid = entries.filter { it.isValid }
                    if (valid.isEmpty()) {
                        error = "Fill in at least one product with valid name, price, and cost"
                        return@Button
                    }
                    error = null
                    isSaving = true
                    scope.launch {
                        try {
                            val now = System.currentTimeMillis()
                            valid.forEach { entry ->
                                productRepository.insert(
                                    Product(
                                        name = entry.name,
                                        sellingPrice = entry.sellingPrice.toLong(),
                                        makingCost = entry.makingCost.toLong(),
                                        costSource = CostSource.MANUAL,
                                        createdAt = now
                                    )
                                )
                            }
                            savedCount += valid.size
                            entries = listOf(BulkProductEntry())
                        } catch (e: Exception) {
                            error = e.message ?: "Save failed"
                        } finally {
                            isSaving = false
                        }
                    }
                },
                enabled = !isSaving && entries.any { it.isValid },
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                }
                Text("Save ${entries.count { it.isValid }} product(s)")
            }
        }
    }
}
