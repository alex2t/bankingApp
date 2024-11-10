package com.example.atm_osphere.viewmodels.payee

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.work.WorkManager
import com.example.atm_osphere.utils.database.PayeeDatabaseHelper

import android.content.Context


class PayeeViewModelFactory(
    private val context: Context, // Only Context is passed here
    private val passphrase: String
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PayeeViewModel::class.java)) {
            val databaseHelper = PayeeDatabaseHelper(context)
            val workManager = WorkManager.getInstance(context) // Initialize WorkManager here
            @Suppress("UNCHECKED_CAST")
            return PayeeViewModel(databaseHelper, workManager, passphrase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

