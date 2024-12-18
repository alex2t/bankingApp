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
import com.example.atm_osphere.model.TransactionPayload
import com.example.atm_osphere.utils.OutputManager
import com.example.atm_osphere.utils.api.ApiHelper


class TransactionViewModel(
    private val databaseHelper: TransactionDatabaseHelper,
    private val workManager: WorkManager,
    private val apiHelper: ApiHelper
) : ViewModel() {

    private val _transactions = MutableStateFlow<List<TransactionWithPayee>>(emptyList())
    val transactions: StateFlow<List<TransactionWithPayee>> get() = _transactions

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> get() = _loading

    private val _transactionStatus = MutableStateFlow<String?>(null)
    val transactionStatus: StateFlow<String?> get() = _transactionStatus

    private val _deleteTransactionStatus = MutableStateFlow<Result<String>>(Result.success(""))


    fun fetchTransactions(puid: String) {
        _loading.value = true // Start loading spinner
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val transactions = databaseHelper.getTransactionsWithPayeeName(puid)
                Log.d("TransactionViewModel", "Fetched ${transactions.size} transactions")

                withContext(Dispatchers.Main) {
                    _transactions.value = transactions
                    _loading.value = false
                }
            } catch (e: Exception) {
                Log.e("TransactionViewModel", "Error fetching transactions", e)
                withContext(Dispatchers.Main) {
                    _loading.value = false
                }
            }
        }
    }

    fun insertTransactionInBackground(transaction: Transaction, sessionId: String, selectedPayeeIban: String, selectedPayeeCountry: String) {
        _loading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val (userAgent, remoteIp) = OutputManager.getUserAgentAndRemoteIp()

                val inputData = Data.Builder()
                    .putString("puid", transaction.puid)
                    .putInt("payee_id", transaction.payeeId)
                    .putDouble("amount", transaction.amount)
                    .putString("date", transaction.date)
                    .putString("transaction_type", transaction.transactionType)
                    .build()

                val workRequest = OneTimeWorkRequestBuilder<TransactionDatabaseWorker>()
                    .setInputData(inputData)
                    .build()

                workManager.enqueue(workRequest)
                withContext(Dispatchers.Main) {
                    workManager.getWorkInfoByIdLiveData(workRequest.id)
                        .observeForever { workInfo ->
                            if (workInfo != null && workInfo.state.isFinished) {
                                if (workInfo.state == WorkInfo.State.SUCCEEDED) {
                                    _transactionStatus.value = "Transaction successful"


                                    val payload = TransactionPayload(
                                        sessionId = sessionId,
                                        permanentUserId = transaction.puid,
                                        payeeIban = selectedPayeeIban,
                                        payeeCountry = selectedPayeeCountry,
                                        amount = transaction.amount,
                                        transactionType = transaction.transactionType,
                                        userAgent = userAgent,
                                        remoteIp = remoteIp
                                    )

                                    // Call API
                                    viewModelScope.launch(Dispatchers.IO) {
                                        apiTransaction(payload)
                                    }
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
    private suspend fun apiTransaction(payload: TransactionPayload) {
        try {
            val response = apiHelper.makeTransaction(payload)
            Log.d("TransactionViewModel", "API Transaction response: $response")
        } catch (e: Exception) {
            Log.e("TransactionViewModel", "Error making transaction API call", e)
        }
    }

    fun deleteTransaction(transaction: TransactionWithPayee) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val rowsAffected = databaseHelper.deleteTransaction(transaction.transactionId.toLong())
                if (rowsAffected > 0) {
                    _deleteTransactionStatus.value = Result.success("Transaction deleted successfully.")
                } else {
                    _deleteTransactionStatus.value = Result.failure(Exception("Transaction not found or could not be deleted."))
                }
            } catch (e: Exception) {
                _deleteTransactionStatus.value = Result.failure(e)
            }
        }
    }
    fun undoDelete(transaction: TransactionWithPayee) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                databaseHelper.insertTransaction(transaction) // Reinsert the deleted transaction
                fetchTransactions(transaction.puid) // Refresh the transaction list
            } catch (e: Exception) {
                Log.e("TransactionViewModel", "Error undoing transaction deletion", e)
            }
        }
    }
    fun clearTransactionStatus() {
        _transactionStatus.value = null
    }
}

