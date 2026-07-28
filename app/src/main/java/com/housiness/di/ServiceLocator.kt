package com.housiness.di

import android.content.Context
import com.housiness.data.db.AppDatabase
import com.housiness.data.db.dao.*
import com.housiness.data.repository.*

class ServiceLocator(context: Context) {

    private val db = AppDatabase.getInstance(context)

    val productDao: ProductDao get() = db.productDao()
    val productMaterialDao: ProductMaterialDao get() = db.productMaterialDao()
    val pantryDao: PantryDao get() = db.pantryDao()
    val orderDao: OrderDao get() = db.orderDao()
    val invoiceDao: InvoiceDao get() = db.invoiceDao()
    val invoiceLineItemDao: InvoiceLineItemDao get() = db.invoiceLineItemDao()
    val messageTemplateDao: MessageTemplateDao get() = db.messageTemplateDao()

    val productRepository: ProductRepository by lazy {
        ProductRepository(db.productDao(), db.productMaterialDao())
    }

    val pantryRepository: PantryRepository by lazy {
        PantryRepository(db.pantryDao())
    }

    val orderRepository: OrderRepository by lazy {
        OrderRepository(db.orderDao())
    }

    val invoiceRepository: InvoiceRepository by lazy {
        InvoiceRepository(db.invoiceDao(), db.invoiceLineItemDao())
    }

    val messageTemplateRepository: MessageTemplateRepository by lazy {
        MessageTemplateRepository(db.messageTemplateDao())
    }
}
