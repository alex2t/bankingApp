package com.example.atm_osphere.viewmodels.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.atm_osphere.utils.database.UserDatabaseHelper


class AuthViewModelFactory(
    private val userDatabaseHelper: UserDatabaseHelper,
    private val passphrase: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(userDatabaseHelper, passphrase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

