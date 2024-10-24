package com.example.atm_osphere.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.atm_osphere.model.Transaction
import com.example.atm_osphere.utils.TransactionDatabaseHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class TransactionViewModel(
    private val databaseHelper: TransactionDatabaseHelper,
    private val passphrase: CharArray
) : ViewModel() {

    // StateFlow for the transactions list
    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList()) // Use StateFlow instead of LiveData
    val transactions: StateFlow<List<Transaction>> get() = _transactions

    // StateFlow for the loading state
    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> get() = _loading

    // Function to fetch transactions for a given puid
    fun fetchTransactions(puid: String) {
        viewModelScope.launch(Dispatchers.IO) {
            Log.d("TransactionViewModel", "Fetching transactions for PUID: $puid")
            _loading.value = true // Start loading spinner

            try {
                // Fetch transactions from the database
                var transactions = databaseHelper.getTransactionsByPuid(puid, passphrase.joinToString(""))
                Log.d("TransactionViewModel", "Fetched ${transactions.size} transactions")

                if (transactions.isEmpty()) {
                    // If no transactions are found, insert default transactions
                    Log.d("TransactionViewModel", "No transactions found. Inserting defaults.")
                    databaseHelper.insertDefaultTransactionsForPuid(puid, passphrase.joinToString(""))

                    // Re-fetch the transactions after inserting default ones
                    transactions = databaseHelper.getTransactionsByPuid(puid, passphrase.joinToString(""))
                    Log.d("TransactionViewModel", "Re-fetched ${transactions.size} transactions after inserting defaults.")
                }

                // Update StateFlow with the fetched transactions
                withContext(Dispatchers.Main) {
                    Log.d("TransactionViewModel", "Updating StateFlow with ${transactions.size} transactions")
                    _transactions.value = transactions // Update StateFlow
                    Log.d("TransactionViewModel", "Updated transactions: ${_transactions.value.size}")
                    _loading.value = false // Stop loading spinner
                    Log.d("TransactionViewModel", "StateFlow updated with ${transactions.size} transactions")
                }
            } catch (e: Exception) {
                Log.e("TransactionViewModel", "Error fetching transactions", e)
                withContext(Dispatchers.Main) {
                    _loading.value = false // Stop loading spinner on error
                }
            }
        }
    }
}
