package com.aarya.csaassistant.model

import android.util.Log
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.realtime.Column
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class EmployeeRepository @Inject constructor(
    private val supabase: SupabaseClient
) {
    suspend fun getCurrentUserRole(userId: String): String? = withContext(Dispatchers.IO) {
        try {
            supabase.from("Users")
                .select(columns = Columns.list("role")) {
                    filter {
                        eq("id", userId)
                    }
                }
                .decodeSingleOrNull<Map<String, String>>()?.get("role")
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getAllUsers(): List<Employee> = withContext(Dispatchers.IO) {
        try {
            supabase.from("Users")
                .select(columns = Columns.list("id, full_name, avatar_url, role"))
                .decodeList<Employee>()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getEmployeeDetailsById(employeeId: String?): Employee? = withContext(Dispatchers.IO) {
        if (employeeId.isNullOrBlank()) {
            Log.e("EmployeeRepository", "employeeId is null or blank")
            return@withContext null
        }
        try {
            val result = supabase.from("Users")
                .select {
                    filter { eq("id::uuid", employeeId) }
                }
                .decodeSingleOrNull<Employee>()
            return@withContext result
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }

        val raw = supabase.from("Users")
            .select() // explicitly select all fields
            { filter { eq("id", employeeId) } }
            .decodeList<Map<String, Any?>>()

        Log.d("DEBUG", "Raw Supabase payload: $raw")
        return@withContext null
    }

    suspend fun updateUserRole(userId: String, newRole: String) = withContext(Dispatchers.IO) {
        try {
            supabase.from("Users")
                .update(
                    {
                        set("role", newRole)
                    }
                ) {
                    filter { eq("id", userId) }
                }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}