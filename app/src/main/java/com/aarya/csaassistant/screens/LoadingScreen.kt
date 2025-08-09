package com.aarya.csaassistant.screens

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocalGasStation
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aarya.csaassistant.viewmodel.AuthViewModel
import io.github.jan.supabase.auth.status.SessionStatus

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LoadingScreen(
    authViewModel: AuthViewModel = hiltViewModel(),
    onLoaded: () -> Unit,
    onNotAuthenticated: () -> Unit
) {

    val sessionState by authViewModel.sessionState.collectAsState()

    LaunchedEffect(sessionState) {
        if (sessionState is SessionStatus.Authenticated) {
            onLoaded()
            Log.d("LoadingScreen", "SessionStatus.Authenticated")
        } else if (sessionState is SessionStatus.NotAuthenticated) {
            onNotAuthenticated()
            Log.d("LoadingScreen", "SessionStatus.NotAuthenticated")
        }
    }

    Surface {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularWavyProgressIndicator(
                modifier = Modifier.size(112.dp),
                amplitude = 1.5f,
                wavelength = 24.dp,
                waveSpeed = 112.dp
            )

            Icon(
                Icons.Rounded.LocalGasStation,
                null,
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(54.dp)
            )

        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoadingScreenPreview() {
//    LoadingScreen()
}
