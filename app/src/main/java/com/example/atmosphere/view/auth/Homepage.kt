package com.example.atmosphere.view.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.atmosphere.ui.theme.LogoFontFamily
import com.example.atmosphere.ui.theme.SecondFontFamily
import androidx.compose.runtime.remember
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import com.example.atmosphere.R


@Composable
fun HomePage(navController: NavController) {
    val sessionId = remember { java.util.UUID.randomUUID().toString() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()) // Allows content to scroll on smaller screens
            .padding(16.dp), // General padding for consistent spacing
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.athmosphere),
            contentDescription = "ATM_OSPHERE Logo",
            modifier = Modifier
                .fillMaxWidth(0.8f) // Scales width to 80% of screen width
                .aspectRatio(1f)
                .padding(top = 30.dp)
        )

        Text(
            text = buildAnnotatedString {
                withStyle(style = SpanStyle(
                    fontSize = 40.sp,
                    fontFamily = LogoFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.tertiary
                )) {
                    append("ATM_OSPHERE\n")
                }
                withStyle(style = SpanStyle(fontSize = 32.sp, fontFamily = SecondFontFamily)) {
                    append("BANK")
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp), // Dynamic spacing
            textAlign = TextAlign.Center,
            lineHeight = 50.sp
        )

        Text(
            text = "Session ID: $sessionId",
            modifier = Modifier.padding(vertical = 8.dp), // Adds some spacing
            textAlign = TextAlign.Center
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = { navController.navigate("signIn/$sessionId") },
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            ) {
                BasicText("Sign In", style = TextStyle(color = Color.White))
            }

            Button(
                onClick = { navController.navigate("createAccount/$sessionId") },
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
            ) {
                BasicText("Register", style = TextStyle(color = Color.White))
            }
        }
    }
}

