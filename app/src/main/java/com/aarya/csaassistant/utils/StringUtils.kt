package com.aarya.csaassistant.utils

import android.icu.text.DecimalFormat
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow

fun formatNumberToShortCompactForm(number: Float?): String {
    if (number == null) return "N/A"
    if (number == 0f) return "0"

    if (number < 0) {
        return "-${formatNumberToShortCompactForm(-number)}"
    }

    if (number < 1000) {
        return if (number == floor(number)) {
            DecimalFormat("#,###").format(number)
        } else {
            DecimalFormat("#,###0.#").format(number)
        }
    }

    val magnitude = floor(log10(number.toDouble()) / 3).toInt()
    val scaledNumber = number / 10.0.pow(magnitude * 3)

    val suffix = when (magnitude) {
        1 -> "k"
        2 -> "M"
        3 -> "B"
        4 -> "T"
        else -> ""
    }

    val formatter = when {
        scaledNumber < 10 && scaledNumber != floor(scaledNumber.toDouble()) -> DecimalFormat("#,##0.0")
        else -> DecimalFormat("#,###")
    }
    return "${formatter.format(scaledNumber)}$suffix"
}