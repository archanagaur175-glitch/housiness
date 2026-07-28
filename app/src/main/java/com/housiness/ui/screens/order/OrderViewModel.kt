package com.housiness.ui.screens.order

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.housiness.data.db.entity.Order
import com.housiness.data.db.entity.OrderStatus
import com.housiness.data.repository.InvoiceRepository
import com.housiness.data.repository.OrderRepository
import com.housiness.data.repository.ProductRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class OrderUiState(
    val upcomingOrders: List<Order> = emptyList(),
    val completedOrders: List<Order> = emptyList(),
    val showCompleted: Boolean = false,
    val selectedOrder: Order? = null,
    val showDetailSheet: Boolean = false,
    val showNewOrderScreen: Boolean = false,
    val invoiceGeneratedId: Long? = null
)

class OrderViewModel(
    private val orderRepository: OrderRepository,
    val productRepository: ProductRepository,
    private val invoiceRepository: InvoiceRepository
) : ViewModel() {

    private val _state = MutableStateFlow(OrderUiState())
    val state: StateFlow<OrderUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            orderRepository.getByStatusFlow(OrderStatus.UPCOMING).collect { orders ->
                _state.update { it.copy(upcomingOrders = orders) }
            }
        }
        viewModelScope.launch {
            orderRepository.getByStatusFlow(OrderStatus.COMPLETED).collect { orders ->
                _state.update { it.copy(completedOrders = orders) }
            }
        }
    }

    fun showNewOrder() { _state.update { it.copy(showNewOrderScreen = true) } }
    fun hideNewOrder() { _state.update { it.copy(showNewOrderScreen = false) } }

    fun toggleCompleted() { _state.update { it.copy(showCompleted = !it.showCompleted) } }

    fun selectOrder(order: Order) {
        _state.update { it.copy(selectedOrder = order, showDetailSheet = true) }
    }

    fun dismissDetailSheet() {
        _state.update { it.copy(selectedOrder = null, showDetailSheet = false) }
    }

    fun markComplete(order: Order) {
        viewModelScope.launch {
            orderRepository.update(order.copy(status = OrderStatus.COMPLETED))
            _state.update { it.copy(selectedOrder = null, showDetailSheet = false) }
        }
    }

    fun generateInvoice(order: Order) {
        viewModelScope.launch {
            try {
                val ref = invoiceRepository.getNextInvoiceNumber()
                val invoice = com.housiness.data.db.entity.Invoice(
                    orderId = order.id,
                    orderRef = ref,
                    subtotal = order.totalPrice,
                    generatedAt = System.currentTimeMillis()
                )
                val invoiceId = invoiceRepository.insert(invoice)

                val safeQty = if (order.quantity > 0) order.quantity else 1
                val lineItem = com.housiness.data.db.entity.InvoiceLineItem(
                    invoiceId = invoiceId,
                    productName = order.productName,
                    quantity = order.quantity,
                    unitPrice = order.totalPrice / safeQty,
                    total = order.totalPrice
                )
                invoiceRepository.saveLineItems(listOf(lineItem))

                orderRepository.update(order.copy(invoiceId = invoiceId, status = OrderStatus.COMPLETED))
                _state.update { it.copy(invoiceGeneratedId = invoiceId, showDetailSheet = false) }
            } catch (_: Exception) { }
        }
    }

    fun clearInvoiceFlag() { _state.update { it.copy(invoiceGeneratedId = null) } }

    fun createOrder(productId: Long, productName: String, price: Long, cost: Long, quantity: Int, dateTime: Long) {
        viewModelScope.launch {
            orderRepository.insert(
                Order(
                    productId = productId,
                    productName = productName,
                    quantity = quantity,
                    totalPrice = price * quantity,
                    totalCost = cost * quantity,
                    status = OrderStatus.UPCOMING,
                    dateTime = dateTime,
                    createdAt = System.currentTimeMillis()
                )
            )
            hideNewOrder()
        }
    }

    class Factory(
        private val orderRepository: OrderRepository,
        private val productRepository: ProductRepository,
        private val invoiceRepository: InvoiceRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            OrderViewModel(orderRepository, productRepository, invoiceRepository) as T
    }
}
