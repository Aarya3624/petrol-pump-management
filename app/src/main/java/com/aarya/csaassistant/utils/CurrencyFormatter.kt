package com.aarya.csaassistant.utils

import java.text.NumberFormat
import java.util.Locale

fun formatCurrency(amount: Float?, currencyCode: String = "INR"): String {
    if (amount == null) return "N/A" // Or "₹0.00" or "" depending on preference
    return try {
        val format = NumberFormat.getCurrencyInstance(Locale("en", "IN")) // For Indian Rupee format
        // For other locales/currencies, adjust Locale("en", "IN")
        // To use default system locale for currency: NumberFormat.getCurrencyInstance()
        format.currency = java.util.Currency.getInstance(currencyCode)
        format.format(amount)
    } catch (e: Exception) {
        // Fallback for invalid currency code or other issues
        "₹${String.format("%.2f", amount)}"
    }
}
