package com.housiness.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.housiness.HousinessApp
import com.housiness.data.db.entity.Order
import com.housiness.data.db.entity.OrderStatus
import com.housiness.ui.screens.invoice.InvoicePreviewScreen
import com.housiness.ui.screens.order.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersScreen() {
    val app = LocalContext.current.applicationContext as HousinessApp
    val vm: OrderViewModel = viewModel(
        factory = OrderViewModel.Factory(
            app.serviceLocator.orderRepository,
            app.serviceLocator.productRepository,
            app.serviceLocator.invoiceRepository
        )
    )
    val state by vm.state.collectAsStateWithLifecycle()

    if (state.showNewOrderScreen) {
        val products by vm.productRepository.getAllFlow()
            .collectAsStateWithLifecycle(initialValue = emptyList())
        NewOrderScreen(
            products = products,
            onDismiss = vm::hideNewOrder,
            onCreateOrder = vm::createOrder
        )
        return
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = vm::showNewOrder) {
                Icon(Icons.Filled.Add, contentDescription = "New order")
            }
        }
    ) { padding ->
        if (state.upcomingOrders.isEmpty() && state.completedOrders.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "\uD83D\uDCEB",
                    style = MaterialTheme.typography.headlineLarge
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "No orders yet",
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Tap + to add your first order",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Text(
                        text = "Upcoming Orders",
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }

                if (state.upcomingOrders.isEmpty()) {
                    item {
                        Text(
                            text = "No upcoming orders",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }

                items(state.upcomingOrders, key = { it.id }) { order ->
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = {
                            if (it == SwipeToDismissBoxValue.EndToStart) {
                                vm.markComplete(order)
                                true
                            } else false
                        }
                    )
                    SwipeToDismissBox(
                        state = dismissState,
                        enableDismissFromStartToEnd = false,
                        backgroundContent = {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.tertiaryContainer)
                                    .padding(horizontal = 24.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Text(
                                    text = "\u2705 Complete",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        },
                        content = {
                            OrderCard(order = order, onClick = { vm.selectOrder(order) })
                        }
                    )
                }

                item {
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { vm.toggleCompleted() },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Completed (${state.completedOrders.size})",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Icon(
                            if (state.showCompleted) Icons.Filled.ExpandLess
                            else Icons.Filled.ExpandMore,
                            contentDescription = "Toggle"
                        )
                    }
                }

                if (state.showCompleted && state.completedOrders.isNotEmpty()) {
                    items(state.completedOrders, key = { it.id }) { order ->
                        OrderCard(order = order, onClick = { vm.selectOrder(order) })
                    }
                }

                item { Spacer(Modifier.height(80.dp)) } // FAB clearance
            }
        }
    }

    if (state.showDetailSheet && state.selectedOrder != null) {
        OrderDetailSheet(
            order = state.selectedOrder!!,
            onDismiss = vm::dismissDetailSheet,
            onMarkComplete = { vm.markComplete(state.selectedOrder!!) },
            onGenerateInvoice = { vm.generateInvoice(state.selectedOrder!!) }
        )
    }

    val invoiceId = state.invoiceGeneratedId
    if (invoiceId != null) {
        InvoicePreviewScreen(
            invoiceId = invoiceId,
            onBack = {
                vm.clearInvoiceFlag()
            }
        )
    }
}
