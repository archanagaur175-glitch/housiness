package com.housiness.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "invoices")
data class Invoice(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val orderId: Long,
    val orderRef: String,
    val customerName: String? = null,
    val subtotal: Long,
    val generatedAt: Long
)
