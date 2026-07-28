package com.housiness.data.repository

import com.housiness.data.db.dao.InvoiceDao
import com.housiness.data.db.dao.InvoiceLineItemDao
import com.housiness.data.db.entity.Invoice
import com.housiness.data.db.entity.InvoiceLineItem
import kotlinx.coroutines.flow.Flow

class InvoiceRepository(
    private val invoiceDao: InvoiceDao,
    private val invoiceLineItemDao: InvoiceLineItemDao
) {

    fun getAllFlow(): Flow<List<Invoice>> = invoiceDao.getAllFlow()

    suspend fun getAll(): List<Invoice> = invoiceDao.getAll()

    suspend fun getById(id: Long): Invoice? = invoiceDao.getById(id)

    suspend fun getByOrderId(orderId: Long): Invoice? = invoiceDao.getByOrderId(orderId)

    suspend fun getNextInvoiceNumber(): String {
        val lastRef = invoiceDao.getLastOrderRef()
        val maxNum = lastRef?.removePrefix("INV-")?.toLongOrNull() ?: 0L
        return "INV-${String.format("%03d", maxNum + 1)}"
    }

    suspend fun insert(invoice: Invoice): Long = invoiceDao.insert(invoice)

    suspend fun getLineItems(invoiceId: Long): List<InvoiceLineItem> =
        invoiceLineItemDao.getByInvoiceId(invoiceId)

    suspend fun saveLineItems(items: List<InvoiceLineItem>) {
        if (items.isNotEmpty()) {
            invoiceLineItemDao.deleteByInvoiceId(items.first().invoiceId)
            invoiceLineItemDao.upsertAll(items)
        }
    }
}
