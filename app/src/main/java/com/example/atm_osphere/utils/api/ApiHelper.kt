package com.example.atm_osphere.utils.api

class ApiHelper (private val apiFactory: ApiFactory){
    companion object {
        private const val BASE_URL = "https://atmosphere.free.beeceptor.com"

    }

    suspend fun login(sessionId: String, puid: String, userAgent: String, remoteIp: String): String {
        //val endpoint = "login" // Specific endpoint
        val payload = PayloadBuilders.buildLoginPayload(sessionId, puid, userAgent, remoteIp)
        return apiFactory.postRequest(BASE_URL, payload)
    }

    suspend fun addPayee(puid: String, name: String, country: String, iban: String): String {
        //val endpoint = "addPayee" // Specific endpoint
        val payload = PayloadBuilders.buildAddPayeePayload(puid, name, country, iban)
        return apiFactory.postRequest(BASE_URL, payload)
    }

    suspend fun makeTransaction(puid: String, payeeId: String, amount: Double, type: String): String {
        //val endpoint = "transaction" // Specific endpoint
        val payload = PayloadBuilders.buildTransactionPayload(puid, payeeId, amount, type)
        return apiFactory.postRequest(BASE_URL, payload)
    }
}