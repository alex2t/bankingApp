package com.example.atmosphere

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.work.WorkManager
import com.example.atmosphere.utils.ApiProvider
import com.example.atmosphere.utils.database.PayeeDatabaseHelper
import com.example.atmosphere.utils.database.TransactionDatabaseHelper
import com.example.atmosphere.utils.database.UserDatabaseHelper
import com.example.atmosphere.viewmodels.auth.AuthViewModel

class ViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            return AuthViewModel(
                UserDatabaseHelper(application),
                PayeeDatabaseHelper(application),
                TransactionDatabaseHelper(application),
                WorkManager.getInstance(application),
                ApiProvider.apiHelper
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
