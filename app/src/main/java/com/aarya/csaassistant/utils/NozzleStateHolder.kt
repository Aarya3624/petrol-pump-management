package com.aarya.csaassistant.utils

import android.os.PowerMonitorReadings
import androidx.compose.runtime.MutableState

data class NozzleStateHolder(
    val checkedStates: Map<String, MutableState<Boolean>>,
    val nozzleReadings: Map<String, MutableState<Pair<String, String>>>
) {
    fun getOpeningReadings(): List<Pair<String, String>> {
        return checkedStates.filterValues { it.value }.map {
            it.key to nozzleReadings[it.key]?.value?.first.orEmpty()
        }
    }

    fun getClosingReadings(): List<Pair<String, String>> {
        return checkedStates.filterValues { it.value }.map {
            it.key to nozzleReadings[it.key]?.value?.second.orEmpty()
        }
    }

    fun getAllReadings(): List<Triple<String, String, String>> {
        return checkedStates.filterValues { it.value }.map {
            val pair = nozzleReadings[it.key]?.value ?: ("" to "")
            Triple(it.key, pair.first, pair.second)
        }
    }
}
