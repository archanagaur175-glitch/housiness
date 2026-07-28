package com.housiness.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class CostSource { MANUAL, CALCULATED }

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val sellingPrice: Long,
    val makingCost: Long,
    val costSource: CostSource,
    val createdAt: Long
)
