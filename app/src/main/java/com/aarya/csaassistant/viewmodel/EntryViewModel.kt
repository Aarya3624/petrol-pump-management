package com.aarya.csaassistant.viewmodel

import android.util.Log
import androidx.compose.ui.text.style.BaselineShift
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aarya.csaassistant.model.AuthManager
import com.aarya.csaassistant.model.Entry
import com.aarya.csaassistant.model.EntryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@HiltViewModel
class EntryViewModel @Inject constructor(
    private val entryRepository: EntryRepository,
    private val authManager: AuthManager
): ViewModel() {

    private val _entries = MutableStateFlow<List<Entry>>(emptyList())
    val entries: StateFlow<List<Entry>> = _entries.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _addEntryResult = MutableStateFlow<Result<Unit>?>(null) // Use Result for better error handling
    val addEntryResult: StateFlow<Result<Unit>?> = _addEntryResult.asStateFlow()

    @OptIn(ExperimentalTime::class)
    private val _newEntryState = MutableStateFlow(Entry())
    val newEntryState: StateFlow<Entry> = _newEntryState.asStateFlow()

    // Stateflow to hold the specific entry being viewed on the details screen
    private val _selectedEntryDetails = MutableStateFlow<Entry?>(null)
    val selectedEntryDetails: StateFlow<Entry?> = _selectedEntryDetails.asStateFlow()

    private var fetchingDetailsForId: String? = null

    private var currentSession: UserSession? = null

    init {
        observeSessionStatus()
    }

    private fun fetchEntries() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _entries.value = entryRepository.getEntries()
                Log.d("EntryViewModel", "Entries fetched")
            } catch (e: Exception) {
                Log.e("EntryViewModel", "Error fetching expenses", e)
                _entries.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun observeSessionStatus() {
        viewModelScope.launch {
            authManager.observeSessionStatus().collectLatest { sessionStatus ->
                when (sessionStatus) {
                    is SessionStatus.Authenticated -> {
                        currentSession = sessionStatus.session
                        fetchEntries()
                    }
                    is SessionStatus.NotAuthenticated,
                    is SessionStatus.RefreshFailure -> {
                        currentSession = null
                    _entries.value = emptyList()
                    }
                    is SessionStatus.Initializing -> {
                        _isLoading.value = true
                    }
                }
            }
        }
    }


    fun fetchEntryDetailsById(entryId: String) {
        if (fetchingDetailsForId == entryId && _selectedEntryDetails.value?.id.toString() == entryId) {
            Log.d("EntryViewModel", "Details for $entryId already loaded")
            return
        }
        fetchingDetailsForId = entryId
        _isLoading.value = true
        _selectedEntryDetails.value = null

        viewModelScope.launch {
            try {
                Log.d("EntryViewModel", "Fetching details for entry ID: $entryId")
                // ASSUMPTION: entryRepository has a method to get a fully detailed entry.
                // If getEntries() already returns full details, you could find it there,
                // but a dedicated repository call is usually better for performance and clarity.
                val detailedEntry = entryRepository.getDetailedEntryById(entryId) // Replace with your actual repo method
                _selectedEntryDetails.value = detailedEntry
                if (detailedEntry == null) {
                    Log.w("EntryViewModel", "No details found for entry ID: $entryId")
                } else {
                    Log.d("EntryViewModel", "Details fetched successfully for $entryId")
                }
            } catch (e: Exception) {
                Log.e("EntryViewModel", "Error fetching entry details for $entryId", e)
                _selectedEntryDetails.value = null // Ensure it's null on error
            } finally {
                _isLoading.value = false
                if (_selectedEntryDetails.value?.id.toString() != entryId) { // If fetch failed or different ID came back
                    fetchingDetailsForId = null
                }
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    fun updateShiftChangeData(
        openingReadings: Map<String, Float>?,
        closingReadings: Map<String, Float>?,
        currentShift: String?,
        handedOverBy: String,
        handedOverTo: String?,
        closingImagesUrl: List<String>?
    ) {
        _newEntryState.value = _newEntryState.value.copy(
            opening_readings = openingReadings,
            closing_readings = closingReadings,
            shift = currentShift,
            handed_over_by = handedOverBy,
            handed_over_to = handedOverTo,
            created_at = null,
            closing_images_url = closingImagesUrl
        )
        Log.d("EntryViewModel", "Updated ShiftChangeData: ${_newEntryState.value}")
    }

    @OptIn(ExperimentalTime::class)
    fun updateSettlementData(
        totalSales: Float?,
        salesDetails: Map<String, Float>?,
        settlementDetails: Map<String, Float>?,
        balanceShort: Float?,
        creditDetails: Map<String, Float>?,
        paytmCardNo: String?,
    ) {
        _newEntryState.value = _newEntryState.value.copy(
            total_sales = totalSales,
            sales_details = salesDetails,
            settlement_details = settlementDetails,
            balance_short = balanceShort,
            credit_details = creditDetails,
            paytm_card_no = paytmCardNo,
        )
        Log.d("EntryViewModel", "Updated SettlementData: ${_newEntryState.value}")
    }

    @OptIn(ExperimentalTime::class)
    fun submitNewEntry() {
        viewModelScope.launch {
            val entryToSubmit = _newEntryState.value

            Log.d("EntryViewModel", "Attempting to submit entry: $entryToSubmit")
            if (!isValid(entryToSubmit)) {
                Log.w("EntryViewModel", "Invalid entry data. Not submitting: $entryToSubmit")
                _addEntryResult.value = Result.failure(IllegalStateException("Validation failed. Please check all fields"))
                return@launch
            }
            try {
                _isLoading.value = true
                entryRepository.addEntry(entryToSubmit)
                _addEntryResult.value = Result.success(Unit)
                Log.d("EntryViewModel", "Entry added successfully")
                fetchEntries()
                _newEntryState.value = Entry()
            } catch (e: Exception) {
                Log.e("EntryViewModel", "Error adding entry", e)
                _addEntryResult.value = Result.failure(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun isValid(entry: Entry): Boolean {
        // Implement your validation logic here.
        // Example: Ensure required fields are not null or empty.
        // Check non-nullable fields that don't have safe defaults (like handed_over_by if it can't be empty)
        if (entry.handed_over_by!!.isBlank()) {
            Log.w("Validation", "Handed over by is blank")
            return false
        }
        if (entry.total_sales == null || entry.total_sales < 0) { // Example
            Log.w("Validation", "Total sales is invalid: ${entry.total_sales}")
            return false
        }
        // Add more checks for all critical fields from both screens
        return true
    }

//    fun addEntry(entry: Entry) {
//        viewModelScope.launch {
//            Log.d("EntryViewModel", "Adding expense: $entry")
//            val success = entryRepository.addEntry(entry)
//            _addEntryResult.value = success
//            if (success) {
//                Log.d("EntryViewModel", "Entry added successfully")
//                fetchEntries()
//            }
//        }
//    }

    fun deleteEntry(entry: Entry) { // Keep existing delete functionality
        viewModelScope.launch {
            entry.id?.let {
                try {
                    _isLoading.value = true
                    entryRepository.deleteEntry(it)
                    fetchEntries() // Refresh entries after deletion
                } catch (e: Exception) {
                    Log.e("EntryViewModel", "Error deleting entry", e)
                    // Optionally expose this error to the UI
                } finally {
                    _isLoading.value = false
                }
            } ?: run {
                Log.e("EntryViewModel", "Cannot delete entry with null id")
            }
        }
    }

    fun resetAddEntryResult() {
        _addEntryResult.value = null
    }

}