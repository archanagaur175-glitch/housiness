package com.housiness.data.repository

import com.housiness.data.db.dao.OrderDao
import com.housiness.data.db.entity.Order
import com.housiness.data.db.entity.OrderStatus
import kotlinx.coroutines.flow.Flow

class OrderRepository(private val orderDao: OrderDao) {

    fun getAllFlow(): Flow<List<Order>> = orderDao.getAllFlow()

    suspend fun getAll(): List<Order> = orderDao.getAll()

    suspend fun getById(id: Long): Order? = orderDao.getById(id)

    fun getByStatusFlow(status: OrderStatus): Flow<List<Order>> = orderDao.getByStatusFlow(status)

    suspend fun insert(order: Order): Long = orderDao.insert(order)

    suspend fun update(order: Order) = orderDao.update(order)

    suspend fun delete(order: Order) = orderDao.delete(order)

    suspend fun getTotalRevenue(): Long = orderDao.getTotalRevenue()

    suspend fun getTotalProfit(): Long = orderDao.getTotalProfit()

    suspend fun getCompletedOrderCount(): Int = orderDao.getCompletedOrderCount()
}
