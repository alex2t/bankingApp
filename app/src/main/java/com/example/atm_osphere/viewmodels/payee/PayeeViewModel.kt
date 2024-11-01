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
import android.util.Log

class PayeeViewModel(
    private val databaseHelper: PayeeDatabaseHelper,
    private val passphrase: String
) : ViewModel() {

    private val _iban = MutableStateFlow<String?>(null)
    val iban: StateFlow<String?> get() = _iban

    private val _statusMessage = MutableStateFlow<Pair<String?, Boolean>?>(null)
    val statusMessage: StateFlow<Pair<String?, Boolean>?> get() = _statusMessage

    private val _payees = MutableStateFlow<List<Payee>>(emptyList())
    val payees: StateFlow<List<Payee>> get() = _payees

    fun generateIban(country: String) {
        try {
            _iban.value = generateFakeIban(country)
            Log.d("PayeeViewModel", "IBAN generated successfully for country: $country")
        } catch (e: Exception) {
            Log.e("PayeeViewModel", "Error generating IBAN for country: $country", e)
            _statusMessage.value = "Failed to generate IBAN" to false
        }
    }

    fun addPayee(puid: String, name: String, countryCode: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val payee = Payee(name, countryCode, _iban.value!!)
                val success = databaseHelper.insertPayee(puid, payee, passphrase)
                _statusMessage.update {
                    if (success) {
                        "Payee added successfully" to true
                    } else {
                        "An error occurred: Payee was not added" to false
                    }
                }
            } catch (e: Exception) {
                _statusMessage.update {
                    "An error occurred: ${e.message}" to false
                }
            }
        }
    }

    fun getPayeesByPuid(puid: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val payeesList = databaseHelper.getPayeesByPuid(puid, passphrase)
                _payees.update { payeesList }
                Log.d("PayeeViewModel", "Fetched ${payeesList.size} payees for puid: $puid")
            } catch (e: Exception) {
                Log.e("PayeeViewModel", "Error fetching payees for puid: $puid", e)
                _statusMessage.update { "Failed to fetch payees" to false }
            }
        }
    }

    fun resetStatusMessage() {
        _statusMessage.value = "" to false
    }
}
