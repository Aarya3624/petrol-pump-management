package com.aarya.csaassistant.model

import android.util.Log
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth // Correct import for Supabase auth
// import io.github.jan.supabase.storage.FileUploadResponse // Not strictly needed if not using the 'response' variable
// import io.github.jan.supabase.storage.UploadOptionBuilder // Not strictly needed for default options
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ImageRepository @Inject constructor(
    private val supabase: SupabaseClient
) {
    suspend fun uploadImage(bytes: ByteArray, fileName: String): String? = withContext(Dispatchers.IO) {
        try {
            Log.d("ImageRepositoryAuth", "Attempting upload. Current user ID: ${supabase.auth.currentUserOrNull()?.id}, Session status: ${supabase.auth.sessionStatus.value}")

            val bucket = supabase.storage.from("closing-readings")
            // This is the correct full path within the bucket, including the "uploads" folder
            val uploadPath = "uploads/$fileName" 

            // Perform the upload using the defined uploadPath
            bucket.upload(path = uploadPath, data = bytes, options = {
                // upsert = false is the default, but can be set explicitly if needed
                this.upsert = false 
            })

            // Generate the public URL using the 'uploadPath' we defined, NOT response.path
            val publicUrl = bucket.publicUrl(uploadPath) 
            Log.d("ImageRepository", "Image uploaded successfully. Public URL: $publicUrl (using defined uploadPath: $uploadPath)")
            return@withContext publicUrl
        } catch (e: Exception) {
            Log.e("ImageRepository", "Error uploading image: ${e.message}", e)
            return@withContext null
        }
    }

    suspend fun deleteImage(filePath: String): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.d("ImageRepositoryAuth", "Attempting delete. Current user ID: ${supabase.auth.currentUserOrNull()?.id}, Session status: ${supabase.auth.sessionStatus.value}")
            val bucket = supabase.storage.from("closing-readings")
            // Ensure filePath provided to this function is the full path, e.g., "uploads/image.jpg"
            bucket.delete(listOf(filePath))
            Log.d("ImageRepository", "Image deletion attempted for: $filePath")
            return@withContext true
        } catch (e: Exception) {
            Log.e("ImageRepository", "Error deleting image: ${e.message}", e)
            return@withContext false
        }
    }
}
