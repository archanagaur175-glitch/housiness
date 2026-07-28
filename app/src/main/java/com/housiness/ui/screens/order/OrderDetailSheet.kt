package com.housiness.ui.screens.order

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.housiness.data.db.entity.Order
import com.housiness.util.CurrencyFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailSheet(
    order: Order,
    onDismiss: () -> Unit,
    onMarkComplete: () -> Unit,
    onGenerateInvoice: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = order.productName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            HorizontalDivider()

            DetailRow("Quantity", "x${order.quantity}")
            DetailRow("Total price", CurrencyFormatter.format(order.totalPrice))
            DetailRow("Total cost", CurrencyFormatter.format(order.totalCost))
            DetailRow("Profit", CurrencyFormatter.format(order.totalPrice - order.totalCost))
            DetailRow("Status", if (order.status == com.housiness.data.db.entity.OrderStatus.COMPLETED) "Completed" else "Upcoming")

            Spacer(Modifier.height(8.dp))

            if (order.status == com.housiness.data.db.entity.OrderStatus.UPCOMING) {
                Button(
                    onClick = onMarkComplete,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Mark as Complete")
                }

                OutlinedButton(
                    onClick = onGenerateInvoice,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Description, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Generate Invoice")
                }
            } else {
                OutlinedButton(
                    onClick = onGenerateInvoice,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Description, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Generate Invoice")
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
