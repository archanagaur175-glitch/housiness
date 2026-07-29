package com.housiness.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.housiness.data.db.dao.*
import com.housiness.data.db.entity.*

@Database(
    entities = [
        Product::class, ProductMaterial::class, PantryItem::class,
        Order::class, Invoice::class, InvoiceLineItem::class,
        MessageTemplate::class, AppSettings::class
    ],
    version = 2,
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
    abstract fun settingsDao(): SettingsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = Migration(1, 2) { db ->
            db.execSQL("CREATE TABLE IF NOT EXISTS `app_settings` (`id` INTEGER NOT NULL, `businessName` TEXT NOT NULL DEFAULT 'Your Business Name', PRIMARY KEY(`id`))")
            db.execSQL("INSERT OR IGNORE INTO app_settings (id, businessName) VALUES (1, 'Your Business Name')")
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "housiness.db"
                ).addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
