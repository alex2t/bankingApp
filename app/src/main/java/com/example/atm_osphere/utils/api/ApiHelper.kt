package com.example.atm_osphere.utils.api

import com.example.atm_osphere.model.AddPayeePayload
import kotlinx.serialization.json.Json

class ApiHelper (private val apiFactory: ApiFactory){
    companion object {
        private const val BASE_URL = "https://atmosphere.free.beeceptor.com"

    }

    suspend fun login(sessionId: String, puid: String, userAgent: String, remoteIp: String): String {
        //val endpoint = "login" // Specific endpoint
        val payload = PayloadBuilders.buildLoginPayload(sessionId, puid, userAgent, remoteIp)
        return apiFactory.postRequest(BASE_URL, payload)
    }

    suspend fun addPayee(sessionId: String, payeeData: String, userAgent: String, remoteIp: String): String {
        val payload = AddPayeePayload(
            sessionId = sessionId,
            payeeData = Json.decodeFromString(payeeData),
            userAgent = userAgent,
            remoteIp = remoteIp
        )

        // Convert to JSON
        val jsonPayload = Json.encodeToString(AddPayeePayload.serializer(), payload)
        return apiFactory.postRequest(BASE_URL, jsonPayload)
    }


    suspend fun makeTransaction(puid: String, payeeId: String, amount: Double, type: String): String {
        //val endpoint = "transaction" // Specific endpoint
        val payload = PayloadBuilders.buildTransactionPayload(puid, payeeId, amount, type)
        return apiFactory.postRequest(BASE_URL, payload)
    }
}