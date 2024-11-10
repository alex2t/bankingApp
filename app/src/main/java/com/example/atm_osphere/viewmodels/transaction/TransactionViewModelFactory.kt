package com.example.atm_osphere.viewmodels.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.atm_osphere.utils.database.TransactionDatabaseHelper
import android.content.Context
import androidx.work.WorkManager



class TransactionViewModelFactory(
    private val databaseHelper: TransactionDatabaseHelper,
    context: Context, // Receive context here
    private val passphrase: CharArray
) : ViewModelProvider.Factory {

    private val workManager = WorkManager.getInstance(context) // Initialize WorkManager here

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TransactionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TransactionViewModel(databaseHelper, workManager, passphrase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
