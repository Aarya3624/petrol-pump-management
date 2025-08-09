package com.aarya.csaassistant.screens


import android.content.ContentValues.TAG
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.hilt.navigation.compose.hiltViewModel
import com.aarya.csaassistant.R
import com.aarya.csaassistant.viewmodel.AuthViewModel
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.launch
import java.security.MessageDigest
import java.util.UUID

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SignUpScreen(
    onSignUpSuccess: () -> Unit,
    onLoginClick: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    val isLoading by authViewModel.isLoading.collectAsState()
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val sessionState by authViewModel.sessionState.collectAsState()

    LaunchedEffect(sessionState) {
        if (sessionState is SessionStatus.Authenticated) {
            onSignUpSuccess()
        }
    }

    Scaffold() { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Sign Up",
                style = MaterialTheme.typography.displaySmallEmphasized,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(24.dp))

            OutlinedTextField(
                value = name,
                modifier = Modifier
                    .padding(horizontal = 48.dp)
                    .fillMaxWidth(),
                onValueChange = { name = it },
                label = { Text("Name") },
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
            )

            OutlinedTextField(
                value = email,
                modifier = Modifier
                    .padding(horizontal = 48.dp)
                    .fillMaxWidth(),
                onValueChange = { email = it },
                label = { Text("Email") },
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )
            OutlinedTextField(
                value = password,
                modifier = Modifier
                    .padding(horizontal = 48.dp)
                    .fillMaxWidth(),
                onValueChange = { password = it },
                label = { Text("Password") },
                shape = RoundedCornerShape(16.dp),
                trailingIcon = {
                    IconButton(
                        onClick = {}
                    ) {
                        Icon(
                            Icons.Outlined.Visibility,
                            null
                        )
                    }
                }
            )

            Button(
                onClick = {
                    authViewModel.signUp(
                        email = email,
                        password = password,
                        name = name
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 48.dp, vertical = 8.dp)
                    .height(54.dp),
                shapes = ButtonDefaults.shapes(),
            ) {
                Text("Sign Up")
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 32.dp), // Adjust padding as needed
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                HorizontalDivider(
                    modifier = Modifier
                        .weight(1f) // Takes up available space
                        .padding(end = 8.dp), // Space before the text
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant // Or your preferred color
                )
                Text(
                    text = "or",
                    style = MaterialTheme.typography.labelSmall, // Or your preferred style
                    color = MaterialTheme.colorScheme.onSurfaceVariant // Or your preferred color
                )
                HorizontalDivider(
                    modifier = Modifier
                        .weight(1f) // Takes up available space
                        .padding(start = 8.dp), // Space after the text
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant // Or your preferred color
                )
            }

            Spacer(Modifier.height(8.dp))

            GoogleSignInButton(authViewModel)

        }
    }
}


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun GoogleSignInButton(authViewModel: AuthViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val onClick: () -> Unit = {
        val credentialManager = CredentialManager.create(context)

        val rawNonce = UUID.randomUUID().toString()
        val bytes = rawNonce.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        val hashedNonce = digest.fold("") {str, it -> str + "%02x".format(it)}

        val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId("829383209362-6spb5o7mef7o7h53mfmhdieeb005s7eu.apps.googleusercontent.com")
            .setNonce(hashedNonce)
            .build()

        val request: GetCredentialRequest = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        coroutineScope.launch {
            try {
                val result = credentialManager.getCredential(
                    request = request,
                    context = context
                )
                val credential = result.credential

                val googleIdTokenCredential = GoogleIdTokenCredential
                    .createFrom(credential.data)

                val googleIdToken = googleIdTokenCredential.idToken

                Log.i(TAG, "Google ID Token: $googleIdToken")

                authViewModel.signInWithGoogle(googleIdToken, rawNonce)

                Toast.makeText(context, "You are Signed In!", Toast.LENGTH_SHORT).show()
            } catch (e: GetCredentialException) {
                Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
            } catch (e: GoogleIdTokenParsingException) {
                Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .height(54.dp)
            .padding(horizontal = 48.dp),
        shapes = ButtonDefaults.shapes()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(R.drawable.ic_google),
                contentDescription = "Google Icon",
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text("Sign In With Google")
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SignUpScreenPreview() {
    SignUpScreen(onSignUpSuccess = {}, onLoginClick = {})
}