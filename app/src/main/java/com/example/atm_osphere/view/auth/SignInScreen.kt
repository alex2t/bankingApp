package com.example.atm_osphere.view.auth

import com.example.atm_osphere.viewmodels.auth.AuthViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Alignment
import androidx.navigation.NavController
import androidx.compose.ui.text.input.PasswordVisualTransformation


@Composable
fun SignInScreen(viewModel: AuthViewModel, navController: NavController, sessionId: String) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val isLoading by viewModel.isLoading.collectAsState()

    val statusMessage by viewModel.statusMessage.collectAsState()
    val puid by viewModel.puid.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.clearStatusMessage()
    }
    // When PUID is available, navigate to the main page
    LaunchedEffect(puid) {
        if (puid != null && puid!!.isNotBlank()) {
            navController.navigate("mainpage/$sessionId/$puid") {
                popUpTo("home") { inclusive = true }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center, // Center content vertically
        horizontalAlignment = Alignment.CenterHorizontally // Align content horizontally
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally // Align items in the column
        ) {
            TextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                singleLine = true
            )

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

            Button(
                onClick = {
                    focusManager.clearFocus()
                    if (email.isNotBlank() && password.isNotBlank()) {
                        viewModel.signIn(email, password, sessionId)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = email.isNotBlank() && password.isNotBlank()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Sign In")
                }
            }

            statusMessage?.let { (message, isSuccess) ->
                if (message?.isNotBlank() == true) {
                    Text(
                        text = message,
                        color = if (isSuccess) Color.Green else Color.Red,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.weight(1f))

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
}


