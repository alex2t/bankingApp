package com.example.atm_osphere.view.postLogin.payee

import android.util.Log
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
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Icon
import androidx.compose.ui.platform.LocalConfiguration
import kotlinx.coroutines.delay

@Composable
fun AddPayee(
    //navController: NavHostController,
    sessionId: String,
    puid: String,
   // authViewModel: AuthViewModel,
    payeeViewModel: PayeeViewModel
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val offsetFromTop = screenHeight * 0.1f // 10% from top
    val ibanStatusMessage by payeeViewModel.ibanStatusMessage.collectAsState()
    val addPayeeStatusMessage by payeeViewModel.addPayeeStatusMessage.collectAsState()

    // Display snackbars for status messages
    LaunchedEffect(ibanStatusMessage) {
        Log.d("AddPayee IbanStausMessage", "Triggered LaunchedEffect: $ibanStatusMessage")
        ibanStatusMessage?.let { (message, _) ->
            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    message = message,
                    duration = SnackbarDuration.Short
                )
                delay(2000)
                payeeViewModel.resetIbanStatusMessage()
            }
        }
    }

    LaunchedEffect(addPayeeStatusMessage) {
        addPayeeStatusMessage?.let { (message, isSuccess) ->
            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    message = message,
                    actionLabel = if (isSuccess) "OK" else "Retry"
                )
                payeeViewModel.resetAddPayeeStatusMessage()
            }
        }
    }

    SnackbarHost(hostState = snackbarHostState)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            AddPayeeForm(
                payeeViewModel = payeeViewModel,
                puid = puid,
                sessionId = sessionId,
                addPayeeStatusMessage = addPayeeStatusMessage,
                modifier = Modifier.padding(top = offsetFromTop),
            )
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



}

@Composable
fun AddPayeeForm(
    payeeViewModel: PayeeViewModel,
    puid: String,
    sessionId: String,
    addPayeeStatusMessage: Pair<String, Boolean>?,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf("") }
    var selectedCountry by remember { mutableStateOf("FRANCE") }
    val iban by payeeViewModel.iban.collectAsState()
    val countryOptions = listOf("ISRAEL", "UK", "FRANCE", "SPAIN", "USA", "JAPAN")
    var expanded by remember { mutableStateOf(false) }
    val showAddPayeeButton = iban != null // Show the button only if IBAN is generated


    LaunchedEffect(addPayeeStatusMessage) {
        addPayeeStatusMessage?.let { (_, isSuccess) ->
            if (isSuccess) {
                name = ""
                selectedCountry = "FRANCE"
                payeeViewModel.resetFields()
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        TextField(
            value = name,
            onValueChange = {
                if (it.all { char -> char.isLetter() || char.isWhitespace() }) {
                    if (it.length <= 20) name = it
                }
            },
            label = { Text("Payee Name") },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Text),
            maxLines = 1,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Country Dropdown Menu
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true }
                .padding(16.dp)
                .border(1.dp, Color.Gray, shape = RoundedCornerShape(4.dp))
                .padding(8.dp)
        ) {
            Text(
                text = "Country: $selectedCountry",
                modifier = Modifier.align(Alignment.CenterStart)
            )
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "Dropdown arrow",
                modifier = Modifier.align(Alignment.CenterEnd)
            )

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                countryOptions.forEach { country ->
                    DropdownMenuItem(
                        onClick = {
                            selectedCountry = country
                            expanded = false
                        },
                        text = { Text(text = country) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Generate IBAN Button
        if (iban == null) {
            Button(
                onClick = { payeeViewModel.generateIban(selectedCountry) },
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
                        iban?.let { nonNullIban ->
                            val payeeData = mapOf(
                                "puid" to puid,
                                "name" to name,
                                "country" to selectedCountry,
                                "iban" to nonNullIban,
                                "isDefault" to true
                            )

                            payeeViewModel.addPayee(sessionId, payeeData)

                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Add Payee")
                }
            }
        }
        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "Session ID: $sessionId",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .align(Alignment.CenterHorizontally)
        )
        Text(
            text = "PUID: $puid",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
                .align(Alignment.CenterHorizontally)
        )
    }
}
