package com.aarya.csaassistant.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aarya.csaassistant.model.AuthManager
import com.aarya.csaassistant.model.Employee
import com.aarya.csaassistant.model.EmployeeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class EmployeeViewModel @Inject constructor(
    private val employeeRepository: EmployeeRepository,
    private val authManager: AuthManager
) : ViewModel() {

    private val _employees = MutableStateFlow<List<Employee>>(emptyList())
    val employees: StateFlow<List<Employee>> = _employees.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _currentUserRole = MutableStateFlow<String?>(null)
    val currentUserRole: StateFlow<String?> = _currentUserRole.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _selectedEmployeeDetails = MutableStateFlow<Employee?>(null)
    val selectedEmployeeDetails: StateFlow<Employee?> = _selectedEmployeeDetails.asStateFlow()

    private var fetchingEmployeeDetailsForId: String? = null
    private var currentSession: UserSession? = null

    init {
        observeSessionStatus()
    }

    private fun observeSessionStatus() {
        viewModelScope.launch {
            authManager.observeSessionStatus().collectLatest { sessionStatus ->
                _isLoading.value = true // Set loading true at the start of processing a new status
                _errorMessage.value = null // Clear previous errors

                when (sessionStatus) {
                    is SessionStatus.Authenticated -> {
                        currentSession = sessionStatus.session
                        val role = fetchCurrentUserRoleInternal()
                        fetchEmployeesInternal()
                    }
                    is SessionStatus.NotAuthenticated,
                    is SessionStatus.RefreshFailure -> {
                        currentSession = null
                        _employees.value = emptyList()
                        _currentUserRole.value = null
                        // Potentially set an error message for RefreshFailure
                        if (sessionStatus is SessionStatus.RefreshFailure) {
                            _errorMessage.value = "Session refresh failed: ${sessionStatus}"
                        }
                    }
                    is SessionStatus.Initializing -> {
                        // Handled by _isLoading.value = true at the start of collectLatest
                    }
                }
                _isLoading.value = false // Set loading false after all processing for this status
            }
        }
    }

    private suspend fun fetchCurrentUserRoleInternal(): String? {
        return try {
            val role = currentSession?.user?.id?.let { id ->
                // Assuming employeeRepository.getCurrentUserRole is a suspend function
                // or calls withContext(Dispatchers.IO) internally
                employeeRepository.getCurrentUserRole(id)
            }
            _currentUserRole.value = role
            role
        } catch (e: Exception) {
            _errorMessage.value = "Error fetching user role: ${e.message}"
            _currentUserRole.value = null
            null
        }
    }

    fun fetchEmployeeDetailsById(employeeId: String) {
        if (employeeId.isBlank()) {
            Log.e("EmployeeViewModel", "Invalid employeeId")
            return
        }

        if (fetchingEmployeeDetailsForId == employeeId && _selectedEmployeeDetails.value?.id == employeeId) {
            Log.d("EmployeeViewModel", "Employee details already being fetched for $employeeId")
            return
        }
        fetchingEmployeeDetailsForId = employeeId
        _isLoading.value = true
        _selectedEmployeeDetails.value = null

        viewModelScope.launch {
            try {
                Log.d("EntryViewModel", "Fetching employee details for $employeeId")

                val detailedEmployee = employeeRepository.getEmployeeDetailsById(employeeId)
                _selectedEmployeeDetails.value = detailedEmployee
                if (detailedEmployee == null) {
                    Log.w("EmployeeViewModel", "Employee details not found for $employeeId")
                } else {
                    Log.d("EmployeeViewModel", "Fetched employee details: $detailedEmployee")
                }
            } catch (e: Exception) {
                Log.e("EmployeeViewModel", "Error fetching employee details for $employeeId: ${e.message}", e)
                _errorMessage.value = "Error fetching employee details: ${e.message}"
            } finally {
                _isLoading.value = false
                if (_selectedEmployeeDetails.value?.id != employeeId) {
                    fetchingEmployeeDetailsForId = null
                }
            }
        }
    }

    private suspend fun fetchEmployeesInternal() {
        try {
            // Role check is now done before calling this function
            _employees.value = employeeRepository.getAllUsers()
            Log.d("EmployeeViewModel", "Fetched employees: ${_employees.value}")
        } catch (e: Exception) {
            _errorMessage.value = "Error fetching employees: ${e.message}"
            _employees.value = emptyList()
        }
    }

    fun changeUserRole(userId: String, newRole: String) {
        viewModelScope.launch { // Keep on main thread for isLoading and subsequent UI updates
            _isLoading.value = true
            _errorMessage.value = null
            try {
                withContext(Dispatchers.IO) { // Perform network call on IO dispatcher
                    employeeRepository.updateUserRole(userId, newRole)
                }
                // After role update, refresh employee list if current user is admin
                if (_currentUserRole.value == "Admin") {
                    fetchEmployeesInternal()
                }
                // Optionally, if the role of the current user itself might have changed
                // and they are no longer admin, you might need to re-evaluate:
                // fetchCurrentUserRoleInternal() // to update current user's role
                // then check again if admin and fetchEmployeesInternal()
                // For now, assumes current user's admin status is not affected by changing another user's role.

            } catch (e: Exception) {
                _errorMessage.value = "Error updating role: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
