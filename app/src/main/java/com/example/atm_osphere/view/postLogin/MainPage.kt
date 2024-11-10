package com.example.atm_osphere.view.postLogin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.atm_osphere.viewmodels.transaction.TransactionViewModel
import com.example.atm_osphere.viewmodels.auth.AuthViewModel
import com.example.atm_osphere.model.TransactionWithPayee
import com.example.atm_osphere.view.navigation.BasePage
import com.example.atm_osphere.view.navigation.DrawerContent
import kotlinx.coroutines.flow.collectLatest

@Composable
fun MainPage(
    navController: NavHostController,
    sessionId: String,
    puid: String,
    viewModel: TransactionViewModel,
    authViewModel: AuthViewModel
) {
    //var transactions by remember { mutableStateOf(emptyList<Transaction>()) }
    var transactions by remember { mutableStateOf<List<TransactionWithPayee>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }


    LaunchedEffect(puid) {
        viewModel.fetchTransactions(puid)  // Fetch transactions with the payee name included

        viewModel.transactions.collectLatest { newTransactions ->
            transactions = newTransactions
        }

        viewModel.loading.collectLatest { loadingState ->
            isLoading = loadingState
        }
    }

    BasePage(
        navController = navController,
        pageTitle = "View Transactions",
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
                    when {
                        isLoading -> {
                            CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                        }

                        transactions.isNotEmpty() -> {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                items(transactions) { transaction ->
                                    TransactionItem(transaction = transaction)
                                }
                            }
                        }

                        else -> {
                            Text("No transactions found", modifier = Modifier.padding(16.dp))
                        }
                    }

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
