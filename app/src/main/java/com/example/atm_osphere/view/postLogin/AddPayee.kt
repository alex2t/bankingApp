package com.example.atm_osphere.view.postLogin

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import com.example.atm_osphere.view.navigation.BasePage
import com.example.atm_osphere.view.navigation.DrawerContent
import com.example.atm_osphere.viewmodels.auth.AuthViewModel
import com.example.atm_osphere.viewmodels.payee.PayeeViewModel
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.Spacer
import android.util.Log

@Composable
fun AddPayee(
    navController: NavHostController,
    sessionId: String,
    puid: String,
    authViewModel: AuthViewModel,
    payeeViewModel: PayeeViewModel
) {
    BasePage(
        navController = navController,
        pageTitle = "Add Payee Page",
        drawerContent = {
            DrawerContent(
                navController = navController,
                sessionId = sessionId,
                puid = puid,
                authViewModel = authViewModel
            )
        },
        content = { paddingValues ->
            Box(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize(),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // AddPayee form content
                    AddPayeeForm(payeeViewModel = payeeViewModel, puid = puid)

                    // SessionId and PUID at the bottom
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(text = "Session ID: $sessionId")
                        Text(text = "PUID: $puid")
                    }
                }
            }
        },
        sessionId = sessionId,
        puid = puid,
        authViewModel = authViewModel
    )
}

@Composable
fun AddPayeeForm(
    payeeViewModel: PayeeViewModel,
    puid: String,
) {
    var name by remember { mutableStateOf("") }
    var selectedCountry by remember { mutableStateOf("DE") }
    val iban by payeeViewModel.iban.collectAsState()
    val statusMessage by payeeViewModel.statusMessage.collectAsState()
    val countryOptions = listOf("ISRAEL","UK","FRANCE","SPAIN","USA","JAPAN")
    var expanded by remember { mutableStateOf(false) }
    var showAddPayeeButton by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        TextField(
            value = name,
            onValueChange = {
                if (it.all { char -> char.isLetter() }) {
                    if (it.length <= 20) {
                        name = it
                        payeeViewModel.resetStatusMessage()
                    }
                } else {
                    payeeViewModel.resetStatusMessage()
                }
            },
            label = { Text("Payee Name") },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Text),
            maxLines = 1,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Country Dropdown Menu
        Box {
            Text(
                text = "Country: $selectedCountry",
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = true }
                    .padding(16.dp)
            )
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                countryOptions.forEach { country ->
                    DropdownMenuItem(
                        onClick = {
                            selectedCountry = country // Update selected country
                            expanded = false // Close the menu
                        },
                        text = { Text(text = country) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

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

        Spacer(modifier = Modifier.height(16.dp))

        // Generate IBAN Button
        if (iban == null) {
            Button(
                onClick = {
                    coroutineScope.launch {
                        payeeViewModel.generateIban(selectedCountry)
                        showAddPayeeButton = true
                    }
                },
                enabled = name.isNotEmpty() && selectedCountry.isNotEmpty(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Generate IBAN")
            }
        } else {
            Text("IBAN: $iban", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(16.dp))

            // Add Payee Button
            if (showAddPayeeButton) {
                Button(
                    onClick = {
                        payeeViewModel.addPayee(puid, name, selectedCountry)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Add Payee")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
