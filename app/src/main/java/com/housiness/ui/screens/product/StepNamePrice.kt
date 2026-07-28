package com.housiness.ui.screens.product

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun StepNamePrice(
    name: String,
    sellingPrice: String,
    onNameChange: (String) -> Unit,
    onPriceChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = "What are you making?",
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = "Give your product a name and set your selling price.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("Product name") },
            placeholder = { Text("e.g. Chocolate Cake") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = sellingPrice,
            onValueChange = { onPriceChange(it.filter { c -> c.isDigit() }) },
            label = { Text("Selling price") },
            placeholder = { Text("e.g. 500") },
            singleLine = true,
            prefix = { Text("\u20B9 ") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
            )
        )
    }
}
