package com.housiness.data.db.dao

import androidx.room.*
import com.housiness.data.db.entity.Order
import com.housiness.data.db.entity.OrderStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDao {
    @Query("SELECT * FROM orders ORDER BY dateTime DESC")
    fun getAllFlow(): Flow<List<Order>>

    @Query("SELECT * FROM orders ORDER BY createdAt DESC")
    suspend fun getAll(): List<Order>

    @Query("SELECT * FROM orders WHERE id = :id")
    suspend fun getById(id: Long): Order?

    @Query("SELECT * FROM orders WHERE status = :status ORDER BY dateTime ASC")
    fun getByStatusFlow(status: OrderStatus): Flow<List<Order>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(orders: List<Order>)

    @Insert
    suspend fun insert(order: Order): Long

    @Update
    suspend fun update(order: Order)

    @Delete
    suspend fun delete(order: Order)

    @Query("DELETE FROM orders")
    suspend fun deleteAll()

    @Query("SELECT COALESCE(SUM(totalPrice), 0) FROM orders WHERE status = 'COMPLETED'")
    suspend fun getTotalRevenue(): Long

    @Query("SELECT COALESCE(SUM(totalPrice - totalCost), 0) FROM orders WHERE status = 'COMPLETED'")
    suspend fun getTotalProfit(): Long

    @Query("SELECT COUNT(*) FROM orders WHERE status = 'COMPLETED'")
    suspend fun getCompletedOrderCount(): Int
}
