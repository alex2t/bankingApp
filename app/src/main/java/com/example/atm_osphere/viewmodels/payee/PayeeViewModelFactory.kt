package com.example.atm_osphere.viewmodels.payee

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.work.WorkManager
import com.example.atm_osphere.utils.database.PayeeDatabaseHelper
import com.example.atm_osphere.utils.api.ApiHelper

import android.content.Context


class PayeeViewModelFactory(
    private val context: Context, // Only Context is passed here
    private val apiHelper: ApiHelper

) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PayeeViewModel::class.java)) {
            val databaseHelper = PayeeDatabaseHelper(context)
            val workManager = WorkManager.getInstance(context) // Initialize WorkManager here
            @Suppress("UNCHECKED_CAST")
            return PayeeViewModel(databaseHelper, workManager, apiHelper ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

