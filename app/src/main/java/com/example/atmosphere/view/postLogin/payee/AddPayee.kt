package com.example.atmosphere.view.postLogin.payee

import android.util.Log
import androidx.compose.foundation.background
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import com.example.atmosphere.viewmodels.payee.PayeeViewModel
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
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background// Background color for the entire screen
    ) {
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
                .fillMaxSize(),
                //.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.onSecondary)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .padding(top = 24.dp, bottom = 16.dp, start = 16.dp, end = 16.dp),
                    verticalArrangement = Arrangement.Top
                ) {
                    Spacer(modifier = Modifier.height(16.dp))
                    // Payee Name Input
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
                            .background(
                                MaterialTheme.colorScheme.onPrimary,
                                shape = RoundedCornerShape(8.dp)
                            ), // Background color
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.tertiary, // Text color when focused
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface, // Text color when unfocused
                            focusedContainerColor = MaterialTheme.colorScheme.onTertiary, // Background when focused
                            unfocusedContainerColor = MaterialTheme.colorScheme.onPrimary, // Background when unfocused
                            cursorColor = MaterialTheme.colorScheme.primary, // Cursor color
                            focusedIndicatorColor = MaterialTheme.colorScheme.onSecondary, // Outline color when focused

                        )


                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Country Dropdown Menu
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expanded = true }
                            .border(1.dp, Color.Gray, shape = RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.onPrimary)
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
                            onDismissRequest = { expanded = false },
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.onPrimary)
                                .border(
                                    1.dp,
                                    MaterialTheme.colorScheme.onPrimary,
                                    shape = RoundedCornerShape(16.dp)
                                )
                        ) {
                            countryOptions.forEachIndexed { index, country ->
                                DropdownMenuItem(
                                    onClick = {
                                        selectedCountry = country
                                        expanded = false
                                    },
                                    text = { Text(text = country) }
                                )
                                if (index < countryOptions.lastIndex) {
                                    HorizontalDivider(
                                        color = MaterialTheme.colorScheme.onSurface.copy(
                                            alpha = 0.5f
                                        )
                                    )
                                }
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
                        Text(
                            "IBAN: $iban",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = MaterialTheme.colorScheme.primary
                        )

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
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Session ID and PUID at Bottom of the Page
            Text(
                text = "Session ID: $sessionId",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .align(Alignment.CenterHorizontally)
            )
            Text(
                text = "PUID: $puid",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .align(Alignment.CenterHorizontally)
            )
        }

}
