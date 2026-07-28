package com.housiness.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.housiness.data.db.dao.*
import com.housiness.data.db.entity.*

@Database(
    entities = [
        Product::class, ProductMaterial::class, PantryItem::class,
        Order::class, Invoice::class, InvoiceLineItem::class, MessageTemplate::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun productMaterialDao(): ProductMaterialDao
    abstract fun pantryDao(): PantryDao
    abstract fun orderDao(): OrderDao
    abstract fun invoiceDao(): InvoiceDao
    abstract fun invoiceLineItemDao(): InvoiceLineItemDao
    abstract fun messageTemplateDao(): MessageTemplateDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "housiness.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
