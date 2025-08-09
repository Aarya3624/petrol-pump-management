package com.aarya.csaassistant.screens

import android.util.Log
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Logout
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.aarya.csaassistant.R
import com.aarya.csaassistant.model.AuthResponse
import com.aarya.csaassistant.model.UserProfile
import com.aarya.csaassistant.utils.CookieShape
import com.aarya.csaassistant.viewmodel.AuthViewModel
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant


@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class,
    ExperimentalTime::class
)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel(),
    onSignOut: () -> Unit
) {
    val userData by authViewModel.userData.collectAsState()
//    val userData = UserProfile(
//        id = "1",
//        full_name = "Aarya Rashinkar",
//        updated_at = Clock.System.now()
//    )
    var showLogoutDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Settings",
                        style = MaterialTheme.typography.displaySmallEmphasized,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            onBackClick()
                        },
                        shapes = IconButtonDefaults.shapes(),
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer
                        ),
                        modifier = Modifier.width(34.dp)
                    ) {
                        Icon(
                            Icons.Rounded.ArrowBack,
                            null
                        )
                    }
                },
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        },
        bottomBar = {
            Button(
                onClick = {
                    showLogoutDialog = true
                },
                modifier = Modifier
                    .padding(vertical = 24.dp, horizontal = 16.dp)
                    .height(54.dp)
                    .fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ),
                shapes = ButtonDefaults.shapes(

                )
            ) {
                Icon(
                    Icons.Rounded.Logout,
                    null
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Log Out",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    ) { innerPadding ->
        if (showLogoutDialog) {
            AlertDialog(
                onDismissRequest = {
                    showLogoutDialog = false
                },
                title = {
                    Text("Confirm Logout")
                },
                confirmButton = {
                    Button (
                        onClick = {
                            authViewModel.signOut { response ->
                                when (response) {
                                    is AuthResponse.Success -> {
                                        onSignOut()
                                        showLogoutDialog = false
                                    }
                                    is AuthResponse.Error -> {
                                        showLogoutDialog = false
                                        Log.e("Signout", "Logout failed: ${response.message}")
                                    }

                                    AuthResponse.Loading -> TODO()
                                }
                            }

                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    ) {
                        Text("Logout")
                    }
                },
                text = {
                    Text("Are you sure you want to logout?")
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showLogoutDialog = false
                        }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                val avatarUrl = userData?.avatar_url
                val infiniteTransition = rememberInfiniteTransition()
                val rotation by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 360f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 50000, easing = LinearEasing)
                    )
                )

                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .graphicsLayer {
                            rotationZ = rotation // Rotate the whole box
                        }
                        .clip(MaterialShapes.Cookie7Sided.toShape())
                        .background(MaterialTheme.colorScheme.primary)
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(avatarUrl)
                            .crossfade(true)
                            .placeholder(R.drawable.ic_launcher_foreground)
                            .build(),
                        contentDescription = "Avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                rotationZ = -rotation // Counter-rotate the image inside
                            }
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "${userData?.full_name}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                ListItem(
                    headlineContent = {
                        Text("About CSA Assistant")
                    },
                    leadingContent = {
                        Icon(
                            Icons.Outlined.Info,
                            null
                        )
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SettingsScreenPreview() {
    SettingsScreen(onBackClick = {}, onSignOut = {})
}