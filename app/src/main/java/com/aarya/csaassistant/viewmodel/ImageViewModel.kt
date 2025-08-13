package com.aarya.csaassistant.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aarya.csaassistant.model.ImageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ImageViewModel @Inject constructor(
    private val imageRepository: ImageRepository
) : ViewModel() {

    private val _imageUrls = MutableStateFlow<List<String>>(emptyList())
    val imageUrls: StateFlow<List<String>> = _imageUrls.asStateFlow()

    private val _capturedImageUris = MutableStateFlow<List<Uri>>(emptyList())
    val capturedImageUris: StateFlow<List<Uri>> = _capturedImageUris.asStateFlow()

    private var tempCameraImageUri: Uri? = null

    private val _isUploading = MutableStateFlow(false)
    val isUploading: StateFlow<Boolean> = _isUploading.asStateFlow()

    private val _uploadResult = MutableStateFlow<Result<String>?>(null)
    val uploadResult: StateFlow<Result<String>?> = _uploadResult.asStateFlow()

    val maxImages = 5

    fun setTemporaryCameraUri(uri: Uri) {
        tempCameraImageUri = uri
    }

    fun getTemporaryCameraUri(): Uri? {
        return tempCameraImageUri
    }

    // Updated to take context and trigger immediate upload
    fun addCapturedImageUriFromCamera(success: Boolean, context: Context) {
        val uri = tempCameraImageUri
        if (success && uri != null) {
            if ((_capturedImageUris.value.size + _imageUrls.value.size) < maxImages) {
                // Add to captured URIs first, then attempt upload.
                // If upload fails, it remains in _capturedImageUris for potential retry.
                _capturedImageUris.value = _capturedImageUris.value + uri
                uploadSpecificCapturedUri(context, uri)
            } else {
                Log.w("ImageViewModel", "Maximum number of images ($maxImages) reached. Cannot add more from camera.")
                _uploadResult.value = Result.failure(Exception("Maximum number of images ($maxImages) reached."))
            }
        } else {
            Log.e("ImageViewModel", "Camera capture failed or temporary URI was null.")
            // Optionally set a failure result if needed:
            // _uploadResult.value = Result.failure(Exception("Camera capture failed."))
        }
        tempCameraImageUri = null // Reset after processing
    }

    // New function to upload a specific URI immediately
    private fun uploadSpecificCapturedUri(context: Context, uri: Uri) {
        viewModelScope.launch {
            _isUploading.value = true
            try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val bytes = inputStream.readBytes()
                    val fileName = "${UUID.randomUUID()}.jpg"
                    val url = imageRepository.uploadImage(bytes, fileName)
                    if (url != null) {
                        _imageUrls.value = _imageUrls.value + url
                        _capturedImageUris.value = _capturedImageUris.value - uri // Remove from pending
                        _uploadResult.value = Result.success(url)
                        Log.d("ImageViewModel", "URL added to _imageUrls: $url")
                    } else {
                        _uploadResult.value = Result.failure(Exception("Upload failed for $uri"))
                        Log.e("ImageViewModel", "Upload failed for URI: $uri. imageRepository.uploadImage returned null.")
                    }
                } ?: run {
                    Log.e("ImageViewModel", "Could not open input stream for URI: $uri. It will remain in pending list.")
                    _uploadResult.value = Result.failure(Exception("Failed to read image $uri"))
                }
            } catch (e: Exception) {
                Log.e("ImageViewModel", "Error uploading image from URI: $uri. It will remain in pending list.", e)
                _uploadResult.value = Result.failure(e)
            } finally {
                _isUploading.value = false
            }
        }
    }

    fun removeCapturedImageUri(uri: Uri) {
        _capturedImageUris.value = _capturedImageUris.value - uri
    }

    fun clearCapturedUris() {
        _capturedImageUris.value = emptyList()
        tempCameraImageUri = null
    }

    // Batch upload for any remaining captured URIs (e.g., if a retry mechanism is added)
    fun uploadCapturedImages(context: Context) {
        if (_capturedImageUris.value.isEmpty()) {
            _uploadResult.value = Result.failure(Exception("No images to upload."))
            return
        }
        if (_imageUrls.value.size >= maxImages) {
            _uploadResult.value = Result.failure(Exception("Cannot upload, max $maxImages images already uploaded."))
            return
        }

        val urisToProcess = _capturedImageUris.value.toList() // Snapshot for iteration

        viewModelScope.launch {
            _isUploading.value = true
            var overallSuccess = true
            val successfullyUploadedThisBatch = mutableListOf<Uri>()

            for (uri in urisToProcess) {
                if (_imageUrls.value.size >= maxImages) {
                    Log.w("ImageViewModel", "Max images reached during batch upload. Skipping remaining.")
                    _uploadResult.value = Result.failure(Exception("Max images reached. Some images were not uploaded."))
                    overallSuccess = false
                    break
                }
                try {
                    context.contentResolver.openInputStream(uri)?.use { inputStream ->
                        val bytes = inputStream.readBytes()
                        val fileName = "${UUID.randomUUID()}.jpg"
                        val url = imageRepository.uploadImage(bytes, fileName)
                        if (url != null) {
                            _imageUrls.value = _imageUrls.value + url
                            successfullyUploadedThisBatch.add(uri)
                            _uploadResult.value = Result.success(url) // Individual success
                        } else {
                            _uploadResult.value = Result.failure(Exception("Upload failed for $uri"))
                            Log.e("ImageViewModel", "Upload failed for URI: $uri.")
                            overallSuccess = false
                        }
                    } ?: run {
                        Log.e("ImageViewModel", "Could not open input stream for URI: $uri.")
                        _uploadResult.value = Result.failure(Exception("Failed to read image $uri"))
                        overallSuccess = false
                    }
                } catch (e: Exception) {
                    Log.e("ImageViewModel", "Error uploading image from URI: $uri.", e)
                    _uploadResult.value = Result.failure(e)
                    overallSuccess = false
                }
            }

            if (successfullyUploadedThisBatch.isNotEmpty()) {
                _capturedImageUris.value = _capturedImageUris.value - successfullyUploadedThisBatch.toSet()
            }

            if (!overallSuccess && _uploadResult.value?.isSuccess != false) {
                 if (_imageUrls.value.size < maxImages) {
                    _uploadResult.value = Result.failure(Exception("One or more images failed to upload."))
                }
            } else if (overallSuccess && urisToProcess.isNotEmpty() && successfullyUploadedThisBatch.size == urisToProcess.size) {
                // All in this batch succeeded
                 // _uploadResult.value = Result.success("All pending images uploaded successfully.") // Optional: too chatty?
            }
            _isUploading.value = false
        }
    }

    fun uploadImageBytes(bytes: ByteArray) {
        if (_imageUrls.value.size >= maxImages) {
            _uploadResult.value = Result.failure(Exception("Max $maxImages images allowed"))
            return
        }
        viewModelScope.launch {
            _isUploading.value = true
            try {
                val fileName = "${UUID.randomUUID()}.jpg"
                val url = imageRepository.uploadImage(bytes, fileName)
                if (url != null) {
                    _imageUrls.value = _imageUrls.value + url
                    _uploadResult.value = Result.success(url)
                    Log.d("ImageViewModel", "URL added to _imageUrls from bytes: $url")
                } else {
                    _uploadResult.value = Result.failure(Exception("Upload failed"))
                    Log.e("ImageViewModel", "Upload from bytes failed. imageRepository.uploadImage returned null.")
                }
            } catch (e: Exception) {
                _uploadResult.value = Result.failure(e)
            } finally {
                _isUploading.value = false
            }
        }
    }

    // Updated to provide feedback via _uploadResult
    fun removeUploadedImage(url: String) {
        viewModelScope.launch {
            _isUploading.value = true // Indicate activity
            val path = url.substringAfter("closing-readings/") // Ensure this path extraction is robust
            if (path == url || path.isBlank()) {
                Log.e("ImageViewModel", "Failed to extract valid path from URL: $url")
                _uploadResult.value = Result.failure(Exception("Failed to process image URL for deletion."))
                _isUploading.value = false
                return@launch
            }
            try {
                val success = imageRepository.deleteImage(path)
                if (success) {
                    _imageUrls.value = _imageUrls.value - url
                    _uploadResult.value = Result.success("Image deleted successfully") // Indicate success type
                } else {
                    Log.e("ImageViewModel", "Failed to delete image from repository: $url")
                    _uploadResult.value = Result.failure(Exception("Failed to delete image from storage."))
                }
            } catch (e: Exception) {
                Log.e("ImageViewModel", "Error deleting image: $url", e)
                _uploadResult.value = Result.failure(e)
            } finally {
                _isUploading.value = false
            }
        }
    }

    fun clearAllImageData() {
        _imageUrls.value = emptyList()
        _capturedImageUris.value = emptyList()
        tempCameraImageUri = null
        _uploadResult.value = null
    }

    // New function to clear the upload result, to be called by UI after displaying a message
    fun clearUploadResult() {
        _uploadResult.value = null
    }
}
