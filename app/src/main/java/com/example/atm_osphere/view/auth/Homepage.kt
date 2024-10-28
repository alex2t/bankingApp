package com.example.atm_osphere.view.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.runtime.remember
import java.util.UUID

@Composable
fun HomePage(navController: NavController) {
    // Create a new sessionId only on the homepage
    val sessionId = remember { UUID.randomUUID().toString() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Welcome to ATM_OSPHERE BANK",
            fontSize = 32.sp,
            modifier = Modifier
                .fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Row(
            modifier = Modifier
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = {
                // Pass the sessionId to SignInScreen
                navController.navigate("signIn/$sessionId")
            }) {
                Text("Sign In")
            }
            Button(onClick = {
                // Pass the sessionId to CreateAccountScreen
                navController.navigate("createAccount/$sessionId")
            }) {
                Text("Register")
            }
        }

        // Display session ID at the bottom left
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.BottomStart
        ) {
            Text(
                text = "Session ID: $sessionId",
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}


