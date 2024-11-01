package com.example.atm_osphere.viewmodels.payee

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.atm_osphere.utils.database.PayeeDatabaseHelper

class PayeeViewModelFactory(
    private val databaseHelper: PayeeDatabaseHelper,
    private val passphrase: String,
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PayeeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PayeeViewModel(databaseHelper, passphrase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}