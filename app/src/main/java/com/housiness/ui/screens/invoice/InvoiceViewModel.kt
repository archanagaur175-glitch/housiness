package com.housiness.ui.screens.invoice

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.housiness.data.db.entity.Invoice
import com.housiness.data.db.entity.InvoiceLineItem
import com.housiness.data.repository.InvoiceRepository
import com.housiness.data.repository.OrderRepository
import com.housiness.data.repository.SettingsRepository
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
    private val orderRepository: OrderRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(InvoiceUiState())
    val state: StateFlow<InvoiceUiState> = _state.asStateFlow()

    private var businessName: String = "Your Business Name"

    fun loadInvoice(invoiceId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val invoice = invoiceRepository.getById(invoiceId)
            val lineItems = invoiceRepository.getLineItems(invoiceId)
            val settings = settingsRepository.get()
            if (settings != null) businessName = settings.businessName
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
                    lineItems,
                    businessName
                )
                _state.value = _state.value.copy(pdfFile = file, isGenerating = false)
            } catch (e: Throwable) {
                _state.value = _state.value.copy(isGenerating = false, error = e.message ?: "PDF generation failed")
            }
        }
    }

    class Factory(
        private val invoiceRepository: InvoiceRepository,
        private val orderRepository: OrderRepository,
        private val settingsRepository: SettingsRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            InvoiceViewModel(invoiceRepository, orderRepository, settingsRepository) as T
    }
}
