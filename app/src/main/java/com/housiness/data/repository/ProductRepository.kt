package com.housiness.data.repository

import com.housiness.data.db.dao.*
import com.housiness.data.db.entity.*
import kotlinx.coroutines.flow.Flow

class ProductRepository(private val productDao: ProductDao, private val productMaterialDao: ProductMaterialDao) {

    fun getAllFlow(): Flow<List<Product>> = productDao.getAllFlow()

    suspend fun getAll(): List<Product> = productDao.getAll()

    suspend fun getById(id: Long): Product? = productDao.getById(id)

    suspend fun insert(product: Product): Long = productDao.insert(product)

    suspend fun update(product: Product) = productDao.update(product)

    suspend fun delete(product: Product) = productDao.delete(product)

    suspend fun getMaterialsByProductId(productId: Long): List<ProductMaterial> =
        productMaterialDao.getByProductId(productId)

    suspend fun saveMaterials(materials: List<ProductMaterial>) {
        productMaterialDao.deleteByProductId(materials.first().productId)
        productMaterialDao.upsertAll(materials)
    }
}
