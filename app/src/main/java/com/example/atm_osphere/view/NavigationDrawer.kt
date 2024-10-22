
package com.example.atm_osphere.view
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp  // For font size control
import androidx.navigation.NavHostController




@Composable
fun DrawerContent(
    navController: NavHostController,
    sessionId: String,
    puid: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(16.dp)  // Add padding inside the drawer content
            .fillMaxHeight(),  // Fill height of drawer content
        verticalArrangement = Arrangement.Top
    ) {

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))  // Add smaller dividers

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
            Text(
                text = "Main Account",
                fontSize = 20.sp  // Increase text size
            )
        }
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // Transaction Button
        TextButton(
            onClick = {
                navController.navigate("transaction/$sessionId/$puid")
            },
            modifier = Modifier.fillMaxWidth()
        ) {
//            Icon(
//                imageVector = Icons.Filled.Money,
//                contentDescription = "Transaction",
//                modifier = Modifier.padding(end = 8.dp)
//            )
            Text(
                text = "Transaction",
                fontSize = 20.sp  // Increase text size
            )
        }
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // Add Payee Button
        TextButton(
            onClick = {
                navController.navigate("addpayee/$sessionId/$puid")
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "Add Payee",
                modifier = Modifier.padding(end = 8.dp)
            )
            Text(
                text = "Add Payee",
                fontSize = 20.sp  // Increase text size
            )
        }

        // Logout Section with Divider
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // Logout Button
        TextButton(
            onClick = {
                navController.navigate("home") {
                    popUpTo("mainpage") { inclusive = true }  // Navigate to HomePage and clear backstack
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
//            Icon(
//                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
//                contentDescription = "Exit to app",
//                modifier = Modifier.padding(end = 8.dp)
//            )
            Text(
                text = "Logout",
                fontSize = 20.sp  // Increase text size
            )
        }
    }
}
