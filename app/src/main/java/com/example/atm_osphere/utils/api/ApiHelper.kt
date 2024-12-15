package com.example.atm_osphere.utils.api

import com.example.atm_osphere.model.AddPayeePayload
import com.example.atm_osphere.model.LoginPayload
import com.example.atm_osphere.model.TransactionPayload
import kotlinx.serialization.json.Json

class ApiHelper (private val apiFactory: ApiFactory){
    companion object {
        private const val BASE_URL = "https://atmosphere.free.beeceptor.com"

    }
    suspend fun loginApi(payload: LoginPayload): String {
        val jsonPayload = Json.encodeToString(LoginPayload.serializer(), payload)
        return apiFactory.postRequest(BASE_URL, jsonPayload)
    }

    suspend fun addPayeeApi(payload: AddPayeePayload): String {
        val jsonPayload = Json.encodeToString(AddPayeePayload.serializer(), payload)
        return apiFactory.postRequest(BASE_URL, jsonPayload)
    }

    suspend fun makeTransaction(payload: TransactionPayload): String {
        val jsonPayload = Json.encodeToString(TransactionPayload.serializer(), payload)
        return apiFactory.postRequest(BASE_URL, jsonPayload)
    }
}