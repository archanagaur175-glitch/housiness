package com.housiness.data.db.dao

import androidx.room.*
import com.housiness.data.db.entity.MessageTemplate
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageTemplateDao {
    @Query("SELECT * FROM message_templates ORDER BY lastEditedAt DESC")
    fun getAllFlow(): Flow<List<MessageTemplate>>

    @Query("SELECT * FROM message_templates ORDER BY lastEditedAt DESC")
    suspend fun getAll(): List<MessageTemplate>

    @Query("SELECT * FROM message_templates WHERE id = :id")
    suspend fun getById(id: Long): MessageTemplate?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(templates: List<MessageTemplate>)

    @Insert
    suspend fun insert(template: MessageTemplate): Long

    @Update
    suspend fun update(template: MessageTemplate)

    @Delete
    suspend fun delete(template: MessageTemplate)

    @Query("DELETE FROM message_templates")
    suspend fun deleteAll()
}
