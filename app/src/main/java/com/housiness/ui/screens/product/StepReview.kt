package com.housiness.ui.screens.product

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun StepReview(
    name: String,
    sellingPrice: Long,
    costSource: com.housiness.data.db.entity.CostSource,
    makingCost: Long,
    materials: List<MaterialUiState>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = "Review your product",
            style = MaterialTheme.typography.headlineMedium
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Divider()
                DetailRow("Selling price", "\u20B9 $sellingPrice")
                DetailRow("Making cost", "\u20B9 $makingCost")
                DetailRow("Profit", "\u20B9 ${sellingPrice - makingCost}")
                DetailRow("Margin", if (sellingPrice > 0) "${((sellingPrice - makingCost).toFloat() / sellingPrice * 100).toInt()}%" else "-")
            }
        }

        if (costSource == com.housiness.data.db.entity.CostSource.CALCULATED && materials.isNotEmpty()) {
            Text(
                text = "Materials used",
                style = MaterialTheme.typography.titleMedium
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                materials.forEach { mat ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(mat.name.ifBlank { "Material" }, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "\u20B9 ${mat.calculatedCost}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
    }
}
