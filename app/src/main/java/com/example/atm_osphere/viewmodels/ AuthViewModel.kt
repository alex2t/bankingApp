package com.example.atm_osphere.viewmodels

import com.example.atm_osphere.utils.UserDatabaseHelper
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.sqlcipher.database.SQLiteDatabase
import com.example.atm_osphere.model.User
import android.util.Log

class AuthViewModel(private val databaseHelper: UserDatabaseHelper, private val passphrase: String) : ViewModel() {

    private val _statusMessage = MutableStateFlow<Pair<String?, Boolean>?>(null)
    val statusMessage: StateFlow<Pair<String?, Boolean>?> get() = _statusMessage

    private val _puid = MutableStateFlow<String?>(null)
    val puid: StateFlow<String?> get() = _puid

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> get() = _isLoading

    private var database: SQLiteDatabase? = null

    // Lazily open and cache the database instance
    private fun getDatabaseInstance(): SQLiteDatabase {
        if (database == null) {
            database = databaseHelper.getWritableDatabase(passphrase.toCharArray())
        }
        return database!!
    }

    // Sign In Function
    fun signIn(email: String, password: String) {
        _isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val user = databaseHelper.getUser(email, passphrase)

                withContext(Dispatchers.Main) {
                    val statusMessagePair = when {
                        user == null -> "User not found." to false
                        user.password != password -> "Wrong password." to false
                        else -> {
                            _puid.value = user.permanentUserId
                            "Successfully signed in!" to true
                        }
                    }
                    _statusMessage.value = statusMessagePair
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _statusMessage.value = "Error signing in: ${e.localizedMessage}" to false
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Create Account Function
    fun createAccount(email: String, password: String) {
        _isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val existingUser = databaseHelper.getUser(email, passphrase)

                if (existingUser != null) {
                    withContext(Dispatchers.Main) {
                        _statusMessage.value = "Email already exists." to false
                    }
                    return@launch
                }

                val newUser = User(generateUniqueId(), email, password)
                databaseHelper.insertUserInBackground(newUser, passphrase)

                withContext(Dispatchers.Main) {
                    _statusMessage.value = "Account created successfully." to true
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _statusMessage.value = "Error creating account: ${e.localizedMessage}" to false
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Helper function to generate a unique ID
    private fun generateUniqueId(): String {
        return java.util.UUID.randomUUID().toString()
    }

    // Clear the status message
    fun clearStatusMessage() {
        _statusMessage.value = "" to false
    }

    // Called when ViewModel is about to be destroyed
    override fun onCleared() {
        super.onCleared()
        databaseHelper.closeDatabase(database)
    }
}
