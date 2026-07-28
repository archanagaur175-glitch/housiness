package com.housiness.data.db.dao

import androidx.room.*
import com.housiness.data.db.entity.Invoice
import kotlinx.coroutines.flow.Flow

@Dao
interface InvoiceDao {
    @Query("SELECT * FROM invoices ORDER BY generatedAt DESC")
    fun getAllFlow(): Flow<List<Invoice>>

    @Query("SELECT * FROM invoices ORDER BY generatedAt DESC")
    suspend fun getAll(): List<Invoice>

    @Query("SELECT * FROM invoices WHERE id = :id")
    suspend fun getById(id: Long): Invoice?

    @Query("SELECT * FROM invoices WHERE orderId = :orderId")
    suspend fun getByOrderId(orderId: Long): Invoice?

    @Query("SELECT order_ref FROM invoices ORDER BY id DESC LIMIT 1")
    suspend fun getLastOrderRef(): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(invoices: List<Invoice>)

    @Insert
    suspend fun insert(invoice: Invoice): Long

    @Delete
    suspend fun delete(invoice: Invoice)

    @Query("DELETE FROM invoices")
    suspend fun deleteAll()
}
