package com.example.atmosphere.view.auth

import android.util.Log
import com.example.atmosphere.viewmodels.auth.AuthViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.isSystemInDarkTheme
import com.example.atmosphere.ui.theme.SecondFontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp


@Composable
fun CreateAccountScreen(viewModel: AuthViewModel, navController: NavController, sessionId: String) {
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary
    val isDarkTheme = isSystemInDarkTheme()
    Log.d("ThemeInfo", "onPrimary: $onPrimaryColor, isDarkTheme: $isDarkTheme")

    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.clearStatusMessage()
    }

    val statusMessage by viewModel.statusMessage.collectAsState()
    var isProcessing by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Card centered in the screen
        Box(
            modifier = Modifier
                .weight(1f) // Centers the card vertically
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            // Card for form elements
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f) // Set the card width to 90% of the screen
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(8.dp), // Add elevation for shadow
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface) // Card background color
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp), // Inner padding for the card content
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // "Register" Text
                    Text(
                        text = "Register",
                        fontSize = 50.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = SecondFontFamily,
                        color = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.padding(bottom = 32.dp)
                    )

                    // Email TextField
                    TextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        singleLine = true
                    )

                    // Password TextField
                    TextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        singleLine = true
                    )

                    // Create Account Button
                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            if (email.isNotBlank() && password.isNotBlank() && !isProcessing) {
                                isProcessing = true
                                viewModel.createAccount(email, password)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = email.isNotBlank() && password.isNotBlank() && !isProcessing
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Create Account")
                        }
                    }

                    // Status Message
                    statusMessage?.let { (message, isSuccess) ->
                        if (message?.isNotBlank() == true) {
                            Text(
                                text = message,
                                color = if (isSuccess) Color.Green else Color.Red,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }

                    // Navigation to Sign In Screen
                    TextButton(onClick = { navController.navigate("signIn/$sessionId") }) {
                        Text("Sign In")
                    }
                }
            }
        }

        // Session ID at the bottom
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.BottomStart
        ) {
            Text(
                text = "Session ID: $sessionId",
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}




