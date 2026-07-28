package com.housiness.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.housiness.data.repository.OrderRepository
import com.housiness.data.repository.ProductRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class FinancialSummary(
    val revenue: Long = 0,
    val profit: Long = 0,
    val orderCount: Int = 0,
    val avgProfit: Long = 0,
    val avgMargin: Double? = null,
    val hasOrders: Boolean = false
)

class HomeViewModel(
    private val orderRepository: OrderRepository,
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _summary = MutableStateFlow(FinancialSummary())
    val summary: StateFlow<FinancialSummary> = _summary.asStateFlow()

    init {
        loadSummary()
    }

    fun loadSummary() {
        viewModelScope.launch(Dispatchers.IO) {
            val revenue = orderRepository.getTotalRevenue()
            val profit = orderRepository.getTotalProfit()
            val count = orderRepository.getCompletedOrderCount()

            val avgProfit = if (count > 0) profit / count else 0L
            val avgMargin = when {
                count > 0 && revenue > 0 && profit > 0 -> (profit.toDouble() / revenue) * 100.0
                else -> null
            }

            _summary.value = FinancialSummary(
                revenue = revenue,
                profit = profit,
                orderCount = count,
                avgProfit = avgProfit,
                avgMargin = avgMargin,
                hasOrders = count > 0
            )
        }
    }

    class Factory(
        private val orderRepository: OrderRepository,
        private val productRepository: ProductRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            HomeViewModel(orderRepository, productRepository) as T
    }
}
