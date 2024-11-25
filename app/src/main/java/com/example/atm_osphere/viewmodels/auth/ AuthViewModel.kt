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
import com.example.atm_osphere.model.User
import android.util.Log
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.example.atm_osphere.utils.database.TransactionDatabaseHelper
import com.example.atm_osphere.utils.workers.PayeeDatabaseWorker
import com.example.atm_osphere.utils.OutputManager
import com.example.atm_osphere.utils.api.ApiHelper
import java.util.UUID
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AuthViewModel(private val databaseHelper: UserDatabaseHelper,
                    private val payeeDatabaseHelper: PayeeDatabaseHelper,
                    private val transactionDatabaseHelper: TransactionDatabaseHelper,
                    private val workManager: WorkManager,
                    private val apiHelper: ApiHelper) : ViewModel() {

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


    // Sign In Function
    fun signIn(email: String, password: String, sessionId: String) {
        _isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val user = databaseHelper.getUser(email)

                withContext(Dispatchers.Main) {
                    val statusMessagePair = when {
                        user == null -> "User not found." to false
                        user.password != password -> "Wrong password." to false
                        else -> {
                            _puid.value = user.permanentUserId
                            _loggedIn.value = true
                            val (userAgent, remoteIp) = OutputManager.getUserAgentAndRemoteIp()

                            try {
                                loginCall(sessionId, puid.value ?: "", userAgent, remoteIp)
                            } catch (e: Exception) {
                                Log.e("AuthViewModel", "Error in loginCall: ${e.localizedMessage}")
                            }
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
    // function to send data to the server
    private suspend fun loginCall(sessionId:String , puid: String, userAgent: String, remoteIp: String ) {
        try {
            val response = apiHelper.login(sessionId, puid, userAgent, remoteIp)
            Log.d("AuthViewModel", "response: $response")
        } catch (e: Exception) {
            Log.e("AuthViewModel", "errorLoginCall: ${e.message}")
        }
    }
    // Create Account Function
    fun createAccount(email: String, password: String) {
        _puid.value = null
        _isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val existingUser = databaseHelper.getUser(email)

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

                databaseHelper.insertUserInBackground(newUser)
                initializeDefaultDataForUser(puid) // Ensure puid is generated and passed correctly

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
    private fun Payee.toWorkData(): Data {
        return Data.Builder()
            .putString("puid", puid)
            .putString("name", name)
            .putString("country", country)
            .putString("iban", iban)
            .putInt("isDefault", if (isDefault) 1 else 0)
            .build()
    }
    private fun initializeDefaultDataForUser(puid: String) {
        if (puid.isBlank()) {
            Log.e("AuthViewModel", "puid is blank. Cannot initialize default data.")
            return
        }
        Log.d("AuthViewModel", "puid: $puid")

        viewModelScope.launch {
            try {
                // Insert default payees and wait for completion
                insertDefaultPayees(puid)
                // Once payees are inserted, insert default transactions
                insertDefaultTransactions(puid)
                Log.d("AuthViewModel", "Default data initialized successfully for puid: $puid")
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error initializing default data for user: $e")
            }
        }
    }

    private suspend fun insertDefaultPayees(puid: String) {
        withContext(Dispatchers.IO) {
            val defaultPayees = listOf(
                Payee(null, puid, "Netflix", "US", "US1234567890", false),
                Payee(null, puid, "Salary", "US", "US0987654321", false),
                Payee(null, puid, "John Doe", "US", "US1122334455", false),
                Payee(null, puid, "Gym", "US", "US6677889900", false),
                Payee(null, puid, "Sponsor", "US", "US5566778899", false),
                Payee(null, puid, "Frederick Schmidt", "DE", "DE35201202001934568467", true),
                Payee(null, puid, "Peter Hendrik", "NL", "NL38RABO5198491756", true),
                Payee(null, puid, "Pat Murphy", "IE", "IE85BOFI900017779245", true)
            )

            // Track WorkRequest IDs for all payees
            val workRequests = defaultPayees.map { payee ->
                OneTimeWorkRequestBuilder<PayeeDatabaseWorker>()
                    .setInputData(payee.toWorkData())
                    .build()
            }

            // Enqueue all work requests
            workRequests.forEach { workManager.enqueue(it) }

            // Wait for all work requests to complete
            workRequests.forEach { workRequest ->
                var workInfo: WorkInfo
                do {
                    workInfo = workManager.getWorkInfoById(workRequest.id).get()
                } while (!workInfo.state.isFinished)

                if (workInfo.state == WorkInfo.State.SUCCEEDED) {
                    Log.d("AuthViewModel", "Payee insertion succeeded for WorkRequest: ${workRequest.id}")
                } else {
                    Log.e("AuthViewModel", "Payee insertion failed for WorkRequest: ${workRequest.id}")
                }
            }
        }
    }

    private suspend fun insertDefaultTransactions(puid: String) {
        withContext(Dispatchers.IO) {
            // Fetch Payee IDs
            val payeeIds = mapOf(
                "Netflix" to payeeDatabaseHelper.getPayeeIdByName("Netflix"),
                "Salary" to payeeDatabaseHelper.getPayeeIdByName("Salary"),
                "John Doe" to payeeDatabaseHelper.getPayeeIdByName("John Doe"),
                "Gym" to payeeDatabaseHelper.getPayeeIdByName("Gym"),
                "Sponsor" to payeeDatabaseHelper.getPayeeIdByName("Sponsor")
            )

            Log.d("AuthViewModel", "Payee IDs fetched: $payeeIds")

            // Define default transactions
            val defaultTransactions = listOf(
                Transaction(null, puid, payeeIds["Netflix"] ?: -1, 25.00, "2024-01-01", "debit"),
                Transaction(null, puid, payeeIds["Salary"] ?: -1, 5000.00, "2024-01-02", "credit"),
                Transaction(null, puid, payeeIds["John Doe"] ?: -1, 500.00, "2024-01-03", "credit"),
                Transaction(null, puid, payeeIds["Gym"] ?: -1, 20.00, "2024-01-03", "debit"),
                Transaction(null, puid, payeeIds["Sponsor"] ?: -1, 70.00, "2024-01-05", "credit")
            )

            defaultTransactions.forEach { transaction ->
                try {
                    transactionDatabaseHelper.insertTransactionInBackground(transaction)
                    Log.d("AuthViewModel", "Transaction inserted: $transaction")
                } catch (e: Exception) {
                    Log.e("AuthViewModel", "Error inserting transaction: $transaction, error: $e")
                }
            }
        }
    }


    // Logout Function
    fun logout() {
        Log.d("AuthViewModel", "Entering logout() in AuthViewModel")
        _puid.value = null // Clear PUID on logout
        _statusMessage.value = null // Reset the status message
        _loggedIn.value = false // Ensure loggedIn is reset
        OutputManager.resetOutput() // reset remoteIP and userAgent
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
