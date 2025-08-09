package com.aarya.csaassistant.utils

import kotlin.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.time.ExperimentalTime

fun formatDateHeaderNew(date: LocalDate): String { // Takes java.time.LocalDate
    val today = LocalDate.now()
    val yesterday = today.minusDays(1)

    return when (date) {
        today -> "Today"
        yesterday -> "Yesterday"
        else -> date.format(DateTimeFormatter.ofPattern("EEEE, MMMM d"))
    }
}

// Extension function to convert kotlin.time.Instant to java.time.LocalDate
@OptIn(ExperimentalTime::class)
fun kotlin.time.Instant.toJavaLocalDate(zone: ZoneId = ZoneId.systemDefault()): LocalDate {
    return java.time.Instant.ofEpochSecond(this.epochSeconds, this.nanosecondsOfSecond.toLong())
        .atZone(zone)
        .toLocalDate()
}

// Extension function to convert kotlin.time.Instant to java.time.LocalTime
@OptIn(ExperimentalTime::class)
fun kotlin.time.Instant.toJavaLocalTime(zone: ZoneId = ZoneId.systemDefault()): java.time.LocalTime {
    return java.time.Instant.ofEpochSecond(this.epochSeconds, this.nanosecondsOfSecond.toLong())
        .atZone(zone)
        .toLocalTime()
}
