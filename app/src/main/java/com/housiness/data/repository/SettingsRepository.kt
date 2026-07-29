package com.housiness.data.repository

import com.housiness.data.db.dao.SettingsDao
import com.housiness.data.db.entity.AppSettings
import kotlinx.coroutines.flow.Flow

class SettingsRepository(private val dao: SettingsDao) {

    fun observe(): Flow<AppSettings?> = dao.observe()

    suspend fun get(): AppSettings? = dao.get()

    suspend fun save(settings: AppSettings) = dao.save(settings)
}
