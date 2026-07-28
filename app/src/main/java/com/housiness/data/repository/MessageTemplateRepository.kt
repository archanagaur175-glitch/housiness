package com.housiness.data.repository

import com.housiness.data.db.dao.MessageTemplateDao
import com.housiness.data.db.entity.MessageTemplate
import kotlinx.coroutines.flow.Flow

class MessageTemplateRepository(private val templateDao: MessageTemplateDao) {

    fun getAllFlow(): Flow<List<MessageTemplate>> = templateDao.getAllFlow()

    suspend fun getAll(): List<MessageTemplate> = templateDao.getAll()

    suspend fun getById(id: Long): MessageTemplate? = templateDao.getById(id)

    suspend fun insert(template: MessageTemplate): Long = templateDao.insert(template)

    suspend fun update(template: MessageTemplate) = templateDao.update(template)

    suspend fun delete(template: MessageTemplate) = templateDao.delete(template)
}
