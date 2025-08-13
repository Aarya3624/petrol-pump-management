package com.aarya.csaassistant.screens

import android.Manifest // Required for Manifest.permission.CAMERA
import android.content.Context
import android.content.pm.PackageManager // Required for PackageManager
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items // Ensure items is imported
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color // For temporary background
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat // Required for ContextCompat
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.aarya.csaassistant.BuildConfig
import com.aarya.csaassistant.viewmodel.ImageViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

// Helper function to create an image file (aligned with cache-path in file_paths.xml)
private fun createImageFileForCapture(context: Context): File {
    val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val imageFileName = "JPEG_${timeStamp}_"
    val imagePath = File(context.cacheDir, "images") // As per your file_paths.xml
    if (!imagePath.exists()) {
        imagePath.mkdirs()
    }
    return File.createTempFile(
        imageFileName, ".jpg", imagePath
    )
}

// Helper function to get a content URI for a file using FileProvider
private fun getUriForFileCapture(context: Context, file: File): Uri {
    return FileProvider.getUriForFile(
        context,
        "${BuildConfig.APPLICATION_ID}.provider", // Ensure BuildConfig is resolving
        file
    )
}

@Composable
fun ImageCaptureSection(
    modifier: Modifier = Modifier,
    imageViewModel: ImageViewModel = hiltViewModel(),
    onImageUrlsChanged: (List<String>) -> Unit,
    maxImages: Int = 5
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val uploadedImageUrls by imageViewModel.imageUrls.collectAsState()
    val uploadResult by imageViewModel.uploadResult.collectAsState()

    // Log when uploadedImageUrls state changes
    LaunchedEffect(uploadedImageUrls) {
        Log.d("ImageCaptureSection", "uploadedImageUrls changed: Count = ${uploadedImageUrls.size}, URLs = $uploadedImageUrls")
        onImageUrlsChanged(uploadedImageUrls)
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            imageViewModel.addCapturedImageUriFromCamera(success, context)
        }
    )

    // Permission Launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            try {
                val photoFile = createImageFileForCapture(context)
                val photoUri = getUriForFileCapture(context, photoFile)
                imageViewModel.setTemporaryCameraUri(photoUri)
                cameraLauncher.launch(photoUri)
            } catch (ex: Exception) {
                Log.e("ImageCaptureSection", "Error preparing camera after permission grant: ${ex.message}", ex)
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Error preparing camera: ${ex.message}", duration = SnackbarDuration.Long)
                }
            }
        } else {
            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    "Camera permission denied. Cannot take pictures.",
                    duration = SnackbarDuration.Long
                )
            }
        }
    }

    val launchCameraWithPermissionCheck = {
        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) -> {
                try {
                    val photoFile = createImageFileForCapture(context)
                    val photoUri = getUriForFileCapture(context, photoFile)
                    imageViewModel.setTemporaryCameraUri(photoUri)
                    cameraLauncher.launch(photoUri)
                } catch (ex: Exception) {
                    Log.e("ImageCaptureSection", "Error preparing camera (permission granted flow): ${ex.message}", ex)
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Error preparing camera: ${ex.message}", duration = SnackbarDuration.Long)
                    }
                }
            }
            else -> {
                permissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }


    LaunchedEffect(uploadResult) {
        uploadResult?.let { result ->
            val message = result.fold(
                onSuccess = {
                    if (it.startsWith("Image deleted")) it else "Image processed: ${it.substringAfterLast('/')}"
                },
                onFailure = { "Operation failed: ${it.message}" }
            )
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
            imageViewModel.clearUploadResult()
        }
    }

    Column(modifier = modifier) {
        Text(
            text = "Closing Images",
            modifier = Modifier.padding(bottom = 8.dp),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LazyRow(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(items = uploadedImageUrls, key = { it }) { imageUrl -> // Added key for stability
                    Log.d("ImageCaptureSection", "LazyRow: Displaying item for URL: $imageUrl")
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                            .background(Color.Magenta) // Temporary background
                            .clickable {
                                // Will add fullscreen logic here later
                                Log.d("ImageCaptureSection", "Image clicked: $imageUrl")
                            }
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(
                                model = imageUrl,
                                onError = { errorResult ->
                                    Log.e("ImageCaptureSection", "Coil error loading $imageUrl: ${errorResult.result.throwable.message}", errorResult.result.throwable)
                                },
                                onSuccess = {
                                    Log.d("ImageCaptureSection", "Coil success loading $imageUrl")
                                }
                            ),
                            contentDescription = "Uploaded Image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        IconButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.VirtualKey)
                                imageViewModel.removeUploadedImage(imageUrl)
                            },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(4.dp)
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Remove Image",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            } // End of LazyRow

            if (uploadedImageUrls.size < maxImages) {
                Spacer(Modifier.width(8.dp))
                OutlinedButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.VirtualKey)
                        launchCameraWithPermissionCheck()
                    },
                    modifier = Modifier.size(80.dp),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AddAPhoto,
                            contentDescription = "Add Image",
                            modifier = Modifier.size(32.dp)
                        )
                        Text("Add", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        } // End of parent Row

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}
