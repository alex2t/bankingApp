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
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Icon
import androidx.compose.ui.platform.LocalConfiguration

@Composable
fun AddPayee(
    navController: NavHostController,
    sessionId: String,
    puid: String,
    authViewModel: AuthViewModel,
    payeeViewModel: PayeeViewModel
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val offsetFromTop = screenHeight * 0.1f  // 20% from top
    val ibanStatusMessage by payeeViewModel.ibanStatusMessage.collectAsState()
    val addPayeeStatusMessage by payeeViewModel.addPayeeStatusMessage.collectAsState()

    // Show snackbar for IBAN generation status
    LaunchedEffect(ibanStatusMessage) {
        ibanStatusMessage?.let { (message, isSuccess) ->
            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    message = message,
                    actionLabel = if (isSuccess) "OK" else "Retry"
                )
                payeeViewModel.resetIbanStatusMessage()
            }
        }
    }

    // Show snackbar for Add Payee status
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
                    // Display AddPayeeForm content
                    AddPayeeForm(
                        payeeViewModel = payeeViewModel,
                        puid = puid,
                        sessionId = sessionId,
                        modifier = Modifier.padding(top = offsetFromTop)
                    )

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

    // Place the SnackbarHost outside BasePage to display snackbar messages
    SnackbarHost(hostState = snackbarHostState)
}
@Composable
fun AddPayeeForm(
    payeeViewModel: PayeeViewModel,
    puid: String,
    sessionId: String,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf("") }
    var selectedCountry by remember { mutableStateOf("FRANCE") }
    val iban by payeeViewModel.iban.collectAsState()
    val countryOptions = listOf("ISRAEL", "UK", "FRANCE", "SPAIN", "USA", "JAPAN")
    var expanded by remember { mutableStateOf(false) }
    val showAddPayeeButton = iban != null // Show the button only if IBAN is generated

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
                            payeeViewModel.addPayee(puid, name, selectedCountry, nonNullIban)
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
