package com.aarya.csaassistant.utils

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun getItemShape(index: Int, totalItemCount: Int, defaultCornerSize: Dp = 8.dp, endCornerSize: Dp = 20.dp): Shape {
    return when (index) {
        0 -> { // First item
            RoundedCornerShape(
                topStart = endCornerSize,
                topEnd = endCornerSize,
                bottomStart = defaultCornerSize,
                bottomEnd = defaultCornerSize
            )
        }
        totalItemCount - 1 -> { // Last item
            RoundedCornerShape(
                topStart = defaultCornerSize,
                topEnd = defaultCornerSize,
                bottomStart = endCornerSize,
                bottomEnd = endCornerSize
            )
        }
        else -> { // Middle items
            androidx.compose.foundation.shape.RoundedCornerShape(defaultCornerSize)
        }
    }
}

@Composable
fun getListBoundaryShape(
    index: Int,
    totalItemCount: Int,
    topCornerSize: Dp = 20.dp, // For the very top of the list
    bottomCornerSize: Dp = 20.dp, // For the very bottom of the list
    defaultCornerSize: Dp = 0.dp // For all other corners
): Shape {
    if (totalItemCount == 0) return RoundedCornerShape(defaultCornerSize) // Should not happen with items
    if (totalItemCount == 1) return RoundedCornerShape(topCornerSize) // Single item, fully rounded

    return when (index) {
        0 -> RoundedCornerShape(topStart = topCornerSize, topEnd = topCornerSize, bottomStart = defaultCornerSize, bottomEnd = defaultCornerSize)
        totalItemCount - 1 -> RoundedCornerShape(topStart = defaultCornerSize, topEnd = defaultCornerSize, bottomStart = bottomCornerSize, bottomEnd = bottomCornerSize)
        else -> RoundedCornerShape(defaultCornerSize)
    }
}
