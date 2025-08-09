package com.aarya.csaassistant.utils

import java.time.LocalDate
import java.time.format.DateTimeFormatter

fun formatDateHeader(date: LocalDate): String {
    val today = LocalDate.now()
    val yesterday = today.minusDays(1)

    return when (date) {
        today -> "Today"
        yesterday -> "Yesterday"
        else -> date.format(DateTimeFormatter.ofPattern("MMM d"))
    }
}