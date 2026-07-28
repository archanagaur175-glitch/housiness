package com.housiness.ui.screens.product

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.housiness.data.db.entity.CostSource
import com.housiness.data.db.entity.PantryItem
import com.housiness.data.db.entity.UnitType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StepCost(
    costSource: CostSource,
    manualCost: String,
    materials: List<MaterialUiState>,
    pantryItems: List<PantryItem>,
    onCostSourceChange: (CostSource) -> Unit,
    onManualCostChange: (String) -> Unit,
    onAddMaterial: () -> Unit,
    onUpdateMaterial: (String, (MaterialUiState) -> MaterialUiState) -> Unit,
    onRemoveMaterial: (String) -> Unit,
    onPickFromPantry: (PantryItem) -> Unit
) {
    var showPantryPicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "What does it cost to make?",
            style = MaterialTheme.typography.headlineMedium
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FilterChip(
                selected = costSource == CostSource.MANUAL,
                onClick = { onCostSourceChange(CostSource.MANUAL) },
                label = { Text("Enter total cost") }
            )
            FilterChip(
                selected = costSource == CostSource.CALCULATED,
                onClick = { onCostSourceChange(CostSource.CALCULATED) },
                label = { Text("Calculate from materials") }
            )
        }

        AnimatedContent(targetState = costSource, label = "cost_mode") { mode ->
            when (mode) {
                CostSource.MANUAL -> {
                    OutlinedTextField(
                        value = manualCost,
                        onValueChange = { onManualCostChange(it.filter { c -> c.isDigit() }) },
                        label = { Text("Total making cost") },
                        placeholder = { Text("e.g. 200") },
                        singleLine = true,
                        prefix = { Text("\u20B9 ") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                        )
                    )
                }

                CostSource.CALCULATED -> {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        materials.forEachIndexed { index, material ->
                            MaterialRow(
                                index = index,
                                material = material,
                                onUpdate = { fn -> onUpdateMaterial(material.tempId, fn) },
                                onRemove = { onRemoveMaterial(material.tempId) }
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            FilledTonalButton(onClick = onAddMaterial) {
                                Icon(Icons.Filled.Add, contentDescription = null)
                                Spacer(Modifier.width(4.dp))
                                Text("Add material")
                            }
                            if (pantryItems.isNotEmpty()) {
                                FilledTonalButton(onClick = { showPantryPicker = true }) {
                                    Icon(Icons.Filled.Menu, contentDescription = null)
                                    Spacer(Modifier.width(4.dp))
                                    Text("From pantry")
                                }
                            }
                        }

                        if (materials.isNotEmpty()) {
                            val total = materials.sumOf { it.calculatedCost }
                            Text(
                                text = "Total cost: \u20B9 $total",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
            }
        }
    }

    if (showPantryPicker) {
        PantryPickerSheet(
            items = pantryItems,
            onSelect = { item ->
                onPickFromPantry(item)
                showPantryPicker = false
            },
            onDismiss = { showPantryPicker = false }
        )
    }
}

@Composable
private fun MaterialRow(
    index: Int,
    material: MaterialUiState,
    onUpdate: ((MaterialUiState) -> MaterialUiState) -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Material ${index + 1}", style = MaterialTheme.typography.titleSmall)
                IconButton(onClick = onRemove) {
                    Icon(Icons.Filled.Delete, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error)
                }
            }

            OutlinedTextField(
                value = material.name,
                onValueChange = { onUpdate { it.copy(name = it.name) } },
                label = { Text("Material name") },
                placeholder = { Text("e.g. Flour") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = material.entryMode == MaterialEntryMode.FLAT,
                    onClick = { onUpdate { it.copy(entryMode = MaterialEntryMode.FLAT) } },
                    label = { Text("Flat cost") }
                )
                FilterChip(
                    selected = material.entryMode == MaterialEntryMode.UNIT_BASED,
                    onClick = { onUpdate { it.copy(entryMode = MaterialEntryMode.UNIT_BASED) } },
                    label = { Text("Per unit") }
                )
            }

            when (material.entryMode) {
                MaterialEntryMode.FLAT -> {
                    OutlinedTextField(
                        value = material.flatCost,
                        onValueChange = { v ->
                            val digits = v.filter { c -> c.isDigit() }
                            onUpdate { it.copy(flatCost = digits) }
                        },
                        label = { Text("Cost") },
                        singleLine = true,
                        prefix = { Text("\u20B9 ") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                        )
                    )
                }

                MaterialEntryMode.UNIT_BASED -> {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = material.pricePaid,
                            onValueChange = { v ->
                                val digits = v.filter { c -> c.isDigit() }
                                onUpdate { it.copy(pricePaid = digits) }
                            },
                            label = { Text("Price paid") },
                            singleLine = true,
                            prefix = { Text("\u20B9 ") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                            )
                        )
                        OutlinedTextField(
                            value = material.unitSize,
                            onValueChange = { v ->
                                val digits = v.filter { c -> c.isDigit() }
                                onUpdate { it.copy(unitSize = digits) }
                            },
                            label = { Text("Size") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                            )
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = material.quantityUsed,
                            onValueChange = { v ->
                                val digits = v.filter { c -> c.isDigit() }
                                onUpdate { it.copy(quantityUsed = digits) }
                            },
                            label = { Text("Quantity used") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                            )
                        )
                        var expanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = it }
                        ) {
                            OutlinedTextField(
                                value = material.unitType.name,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Unit") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                                modifier = Modifier
                                    .weight(1f)
                                    .menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                UnitType.entries.forEach { unit ->
                                    DropdownMenuItem(
                                        text = { Text(unit.name) },
                                        onClick = {
                                            onUpdate { it.copy(unitType = unit) }
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (material.calculatedCost > 0) {
                Text(
                    text = "Costs: \u20B9 ${material.calculatedCost}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = material.saveToPantry,
                    onCheckedChange = { onUpdate { it.copy(saveToPantry = it.saveToPantry) } }
                )
                Text(
                    "Save to pantry",
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}
