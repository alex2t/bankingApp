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

@Composable
fun PayPayee(
    navController: NavHostController,
    sessionId: String,
    puid: String,
    authViewModel: AuthViewModel,
    payeeViewModel: PayeeViewModel,
    transactionviewModel: TransactionViewModel,
) {
    //val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var selectedPayee by remember { mutableStateOf<Payee?>(null) }
    var amount by remember { mutableStateOf("") }
    val payees by payeeViewModel.payees.collectAsState(initial = emptyList())

    LaunchedEffect(puid) {
        payeeViewModel.getPayeesByPuid(puid)
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
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Text("Select Payee", style = MaterialTheme.typography.bodyLarge)

                        // PayPayeeDropdown function call
                        PayPayeeDropdown(
                            payees = payees,
                            onPayeeSelected = { payee -> selectedPayee = payee }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Amount input field
                        OutlinedTextField(
                            value = amount,
                            onValueChange = {
                                if (it.length <= 8 && it.all { ch -> ch.isDigit() }) amount = it
                            },
                            label = { Text("Amount") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Pay button
                        Button(
                            onClick = {
                                if (selectedPayee != null && amount.isNotEmpty()) {
                                    coroutineScope.launch(Dispatchers.IO) {
                                        val transaction = Transaction(
                                            puid = puid,
                                            name = selectedPayee!!.name,
                                            type = "debit",
                                            amount = amount.toDouble()
                                        )
                                        transactionviewModel.insertTransactionInBackground(transaction)
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Pay") }
                    }

                    // Session ID and PUID at the bottom
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
fun PayPayeeDropdown(payees: List<Payee>, onPayeeSelected: (Payee) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    var selectedPayeeText by remember { mutableStateOf("Select a Payee") }

    Box {
        // Text to display selected payee and trigger dropdown
        Text(
            text = selectedPayeeText,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true }
                .padding(16.dp)
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
