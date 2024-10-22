package com.example.atm_osphere.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.livedata.observeAsState
import androidx.navigation.NavHostController
import com.example.atm_osphere.viewmodels.TransactionViewModel
import com.example.atm_osphere.model.Transaction
import kotlinx.coroutines.flow.collectLatest
import android.util.Log

@Composable
fun MainPage(
    navController: NavHostController,
    sessionId: String,
    puid: String,
    viewModel: TransactionViewModel
) {
    var transactions by remember { mutableStateOf(emptyList<Transaction>()) }
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(puid) {
        viewModel.fetchTransactions(puid)

        viewModel.transactions.collectLatest { newTransactions ->
            transactions = newTransactions
        }

        viewModel.loading.collectLatest { loadingState ->
            isLoading = loadingState
        }
    }

    BasePage(
        navController = navController,
        pageTitle = "Transaction Page",
        drawerContent = {
            DrawerContent(navController = navController, sessionId = sessionId, puid = puid)
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
                    // Main content at the top (spinner or transactions)
                    when {
                        isLoading -> {
                            CircularProgressIndicator(modifier = Modifier.padding(16.dp)) // Show spinner while loading
                        }
                        transactions.isNotEmpty() -> {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                items(transactions) { transaction ->
                                    TransactionItem(transaction = transaction) // Use the customized TransactionItem composable
                                }
                            }
                        }
                        else -> {
                            // If loading is false and transactions are empty, show the message
                            Text("No transactions found", modifier = Modifier.padding(16.dp))
                        }
                    }

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
        puid = puid
    )
}
