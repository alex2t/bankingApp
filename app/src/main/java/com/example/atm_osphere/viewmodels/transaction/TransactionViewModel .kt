package com.example.atm_osphere.viewmodels.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.atm_osphere.model.TransactionWithPayee
import com.example.atm_osphere.utils.database.TransactionDatabaseHelper
import com.example.atm_osphere.model.Transaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.atm_osphere.utils.workers.TransactionDatabaseWorker
import androidx.work.WorkInfo
import androidx.work.OneTimeWorkRequestBuilder

import android.content.Context

class TransactionViewModel(
    private val databaseHelper: TransactionDatabaseHelper,
    private val workManager: WorkManager,
    private val passphrase: CharArray
) : ViewModel() {

    private val _transactions = MutableStateFlow<List<TransactionWithPayee>>(emptyList())
    val transactions: StateFlow<List<TransactionWithPayee>> get() = _transactions

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> get() = _loading

    private val _transactionStatus = MutableStateFlow<String?>(null)
    val transactionStatus: StateFlow<String?> get() = _transactionStatus

    // Function to fetch transactions for a given puid
    fun fetchTransactions(puid: String) {
        _loading.value = true // Start loading spinner
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Fetch transactions from the database
                var transactions = databaseHelper.getTransactionsWithPayeeName(puid, passphrase.joinToString(""))
                Log.d("TransactionViewModel", "Fetched ${transactions.size} transactions")

                if (transactions.isEmpty()) {
                    // If no transactions are found, insert default transactions
                    Log.d("TransactionViewModel", "No transactions found. Inserting defaults.")
                    databaseHelper.insertDefaultTransactionsForPuid(puid, passphrase.joinToString(""))

                    // Re-fetch the transactions after inserting default ones
                    transactions = databaseHelper.getTransactionsWithPayeeName(puid, passphrase.joinToString(""))
                    Log.d("TransactionViewModel", "Fetching transactions for PUID: $puid")
                    Log.d("TransactionViewModel", "Re-fetched ${transactions.size} transactions after inserting defaults.")
                }

                // Update StateFlow with the fetched transactions
                withContext(Dispatchers.Main) {
                    _transactions.value = transactions // Update StateFlow
                    _loading.value = false // Stop loading spinner
                }
            } catch (e: Exception) {
                Log.e("TransactionViewModel", "Error fetching transactions", e)
                withContext(Dispatchers.Main) {
                    _loading.value = false // Stop loading spinner on error
                }
            }
        }
    }

    // Function to insert a transaction using WorkManager
    fun insertTransactionInBackground(transaction: Transaction) {
        _loading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val inputData = Data.Builder()
                    .putString("puid", transaction.puid)
                    .putInt("payee_id", transaction.payeeId)
                    .putDouble("amount", transaction.amount)
                    .putString("date", transaction.date)
                    .putString("transaction_type", transaction.transactionType)
                    .putString("passphrase", passphrase.joinToString(""))
                    .build()

                val workRequest = OneTimeWorkRequestBuilder<TransactionDatabaseWorker>()
                    .setInputData(inputData)
                    .build()

                workManager.enqueue(workRequest)

                // Switch to the main thread to observe the work result
                withContext(Dispatchers.Main) {
                    workManager.getWorkInfoByIdLiveData(workRequest.id)
                        .observeForever { workInfo ->
                            if (workInfo != null && workInfo.state.isFinished) {
                                if (workInfo.state == WorkInfo.State.SUCCEEDED) {
                                    _transactionStatus.value = "Transaction successful"
                                } else {
                                    _transactionStatus.value = "Transaction failed"
                                }
                                _loading.value = false
                            }
                        }
                }
            } catch (e: Exception) {
                Log.e("TransactionViewModel", "Error inserting transaction", e)
                _transactionStatus.value = "Transaction failed: ${e.localizedMessage}"
                _loading.value = false
            }
        }
    }

    fun clearTransactionStatus() {
        _transactionStatus.value = null
    }
}

