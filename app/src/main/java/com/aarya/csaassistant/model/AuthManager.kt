package com.aarya.csaassistant.model

import android.util.Log
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.builtin.IDToken
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import javax.inject.Inject

sealed class AuthResponse {
    object Success: AuthResponse()
    data class Error(val message: String) : AuthResponse()
    object Loading: AuthResponse()
}

class AuthManager @Inject constructor(
    private val supabase: SupabaseClient,
    private val userRepository: UserRepository
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    fun observeSessionStatus(): Flow<SessionStatus> {
        return supabase.auth.sessionStatus
    }

    fun signUp(email: String, password: String, name: String): Flow<AuthResponse> = flow {
        try {
            supabase.auth.signUpWith(Email) {
                this.email = email
                this.password = password
                data = buildJsonObject {
                    put("full_name", JsonPrimitive(name))
                }
            }
            emit(AuthResponse.Success)
        } catch (e: Exception) {
            emit(AuthResponse.Error(e.localizedMessage))
        }
    }

    fun signIn(email: String, password: String): Flow<AuthResponse> = flow {
        try {
            val response = supabase.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            Log.d("AuthManager", "signIn() Success: ${response != null}")
            emit(AuthResponse.Success)
        } catch (e: Exception) {
            emit(AuthResponse.Error(e.localizedMessage))
            Log.e("AuthManager", "Error signing in: ${e.localizedMessage}")
        }
    }

    fun signOut(): Flow<AuthResponse> = flow {
        try {
            supabase.auth.signOut()
            emit(AuthResponse.Success)
        } catch (e: Exception) {
            emit(AuthResponse.Error(e.localizedMessage))
        }
    }

    suspend fun getUserData(session: UserSession): UserProfile? {
        return userRepository.getUserData(session)
    }

    suspend fun getAllUserData(): List<UserProfile> {
        return userRepository.getAllUsers()
    }

    fun signInWithGoogle(rawNonce: String, googleIdToken: String): Flow<AuthResponse> = flow {
        try {
            val response = supabase.auth.signInWith(IDToken) {
                nonce = rawNonce
                idToken = googleIdToken
                provider = Google
            }
            Log.d("AuthManager", "signIn() success: ${response != null}")
            emit(AuthResponse.Success)
        } catch (e: Exception) {
            emit(AuthResponse.Error(e.localizedMessage))
            Log.e("AuthManagerNew", "Error signing in: ${e.localizedMessage}")
        }
    }
}