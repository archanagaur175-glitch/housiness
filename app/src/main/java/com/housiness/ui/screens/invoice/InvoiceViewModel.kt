package com.housiness.ui.screens.invoice

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.housiness.data.db.entity.Invoice
import com.housiness.data.db.entity.InvoiceLineItem
import com.housiness.data.repository.InvoiceRepository
import com.housiness.data.repository.OrderRepository
import com.housiness.util.PdfGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

data class InvoiceUiState(
    val invoice: Invoice? = null,
    val lineItems: List<InvoiceLineItem> = emptyList(),
    val pdfFile: File? = null,
    val isGenerating: Boolean = false,
    val error: String? = null
)

class InvoiceViewModel(
    private val invoiceRepository: InvoiceRepository,
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val _state = MutableStateFlow(InvoiceUiState())
    val state: StateFlow<InvoiceUiState> = _state.asStateFlow()

    fun loadInvoice(invoiceId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val invoice = invoiceRepository.getById(invoiceId)
            val lineItems = invoiceRepository.getLineItems(invoiceId)
            _state.value = _state.value.copy(invoice = invoice, lineItems = lineItems)
        }
    }

    fun generatePdf(context: Context) {
        val invoice = _state.value.invoice ?: return
        val lineItems = _state.value.lineItems
        _state.value = _state.value.copy(isGenerating = true, error = null)
        viewModelScope.launch(Dispatchers.Main) {
            try {
                val file = PdfGenerator.generateInvoicePdf(
                    context,
                    "${invoice.orderRef}.pdf",
                    invoice,
                    lineItems
                )
                _state.value = _state.value.copy(pdfFile = file, isGenerating = false)
            } catch (e: Exception) {
                _state.value = _state.value.copy(isGenerating = false, error = e.message)
            }
        }
    }

    class Factory(
        private val invoiceRepository: InvoiceRepository,
        private val orderRepository: OrderRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            InvoiceViewModel(invoiceRepository, orderRepository) as T
    }
}
