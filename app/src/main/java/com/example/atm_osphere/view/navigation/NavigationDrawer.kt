package com.example.atm_osphere.view.navigation

import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.atm_osphere.viewmodels.auth.AuthViewModel

@Composable
fun DrawerContent(
    navController: NavHostController,
    sessionId: String,
    puid: String,
    authViewModel: AuthViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(1.dp)
            .fillMaxHeight(),
        verticalArrangement = Arrangement.Top
    ) {
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // Main Account Button
        TextButton(
            onClick = {
                navController.navigate("mainpage/$sessionId/$puid")
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Filled.AccountCircle,
                contentDescription = "Main Account",
                modifier = Modifier.padding(end = 8.dp)
            )
            Text(text = "Main Account", fontSize = 20.sp)
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // Transaction Button
        TextButton(
            onClick = {
                navController.navigate("payPayee/$sessionId/$puid")
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Transaction", fontSize = 20.sp)
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // Add Payee Button
        TextButton(
            onClick = {
                navController.navigate("managepayee/$sessionId/$puid")
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "Add Payee",
                modifier = Modifier.padding(end = 8.dp)
            )
            Text(text = "Add Payee", fontSize = 20.sp)
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // Logout Button
        TextButton(
            onClick = {
                authViewModel.logout() // Perform logout action in AuthViewModel
                navController.navigate("home") {
                    popUpTo("home") { inclusive = true } // Navigate to HomePage and clear backstack
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Filled.ExitToApp,
                contentDescription = "Logout",
                modifier = Modifier.padding(end = 8.dp)
            )
            Text(text = "Logout", fontSize = 20.sp)
        }
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
    }
}
