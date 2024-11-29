package com.example.atm_osphere.viewmodels.payee

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.atm_osphere.utils.generateFakeIban
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.example.atm_osphere.utils.database.PayeeDatabaseHelper
import com.example.atm_osphere.model.Payee
import kotlinx.coroutines.delay
import android.util.Log
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.atm_osphere.utils.workers.PayeeDatabaseWorker
import com.example.atm_osphere.utils.mapToWorkData
import androidx.work.WorkInfo
import kotlinx.coroutines.withContext
import com.example.atm_osphere.utils.api.ApiHelper
import com.example.atm_osphere.utils.OutputManager
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive


class PayeeViewModel(
    private val databaseHelper: PayeeDatabaseHelper,
    private val workManager: WorkManager,
    private val apiHelper: ApiHelper
) : ViewModel() {

    private val _iban = MutableStateFlow<String?>(null)
    val iban: StateFlow<String?> get() = _iban

    // Separate status messages for IBAN and Add Payee actions
    private val _ibanStatusMessage = MutableStateFlow<Pair<String, Boolean>?>(null)
    val ibanStatusMessage: StateFlow<Pair<String, Boolean>?> get() = _ibanStatusMessage

    private val _addPayeeStatusMessage = MutableStateFlow<Pair<String, Boolean>?>(null)
    val addPayeeStatusMessage: StateFlow<Pair<String, Boolean>?> get() = _addPayeeStatusMessage

    private val _payees = MutableStateFlow<List<Payee>>(emptyList())
    val payees: StateFlow<List<Payee>> get() = _payees

    fun generateIban(country: String) {
        try {
            _iban.value = generateFakeIban(country)
            Log.d("PayeeViewModel", "IBAN generated successfully for country: $country")
            _ibanStatusMessage.value = "IBAN generated successfully" to true // Set success message
        } catch (e: Exception) {
            Log.e("PayeeViewModel", "Error generating IBAN for country: $country", e)
            _ibanStatusMessage.value = "Failed to generate IBAN" to false // Set error message
        }
    }

    fun addPayee(sessionId: String,puid: String, name: String, countryCode: String, iban: String, isDefault: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Prepare input data for the worker
                val payeeData = mapOf(
                    "puid" to puid,
                    "name" to name,
                    "country" to countryCode,
                    "iban" to iban,
                    "isDefault" to isDefault
                )

                val workData = mapToWorkData(payeeData)

                // Create and enqueue a work request for PayeeDatabaseWorker
                val workRequest = OneTimeWorkRequestBuilder<PayeeDatabaseWorker>()
                    .setInputData(workData)
                    .build()

                // Enqueue the work request using the injected WorkManager instance
                workManager.enqueue(workRequest)

                // Switch to the main thread to observe the work result
                withContext(Dispatchers.Main) {
                    workManager.getWorkInfoByIdLiveData(workRequest.id)
                        .observeForever { workInfo ->
                            if (workInfo != null && workInfo.state.isFinished) {
                                if (workInfo.state == WorkInfo.State.SUCCEEDED) {
                                    _addPayeeStatusMessage.value = "Payee added successfully" to true
                                    viewModelScope.launch(Dispatchers.IO) {
                                        apiAddPayee(payeeData, sessionId)
                                    }
                                } else {
                                    val errorMessage = workInfo.outputData.getString("error_message")
                                        ?: "An error occurred: Payee was not added"
                                    _addPayeeStatusMessage.value = errorMessage to false
                                }

                                // Clear the status message after a short delay
                                viewModelScope.launch {
                                    delay(2000)
                                    _addPayeeStatusMessage.value = null
                                }
                            }
                        }
                }
            } catch (e: Exception) {
                _addPayeeStatusMessage.value = "An error occurred: ${e.message}" to false
            }
        }
    }


    private suspend fun apiAddPayee(payeeData: Map<String, Any>, sessionId: String) {
        val (userAgent, remoteIp) = OutputManager.getUserAgentAndRemoteIp()

        try {
            // Use kotlinx.serialization to convert payeeData
            val payeePayload = JsonObject(payeeData.mapValues { JsonPrimitive(it.value.toString()) })

            val response = apiHelper.addPayee(
                sessionId = sessionId,
                payeeData = payeePayload.toString(),
                userAgent = userAgent,
                remoteIp = remoteIp
            )
            Log.d("PayeeViewModel", "Response: $response")
        } catch (e: Exception) {
            Log.e("PayeeViewModel", "Error calling addPayee API: ${e.message}")
        }
    }




    fun resetIbanStatusMessage() {
        _ibanStatusMessage.value = null
    }

    fun resetAddPayeeStatusMessage() {
        _addPayeeStatusMessage.value = null
    }

    fun getPayeesByPuid(puid: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val payeesList = databaseHelper.getPayeesByPuid(puid)
                _payees.update { payeesList }
                Log.d("PayeeViewModel", "Fetched ${payeesList.size} payees for puid: $puid")
            } catch (e: Exception) {
                Log.e("PayeeViewModel", "Error fetching payees for puid: $puid", e)
                _addPayeeStatusMessage.value = "Failed to fetch payees" to false
            }
        }
    }

    fun clearIban() {
        _iban.value = null
    }

    fun resetFields() {
        _iban.value = null
        _addPayeeStatusMessage.value = null
    }
}

