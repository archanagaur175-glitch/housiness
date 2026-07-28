package com.housiness.data.db.dao

import androidx.room.*
import com.housiness.data.db.entity.InvoiceLineItem

@Dao
interface InvoiceLineItemDao {
    @Query("SELECT * FROM invoice_line_items")
    suspend fun getAll(): List<InvoiceLineItem>

    @Query("SELECT * FROM invoice_line_items WHERE invoiceId = :invoiceId")
    suspend fun getByInvoiceId(invoiceId: Long): List<InvoiceLineItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<InvoiceLineItem>)

    @Insert
    suspend fun insert(item: InvoiceLineItem): Long

    @Query("DELETE FROM invoice_line_items WHERE invoiceId = :invoiceId")
    suspend fun deleteByInvoiceId(invoiceId: Long)

    @Query("DELETE FROM invoice_line_items")
    suspend fun deleteAll()
}
