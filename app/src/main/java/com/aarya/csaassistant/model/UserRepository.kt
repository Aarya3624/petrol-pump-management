package com.aarya.csaassistant.model

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.user.UserSession
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val supabase: SupabaseClient
) {
    suspend fun getUserData(session: UserSession): UserProfile? = withContext(Dispatchers.IO) {
        try {
            val userId = session.user?.id as Any
            supabase.from("profiles")
                .select(columns = Columns.ALL) {
                    filter {
                        eq("id", userId)
                    }
                }
                .decodeSingle<UserProfile>()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getAllUsers(): List<UserProfile> = withContext(Dispatchers.IO) {
        try {
            supabase.from("profiles")
                .select(columns = Columns.ALL)
                .decodeList<UserProfile>()
        } catch (e: Exception) {
            // Log error or return empty list, depending on desired error handling
            println("Error fetching all users: ${e.message}")
            emptyList()
        }
    }

    suspend fun updateUserData(userProfile: UserProfile): Boolean = withContext(Dispatchers.IO) {
        try {
            supabase.from("profiles").update(userProfile)
            true
        } catch (e: Exception) {
            false
        }
    }
}