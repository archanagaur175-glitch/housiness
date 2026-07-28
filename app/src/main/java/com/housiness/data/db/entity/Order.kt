package com.housiness.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class OrderStatus { UPCOMING, COMPLETED }

@Entity(tableName = "orders")
data class Order(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productId: Long,
    val productName: String,
    val quantity: Int,
    val totalPrice: Long,
    val totalCost: Long,
    val status: OrderStatus,
    val dateTime: Long,
    val invoiceId: Long? = null,
    val createdAt: Long
)
