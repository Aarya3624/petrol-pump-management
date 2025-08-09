package com.aarya.csaassistant.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aarya.csaassistant.model.AuthManager
import com.aarya.csaassistant.model.AuthResponse
import com.aarya.csaassistant.model.UserProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authManager: AuthManager
): ViewModel() {

    private val _session = MutableStateFlow<UserSession?>(null)
    val session: StateFlow<UserSession?> = _session.asStateFlow()

    private val _sessionState = MutableStateFlow<SessionStatus>(SessionStatus.Initializing)
    val sessionState: StateFlow<SessionStatus> = _sessionState.asStateFlow()

    private val _userData = MutableStateFlow<UserProfile?>(null)
    val userData: StateFlow<UserProfile?> = _userData.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _allUsers = MutableStateFlow<List<UserProfile>>(emptyList())
    val allUsers: StateFlow<List<UserProfile>> = _allUsers.asStateFlow()


    init {
        observeSession()
        fetchAllUsers()
    }

    private fun fetchAllUsers() {
        viewModelScope.launch {
            try {
                // You might want to set a loading state here if needed
                val users = authManager.getAllUserData() // This will call your new AuthManager function
                _allUsers.value = users
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error fetching all users: ${e.localizedMessage}")
                _allUsers.value = emptyList() // Default to empty list on error
            } finally {
                // Clear loading state if you set one
            }
        }
    }

    private fun observeSession() {
        viewModelScope.launch {
            authManager.observeSessionStatus().collectLatest { status ->
                _sessionState.value = status
                when (status) {
                    is SessionStatus.Authenticated -> {
                        val newSession = status.session
                        _session.value = newSession
                        fetchUserData(newSession)
                    }
                    is SessionStatus.NotAuthenticated,
                    is SessionStatus.RefreshFailure -> {
                        _session.value = null
                        _userData.value = null
                        _isLoading.value = false
                    }
                    is SessionStatus.Initializing -> {
                        _isLoading.value = true
                    }
                }
            }
        }
    }

    private fun fetchUserData(session: UserSession) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val user = authManager.getUserData(session)
                _userData.value = user
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error fetching user data: ${e.localizedMessage}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun signUp(email: String, password: String, name: String) {
        viewModelScope.launch {
            authManager.signUp(email, password, name).collect { response ->
                when (response) {
                    is AuthResponse.Success -> Log.d("AuthViewModel", "Sign up successful")
                    is AuthResponse.Error -> Log.e("AuthViewModel", "Sign up error: ${response.message}")
                    is AuthResponse.Loading -> _isLoading.value = true
                }
            }
        }
    }
    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            Log.d("AuthViewModel", "Signing in with email: $email")
            authManager.signIn(email, password).collect { response ->
                if (response is AuthResponse.Success) {
                    Log.d("AuthViewModel", "Sign-in successful")
                } else if (response is AuthResponse.Error) {
                    Log.e("AuthViewModel", "Sign-in error: ${response.message}")
                }
            }
        }
    }
    fun signOut(onResult: (AuthResponse) -> Unit) {
        viewModelScope.launch {
            authManager.signOut().collect { result ->
                onResult(result)
            }
        }
    }

    fun signInWithGoogle(googleIdToken: String, rawNonce: String) {
        viewModelScope.launch {
            authManager.signInWithGoogle(rawNonce, googleIdToken).collect { response ->
                if (response is AuthResponse.Success) {
                    Log.d("AuthViewModel", "Sign-in with Google successful")
                } else if (response is AuthResponse.Error) {
                    Log.e("AuthViewModel", "Sign-in with Google error: ${response.message}")
                }
            }
        }
    }
}