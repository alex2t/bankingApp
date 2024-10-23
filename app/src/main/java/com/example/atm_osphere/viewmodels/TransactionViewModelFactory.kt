package com.example.atm_osphere.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.atm_osphere.utils.TransactionDatabaseHelper

class TransactionViewModelFactory(
    private val databaseHelper: TransactionDatabaseHelper,
    private val passphrase: CharArray
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TransactionViewModel::class.java)) {
            return TransactionViewModel(databaseHelper, passphrase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
