package com.example.atmosphere.view.postLogin.transaction

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.atmosphere.view.navigation.BasePage
import com.example.atmosphere.view.navigation.DrawerContent
import com.example.atmosphere.viewmodels.auth.AuthViewModel
import com.example.atmosphere.viewmodels.payee.PayeeViewModel
import kotlinx.coroutines.launch
import com.example.atmosphere.model.Payee
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import com.example.atmosphere.viewmodels.transaction.TransactionViewModel
import com.example.atmosphere.model.Transaction
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedPayee by remember { mutableStateOf<Payee?>(null) }
    var selectedPayeeId by remember { mutableStateOf<Int?>(null) }
    var selectedPayeeIban by remember { mutableStateOf<String?>(null) }
    var selectedPayeeCountry by remember { mutableStateOf<String?>(null) }
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
                if (it == "Transaction successful") {
                    amount = ""
                    selectedPayee = null
                    selectedPayeeId = null
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
                authViewModel = authViewModel,

            )
        },
        content = { paddingValues ->

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center // ✅ Centers the content inside Box

            ) {
                Column(
                    modifier = Modifier
                        .padding(paddingValues)
                        .fillMaxSize(0.85f)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .heightIn(min = 250.dp), // Ensures space for UI elements
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.onSecondary)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)

                        ) {
                            Text("Select Payee", style = MaterialTheme.typography.bodyLarge)
                            Spacer(modifier = Modifier.height(16.dp))

                            // Payee Dropdown inside Card
                            PayPayeeDropdown(
                                payees = payees,
                                onPayeeSelected = { payee ->
                                    selectedPayee = payee
                                    selectedPayeeId = payee.payeeId
                                    selectedPayeeIban = payee.iban
                                    selectedPayeeCountry = payee.country
                                },
                                selectedPayee = selectedPayee
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Amount Input Field
                            OutlinedTextField(
                                value = amount,
                                onValueChange = { input ->
                                    if (input.matches(Regex("^\\d{0,6}(\\.\\d{0,2})?$"))) {
                                        amount = input
                                    }
                                },
                                label = { Text("Amount") },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
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

                            // Pay Button
                            Button(
                                onClick = {
                                    if (selectedPayeeId != null && amount.isNotEmpty()) {
                                        val currentDate = SimpleDateFormat(
                                            "yyyy-MM-dd",
                                            Locale.getDefault()
                                        ).format(Date())
                                        coroutineScope.launch {
                                            val transaction = Transaction(
                                                transactionId = null,
                                                puid = puid,
                                                payeeId = selectedPayeeId!!,
                                                amount = amount.toDouble(),
                                                date = currentDate,
                                                transactionType = "debit"
                                            )

                                            transactionviewModel.insertTransactionInBackground(
                                                transaction = transaction,
                                                sessionId = sessionId,
                                                selectedPayeeIban = selectedPayeeIban
                                                    ?: "Unknown IBAN",
                                                selectedPayeeCountry = selectedPayeeCountry
                                                    ?: "Unknown Country"
                                            )
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !isLoading
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
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Session ID and PUID remain at the bottom
                    Text(
                        text = "Session ID: $sessionId",
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.onSecondary,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "PUID: $puid",
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.onSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }

        },
        sessionId = sessionId,
        puid = puid,
        authViewModel = authViewModel,
        snackbarHostState = snackbarHostState
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
    val colors = listOf(
        MaterialTheme.colorScheme.onTertiary,
        MaterialTheme.colorScheme.onPrimary
    )

    LaunchedEffect(selectedPayee) {
        if (selectedPayee == null) {
            selectedPayeeText = "Select a Payee"
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = true }
            .border(1.dp, Color.Gray, shape = RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.onPrimary)
            .padding(8.dp),

    ) {
        Text(
            text = selectedPayeeText,
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
                .border(1.dp, MaterialTheme.colorScheme.onPrimary, shape = RoundedCornerShape(8.dp))
        ) {
            payees.forEachIndexed { index, payee ->
                val backgroundColor = colors[index % colors.size] // Alternate colors
                DropdownMenuItem(
                    onClick = {
                        onPayeeSelected(payee)
                        selectedPayeeText = "${payee.name} - ${payee.iban}"
                        expanded = false
                    },
                    modifier = Modifier.background(backgroundColor),
                    text = {
                        Text(text = "${payee.name} - ${payee.iban}", color = MaterialTheme.colorScheme.onSurface)
                    }
                )
                if (index < payees.lastIndex) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                }
            }
        }
    }
}

