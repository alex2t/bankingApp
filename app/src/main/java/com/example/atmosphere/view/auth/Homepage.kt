package com.example.atmosphere.view.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
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
            .fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.athmosphere), // Ensure the logo is in drawable
            contentDescription = "ATM_OSPHERE Logo",
            modifier = Modifier
                .width(350.dp)
                .aspectRatio(1f)
                .padding(top = 30.dp )

        )
        //Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = buildAnnotatedString {
                withStyle(style = SpanStyle(
                    fontSize = 50.sp,
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
                .padding(top=30.dp, bottom = 180.dp,),
            textAlign = TextAlign.Center,
            lineHeight = 50.sp
        )
        //Spacer(modifier = Modifier.height(42.dp))

        Text(
            text = "Session ID: $sessionId",
           // modifier = Modifier
                //.padding(top = 40.dp,),
                //.align(Alignment.CenterHorizontally),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(2.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                 .padding(bottom= 20.dp),
//                .padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.SpaceBetween

        ) {
            Button(onClick = { navController.navigate("signIn/$sessionId") },
                    modifier = Modifier
                    .weight(1f)
                     .padding(end = 8.dp)

            ) {
                BasicText(
                    "Sign In",
                    style = TextStyle(color = Color.White))
            }
            Button(onClick = { navController.navigate("createAccount/$sessionId") },
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)

                ) {
                BasicText(
                    "Register",
                    style = TextStyle(color = Color.White))
            }
        }
    }
}
