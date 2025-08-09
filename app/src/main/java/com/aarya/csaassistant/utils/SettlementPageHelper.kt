package com.aarya.csaassistant.utils

import android.util.Log
import androidx.compose.ui.graphics.Color

enum class ProductType { MS, HSD, PWR }

data class ProductInfo(
    val type: ProductType,
    val displayName: String,
    val pricePerUnit: Float,
    val colorMatcher: Color
)

val productDetails = listOf(
    ProductInfo(ProductType.MS, "MS", 98.50f, Color(0x8A4EDA53)), // Match your Card color alpha
    ProductInfo(ProductType.HSD, "HSD", 92.30f, Color(0x8360B5DC)),
    ProductInfo(ProductType.PWR, "PWR", 102.00f, Color(0x80F44336)) // Assuming red for Power
)

val nozzleProductColors = mapOf(
    "A1" to Color(0xFF4CAF50), // Green for MS
    "B1" to Color(0xFF4CAF50), // Green for MS
    "A2" to Color(0xFF2196F3), // Blue for HSD
    "B2" to Color(0xFF2196F3), // Blue for HSD
    "C1" to Color(0xFFF44336), // Red for Power (example)
    "C2" to Color(0xFF2196F3)  // Assuming C2 is HSD in your example
)

fun getProductInfoByNozzleColor(nozzleColor: Color): ProductInfo? {
    // Compare only RGB, ignore alpha for matching productDetails colorMatcher
    val targetRgb = nozzleColor.copy(alpha = 1f)
    return productDetails.find { it.colorMatcher.copy(alpha = 1f) == targetRgb }
}

fun getProductInfoByNozzleName(nozzleId: String): ProductInfo? {
    return when (nozzleId.uppercase()) { // Use uppercase for case-insensitive matching
        "A1", "B1" -> productDetails.find { it.type == ProductType.MS }
        "A2", "B2", "C2" -> productDetails.find { it.type == ProductType.HSD }
        "C1" -> productDetails.find { it.type == ProductType.PWR }
        else -> {
            Log.w("SettlementPage", "Unknown nozzle ID: $nozzleId. Cannot determine product.")
            null
        }
    }
}

data class ProductSalesSummary(
    val productInfo: ProductInfo,
    var totalQuantity: Float = 0f,
    var totalAmount: Float = 0f
)