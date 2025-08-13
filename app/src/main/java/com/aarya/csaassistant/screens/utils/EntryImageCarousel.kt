package com.aarya.csaassistant.screens.utils

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import kotlin.ranges.coerceIn

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntryImageCarousel(
    imageUrls: List<String>,
    onImageClick: (imageUrl: String) -> Unit
) {
    if (imageUrls.isEmpty()) {
        return
    }

    Column {
        Text(
            "Attached Images",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)
        )
        HorizontalMultiBrowseCarousel(
            state = rememberCarouselState { imageUrls.count() },
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(bottom = 8.dp),
            preferredItemWidth = 164.dp,
            itemSpacing = 4.dp,
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) { i ->
            val imageUrl = imageUrls[i]
            Image(
                modifier = Modifier
                    .height(225.dp)
                    .clickable { onImageClick(imageUrl) }
                    .maskClip(MaterialTheme.shapes.extraLarge),
                painter = rememberAsyncImagePainter(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageUrl)
                        .crossfade(true)
                        .build(),
                    onError = { error ->
                        Log.e("EntryImageCarousel", "Coil error loading image: ${error.result.throwable}")
                    },
                    onSuccess = {
                        Log.d("EntryImageCarousel", "Coil success loading image: $imageUrl")
                    }
                ),
                contentDescription = "Entry image ${i + 1}",
                contentScale = ContentScale.Crop,
            )
        }
    }
}

@Composable
fun FullScreenImageViewer(
    imageUrl: String,
    onDismissRequest: () -> Unit
) {
    var scale by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    Dialog(
        onDismissRequest = {
            // Reset state when dialog is dismissed
            scale = 1f
            offsetX = 0f
            offsetY = 0f
            onDismissRequest()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnClickOutside = true)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.85f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {
                        // Reset state on dismiss as well
                        scale = 1f
                        offsetX = 0f
                        offsetY = 0f
                        onDismissRequest()
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize() // Allow gesture detection over the whole dialog area
                    .pointerInput(Unit) {
                        detectTransformGestures { centroid, pan, zoom, _ ->
                            // Previous scale update:
                            // scale = (scale * zoom).coerceIn(1f, 5f)
                            // Previous pan update:
                            // offsetX += pan.x * scale
                            // offsetY += pan.y * scale

                            // Let's introduce damping factors
                            val zoomDamping = 0.9f // Reduce zoom sensitivity
                            val panDamping = 0.8f   // Reduce pan sensitivity

                            val newCalculatedScale = scale * (1 + (zoom - 1) * zoomDamping)
                            scale = newCalculatedScale.coerceIn(1f, 5f)

                            if (scale > 1f) {
                                // Apply pan damping. Note: pan is a delta, so we apply damping directly to it.
                                offsetX += pan.x * panDamping * scale // Damped pan, then scaled
                                offsetY += pan.y * panDamping * scale // Damped pan, then scaled

                                // Bounds checking remains the same
                                val maxOffsetX = (size.width * (scale - 1)) / 2
                                val maxOffsetY = (size.height * (scale - 1)) / 2
                                offsetX = offsetX.coerceIn(-maxOffsetX, maxOffsetX)
                                offsetY = offsetY.coerceIn(-maxOffsetY, maxOffsetY)
                            } else {
                                offsetX = 0f
                                offsetY = 0f
                            }
                        }
                    }
            ) {
                Image(
                    painter = rememberAsyncImagePainter(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(imageUrl)
                            .crossfade(true)
                            .build()
                    ),
                    contentDescription = "Fullscreen Image",
                    modifier = Modifier
                        .fillMaxSize() // Image should fill the gesture area initially
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offsetX,
                            translationY = offsetY
                        ),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}