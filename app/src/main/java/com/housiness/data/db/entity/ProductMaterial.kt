package com.housiness.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class UnitType { KG, G, LITER, ML, UNIT }

@Entity(tableName = "product_materials")
data class ProductMaterial(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productId: Long,
    val materialId: Long?,
    val name: String,
    val unitType: UnitType,
    val unitCost: Long,
    val pricePaid: Long,
    val unitSize: Long,
    val quantityUsed: Long,
    val calculatedCost: Long
)
