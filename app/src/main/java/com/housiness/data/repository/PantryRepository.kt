package com.housiness.data.repository

import com.housiness.data.db.dao.PantryDao
import com.housiness.data.db.entity.PantryItem
import kotlinx.coroutines.flow.Flow

class PantryRepository(private val pantryDao: PantryDao) {

    fun getAllFlow(): Flow<List<PantryItem>> = pantryDao.getAllFlow()

    suspend fun getAll(): List<PantryItem> = pantryDao.getAll()

    suspend fun getById(id: Long): PantryItem? = pantryDao.getById(id)

    suspend fun insert(item: PantryItem): Long = pantryDao.insert(item)

    suspend fun update(item: PantryItem) = pantryDao.update(item)

    suspend fun delete(item: PantryItem) = pantryDao.delete(item)
}
