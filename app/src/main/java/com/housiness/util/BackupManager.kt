package com.housiness.util

import com.google.gson.GsonBuilder
import com.housiness.data.db.dao.*
import com.housiness.data.db.entity.*
import java.text.SimpleDateFormat
import java.util.*

enum class ImportMode { MERGE, REPLACE }
data class BackupData(
    val version: Int,
    val exportedAt: Long,
    val products: List<Product>,
    val productMaterials: List<ProductMaterial>,
    val pantryItems: List<PantryItem>,
    val orders: List<Order>,
    val invoices: List<Invoice>,
    val invoiceLineItems: List<InvoiceLineItem>,
    val messageTemplates: List<MessageTemplate>,
    val settings: List<AppSettings> = emptyList()
)

object BackupManager {

    private const val CURRENT_VERSION = 2
    private val gson = GsonBuilder().setPrettyPrinting().create()

    fun generateFilename(): String {
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        return "housiness-backup-$date.json"
    }

    suspend fun export(
        productDao: ProductDao,
        productMaterialDao: ProductMaterialDao,
        pantryDao: PantryDao,
        orderDao: OrderDao,
        invoiceDao: InvoiceDao,
        invoiceLineItemDao: InvoiceLineItemDao,
        messageTemplateDao: MessageTemplateDao,
        settingsDao: SettingsDao
    ): String {
        val currentSettings = settingsDao.get()
        val data = BackupData(
            version = CURRENT_VERSION,
            exportedAt = System.currentTimeMillis(),
            products = productDao.getAll(),
            productMaterials = productMaterialDao.getAll(),
            pantryItems = pantryDao.getAll(),
            orders = orderDao.getAll(),
            invoices = invoiceDao.getAll(),
            invoiceLineItems = invoiceLineItemDao.getAll(),
            messageTemplates = messageTemplateDao.getAll(),
            settings = if (currentSettings != null) listOf(currentSettings) else emptyList()
        )
        return gson.toJson(data)
    }

    fun validate(json: String): ValidationResult {
        return try {
            val data = gson.fromJson(json, BackupData::class.java)
            if (data == null) return ValidationResult.Invalid("Could not read backup file")
            if (data.products == null || data.pantryItems == null ||
                data.orders == null || data.invoices == null || data.messageTemplates == null
            ) {
                return ValidationResult.Invalid("This doesn't look like a Housiness backup file")
            }
            val warning = when {
                data.version > CURRENT_VERSION -> "This backup is from a newer version — some data may not be fully compatible"
                data.version < CURRENT_VERSION -> "This backup is from an older version — some new fields may be missing"
                else -> null
            }
            ValidationResult.Valid(data, warning)
        } catch (e: Exception) {
            ValidationResult.Invalid("This doesn't look like a Housiness backup file")
        }
    }

    suspend fun import(
        data: BackupData,
        mode: ImportMode,
        productDao: ProductDao,
        productMaterialDao: ProductMaterialDao,
        pantryDao: PantryDao,
        orderDao: OrderDao,
        invoiceDao: InvoiceDao,
        invoiceLineItemDao: InvoiceLineItemDao,
        messageTemplateDao: MessageTemplateDao,
        settingsDao: SettingsDao
    ) {
        if (mode == ImportMode.REPLACE) {
            productDao.deleteAll()
            productMaterialDao.deleteAll()
            pantryDao.deleteAll()
            orderDao.deleteAll()
            invoiceDao.deleteAll()
            invoiceLineItemDao.deleteAll()
            messageTemplateDao.deleteAll()
        }

        productDao.upsertAll(data.products)
        productMaterialDao.upsertAll(data.productMaterials)
        pantryDao.upsertAll(data.pantryItems)
        orderDao.upsertAll(data.orders)
        invoiceDao.upsertAll(data.invoices)
        invoiceLineItemDao.upsertAll(data.invoiceLineItems)
        messageTemplateDao.upsertAll(data.messageTemplates)

        data.settings.firstOrNull()?.let { settingsDao.save(it) }
    }

    sealed class ValidationResult {
        data class Valid(val data: BackupData, val warning: String?) : ValidationResult()
        data class Invalid(val message: String) : ValidationResult()
    }
}
