package com.example.atm_osphere.viewmodels.auth

import com.example.atm_osphere.utils.database.UserDatabaseHelper
import com.example.atm_osphere.utils.database.PayeeDatabaseHelper
import com.example.atm_osphere.model.Payee
import com.example.atm_osphere.model.Transaction
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
import com.example.atm_osphere.utils.database.TransactionDatabaseHelper
import java.util.UUID
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AuthViewModel(private val databaseHelper: UserDatabaseHelper,
                    private val payeeDatabaseHelper: PayeeDatabaseHelper,
                    private val transactionDatabaseHelper: TransactionDatabaseHelper,
                    private val passphrase: String) : ViewModel() {

    private val _statusMessage = MutableStateFlow<Pair<String?, Boolean>?>(null)
    val statusMessage: StateFlow<Pair<String?, Boolean>?> get() = _statusMessage

    private val _puid = MutableStateFlow<String?>(null) // Only set for successful sign-in
    val puid: StateFlow<String?> get() = _puid

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> get() = _isLoading

    private val _loggedIn = MutableStateFlow(false)
    val loggedIn: StateFlow<Boolean> get() = _loggedIn

    init {
        _loggedIn.value = false
    }

    private var database: SQLiteDatabase? = null

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
                            _puid.value = user.permanentUserId // Set PUID only on sign-in
                            _loggedIn.value = true // Set loggedIn flag to true
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
        _puid.value = null
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

                // Generate the current date in yyyy-MM-dd format
                val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val puid = generateUniqueId() // Generate unique ID for the new user
                val newUser = User(puid, email, password, date)

                databaseHelper.insertUserInBackground(newUser, passphrase)
                initializeDefaultDataForUser(puid, passphrase) // Ensure puid is generated and passed correctly

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

    fun initializeDefaultDataForUser(puid: String, passphrase: String) {
        viewModelScope.launch(Dispatchers.IO) {
            // Define default payees
            val defaultPayees = listOf(
                //default payee for MaibPage view Transaction set to false to do not show in payee dropdown in Paypayee
                Payee(null, puid, "Netflix", "US", "US1234567890",false),
                Payee(null, puid, "Salary", "US", "US0987654321",false),
                Payee(null, puid, "John Doe", "US", "US1122334455",false),
                Payee(null, puid, "Gym", "US", "US6677889900",false),
                Payee(null, puid, "Sponsor", "US", "US5566778899",false),
                //default payee for make transaction
                Payee(0, puid, "Frederick Schmidt", "DE", "DE35201202001934568467",true),
                Payee(0, puid, "Peter Hendrik", "NL", "NL38RABO5198491756",true),
                Payee(0, puid, "Pat Murphy", "IE", "IE85BOFI900017779245",true)
            )

            // Insert each payee using PayeeDatabaseHelper
            defaultPayees.forEach { payee ->
                payeeDatabaseHelper.insertPayee(puid,payee, passphrase)
            }

            // Retrieve payee IDs using the helper
            val netflixPayeeId = payeeDatabaseHelper.getPayeeIdByName("Netflix", passphrase)
            val salaryPayeeId = payeeDatabaseHelper.getPayeeIdByName("Salary", passphrase)
            val johnDoePayeeId = payeeDatabaseHelper.getPayeeIdByName("John Doe", passphrase)
            val gymPayeeId = payeeDatabaseHelper.getPayeeIdByName("Gym", passphrase)
            val sponsorPayeeId = payeeDatabaseHelper.getPayeeIdByName("Sponsor", passphrase)

            // Define default transactions
            val defaultTransactions = listOf(
                Transaction(null, puid, netflixPayeeId ?: -1, 25.00, "2024-01-01", "debit"),
                Transaction(null, puid, salaryPayeeId ?: -1, 5000.00, "2024-01-02", "credit"),
                Transaction(null, puid, johnDoePayeeId ?: -1, 500.00, "2024-01-03", "credit"),
                Transaction(null, puid, gymPayeeId ?: -1, 20.00, "2024-01-03", "debit"),
                Transaction(null, puid, sponsorPayeeId ?: -1, 70.00, "2024-01-05", "credit")
            )

            // Insert transactions using a function within your database helper
            defaultTransactions.forEach { transaction ->
                transactionDatabaseHelper.insertTransactionInBackground(transaction, passphrase)
            }
        }
    }



    // Logout Function
    fun logout() {
        Log.d("AuthViewModel", "Entering logout() in AuthViewModel")
        _puid.value = null // Clear PUID on logout
        _statusMessage.value = null // Reset the status message
        _loggedIn.value = false // Ensure loggedIn is reset
        Log.d("AuthViewModel", "Logout called: PUID cleared and loggedIn set to false.")
    }

    // Helper function to generate a unique ID
    private fun generateUniqueId(): String {
        return UUID.randomUUID().toString()
    }

    // Clear the status message
    fun clearStatusMessage() {
        _statusMessage.value = "" to false
    }


}
