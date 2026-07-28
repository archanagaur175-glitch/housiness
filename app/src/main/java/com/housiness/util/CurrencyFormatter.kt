package com.housiness.util

import java.text.NumberFormat
import java.util.Locale

object CurrencyFormatter {

    private val format by lazy {
        NumberFormat.getCurrencyInstance(Locale.getDefault()).apply {
            maximumFractionDigits = 0
        }
    }

    fun format(paisa: Long): String {
        return format.format(paisa / 100.0)
    }
}
