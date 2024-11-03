package com.example.atm_osphere.view.postLogin

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.atm_osphere.view.navigation.BasePage
import com.example.atm_osphere.view.navigation.DrawerContent
import com.example.atm_osphere.viewmodels.auth.AuthViewModel
import com.example.atm_osphere.viewmodels.payee.PayeeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.atm_osphere.model.Payee
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import com.example.atm_osphere.viewmodels.transaction.TransactionViewModel
import com.example.atm_osphere.model.Transaction
import androidx.compose.foundation.clickable
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Icon
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState


@Composable
fun PayPayee(
    navController: NavHostController,
    sessionId: String,
    puid: String,
    authViewModel: AuthViewModel,
    payeeViewModel: PayeeViewModel,
    transactionviewModel: TransactionViewModel,
) {
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() } // Localized to PayPayee
    var selectedPayee by remember { mutableStateOf<Payee?>(null) }
    var amount by remember { mutableStateOf("") }
    val payees by payeeViewModel.payees.collectAsState(initial = emptyList())
    val isLoading by transactionviewModel.loading.collectAsState()
    val transactionStatus by transactionviewModel.transactionStatus.collectAsState()

    LaunchedEffect(puid) {
        payeeViewModel.getPayeesByPuid(puid)
    }

    LaunchedEffect(transactionStatus) {
        transactionStatus?.let {
            coroutineScope.launch {
                snackbarHostState.showSnackbar(it)
                transactionviewModel.clearTransactionStatus()
                // Reset input fields on success
                if (it == "Transaction successful") {
                    amount = "" // Clear amount text field
                    selectedPayee = null // Reset dropdown to initial value
                }
            }
        }
    }

    BasePage(
        navController = navController,
        pageTitle = "Make Payment",
        drawerContent = {
            DrawerContent(
                navController = navController,
                sessionId = sessionId,
                puid = puid,
                authViewModel = authViewModel
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Select Payee", style = MaterialTheme.typography.bodyLarge)

                PayPayeeDropdown(
                    payees = payees,
                    onPayeeSelected = { payee -> selectedPayee = payee },
                    selectedPayee = selectedPayee
                )

                OutlinedTextField(
                    value = amount,
                    onValueChange = { input ->
                        if (input.matches(Regex("^\\d{0,6}(\\.\\d{0,2})?$"))) {
                            amount = input
                        }
                    },
                    label = { Text("Amount") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
                )

                Button(
                    onClick = {
                        if (selectedPayee != null && amount.isNotEmpty()) {
                            coroutineScope.launch {
                                transactionviewModel.insertTransactionInBackground(
                                    Transaction(
                                        puid = puid,
                                        name = selectedPayee!!.name,
                                        type = "debit",
                                        amount = amount.toDouble()
                                    )
                                )
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading // Disable button while loading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Pay")
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "Session ID: $sessionId",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "PUID: $puid",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        },
        sessionId = sessionId,
        puid = puid,
        authViewModel = authViewModel,
        snackbarHostState = snackbarHostState // Pass snackbarHostState to BasePage
    )
}


@Composable
fun PayPayeeDropdown(
    payees: List<Payee>,
    onPayeeSelected: (Payee) -> Unit,
    selectedPayee: Payee?

) {
    var expanded by remember { mutableStateOf(false) }
    var selectedPayeeText by remember { mutableStateOf("Select a Payee") }

    // Reset dropdown text when selectedPayee is null
    LaunchedEffect(selectedPayee) {
        if (selectedPayee == null) {
            selectedPayeeText = "Select a Payee" // Reset to initial value
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = true }
            .padding(8.dp)
            .border(1.dp, Color.Gray, shape = RoundedCornerShape(4.dp))
            .padding(8.dp)
    ) {
        // Display selected payee or default text
        Text(
            text = selectedPayeeText,
            modifier = Modifier.align(Alignment.CenterStart)
        )

        // Dropdown arrow icon
        Icon(
            imageVector = Icons.Default.ArrowDropDown,
            contentDescription = "Dropdown arrow",
            modifier = Modifier.align(Alignment.CenterEnd)
        )

        // Dropdown menu to display payee options
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            payees.forEach { payee ->
                DropdownMenuItem(
                    onClick = {
                        onPayeeSelected(payee)
                        selectedPayeeText = "${payee.name} - ${payee.iban}"
                        expanded = false
                    },
                    text = { Text(text = "${payee.name} - ${payee.iban}") }
                )
            }
        }
    }
}

