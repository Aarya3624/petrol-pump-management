package com.aarya.csaassistant.model

import android.util.Log
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class EntryRepository @Inject constructor(
    private val supabase: SupabaseClient
) {
    suspend fun getEntries(): List<Entry> = withContext(Dispatchers.IO) {
        try {
            val result = supabase.from("Entries")
                .select()
                .decodeList<Entry>()

            Log.d("EntryRepository", "Entries fetched: $result")
            return@withContext result
        } catch (e: Exception) {
            Log.e("EntryRepository", "Error fetching entries", e)
            return@withContext emptyList()
        }
    }

    suspend fun addEntry(entry: Entry): Boolean = withContext(Dispatchers.IO) {
        try {
            val success = supabase
                .from("Entries")
                .insert(entry) {
                    select()
                }
                .decodeSingle<Entry>()

            Log.d("EntryRepository", "Entry Added: ${success}")
            true
        } catch (e: Exception) {
            Log.e("EntryRepository", "Error adding entry:", e)
            false
        }
    }

    suspend fun getDetailedEntryById(entryId: String): Entry? = withContext(Dispatchers.IO) {
        try {
            Log.d("EntryRepository", "Attempting to fetch detailed entry for ID: $entryId")

            // Determine if the 'id' column in your Supabase table is an Integer or String/UUID
            // and use the correct type for the query.
            // Let's assume for this example your `Entry` data class has an `id` field
            // that matches the type of the column in Supabase.

            // If your 'id' column in Supabase is an Integer:
            val idAsInt: Int? = try { entryId.toInt() } catch (nfe: NumberFormatException) { null }

            val result = if (idAsInt != null) {
                // Query assuming 'id' is an INTEGER in Supabase
                supabase.from("Entries").select() {
                    filter { eq("id", idAsInt) }
                }.decodeSingleOrNull<Entry>()
            } else {
                // Query assuming 'id' is TEXT/UUID in Supabase
                supabase.from("Entries")
                    .select() // Selects all columns
                    {
                        filter {
                            eq("id", entryId) // Use the string directly
                            // If Entry is a data class with an 'id' property:
                            // Entry::id eq entryId // This is more type-safe if applicable
                        }
                    }
                    .decodeSingleOrNull()
            }

            if (result == null) {
                Log.w("EntryRepository", "No entry found for ID: $entryId")
            } else {
                Log.d("EntryRepository", "Detailed entry fetched for ID $entryId: $result")
            }
            return@withContext result
        } catch (e: Exception) {
            Log.e("EntryRepository", "Error fetching detailed entry for ID $entryId", e)
            return@withContext null
        }
    }

    suspend fun deleteEntry(id: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            supabase.from("Entries")
                .delete {
                    filter { eq("id", id) }
                }
            Log.d("EntryRepository", "Expense deleted: $id")
            true
        } catch (e: Exception) {
            Log.e("EntryRepository", "Error deleting expenses", e)
            false
        }
    }
}