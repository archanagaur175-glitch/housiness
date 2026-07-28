package com.housiness.data.db.dao

import androidx.room.*
import com.housiness.data.db.entity.PantryItem
import kotlinx.coroutines.flow.Flow

@Dao
interface PantryDao {
    @Query("SELECT * FROM pantry_items ORDER BY lastUsed DESC")
    fun getAllFlow(): Flow<List<PantryItem>>

    @Query("SELECT * FROM pantry_items ORDER BY lastUsed DESC")
    suspend fun getAll(): List<PantryItem>

    @Query("SELECT * FROM pantry_items WHERE id = :id")
    suspend fun getById(id: Long): PantryItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<PantryItem>)

    @Insert
    suspend fun insert(item: PantryItem): Long

    @Update
    suspend fun update(item: PantryItem)

    @Delete
    suspend fun delete(item: PantryItem)

    @Query("DELETE FROM pantry_items")
    suspend fun deleteAll()
}
