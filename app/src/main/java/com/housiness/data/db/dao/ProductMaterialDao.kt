package com.housiness.data.db.dao

import androidx.room.*
import com.housiness.data.db.entity.ProductMaterial
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductMaterialDao {
    @Query("SELECT * FROM product_materials")
    suspend fun getAll(): List<ProductMaterial>

    @Query("SELECT * FROM product_materials WHERE productId = :productId")
    suspend fun getByProductId(productId: Long): List<ProductMaterial>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(materials: List<ProductMaterial>)

    @Insert
    suspend fun insert(material: ProductMaterial): Long

    @Query("DELETE FROM product_materials WHERE productId = :productId")
    suspend fun deleteByProductId(productId: Long)

    @Query("DELETE FROM product_materials")
    suspend fun deleteAll()
}
