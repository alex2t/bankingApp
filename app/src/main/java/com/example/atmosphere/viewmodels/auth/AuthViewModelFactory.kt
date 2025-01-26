package com.example.atmosphere.viewmodels.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.work.WorkManager
import com.example.atmosphere.utils.database.UserDatabaseHelper
import com.example.atmosphere.utils.database.TransactionDatabaseHelper
import com.example.atmosphere.utils.database.PayeeDatabaseHelper
import com.example.atmosphere.utils.api.ApiHelper


class AuthViewModelFactory(
    private val userDatabaseHelper: UserDatabaseHelper,
    private val payeeDatabaseHelper: PayeeDatabaseHelper,
    private val transactionDatabaseHelper: TransactionDatabaseHelper,
    private val workManager: WorkManager,
    private val apiHelper: ApiHelper
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(
                userDatabaseHelper,
                payeeDatabaseHelper,
                transactionDatabaseHelper,
                workManager,
                apiHelper) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

