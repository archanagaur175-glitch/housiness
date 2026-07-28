package com.housiness.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "invoice_line_items")
data class InvoiceLineItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val invoiceId: Long,
    val productName: String,
    val quantity: Int,
    val unitPrice: Long,
    val total: Long
)
