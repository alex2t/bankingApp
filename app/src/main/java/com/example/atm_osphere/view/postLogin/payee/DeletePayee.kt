package com.example.atm_osphere.view.postLogin.payee


import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.example.atm_osphere.viewmodels.payee.PayeeViewModel

import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape

@Composable
fun DeletePayee(
    //navController: NavHostController,
    sessionId: String,
    puid: String,
    payeeViewModel: PayeeViewModel
) {
    val payees by payeeViewModel.payees.collectAsState() // Collect the list of payees
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val deletePayeeStatusMessage by payeeViewModel.deletePayeeStatusMessage.collectAsState()
    val isLoading by payeeViewModel.isLoading.collectAsState()


    LaunchedEffect(puid) {
        payeeViewModel.getPayeesByPuid(puid)
    }

    LaunchedEffect(deletePayeeStatusMessage) {
        deletePayeeStatusMessage?.let { (message, _) ->
            coroutineScope.launch {
                snackbarHostState.showSnackbar(message)
                payeeViewModel.resetDeletePayeeStatusMessage()
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (payees.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No payees available.", style = MaterialTheme.typography.bodyMedium)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    items(payees) { payee ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Name: ${payee.name}", style = MaterialTheme.typography.bodyLarge)
                                    Text("Country: ${payee.country}", style = MaterialTheme.typography.bodySmall)
                                }
                                Button(
                                    onClick = { payeeViewModel.deletePayee(puid, payee) },
                                    modifier = Modifier.padding(start = 16.dp)
                                ) {
                                    Text("Delete")
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Text("Session ID: $sessionId", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 8.dp))
            Text("PUID: $puid", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(bottom = 8.dp))
        }
    }
}

