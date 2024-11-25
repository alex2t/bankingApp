package com.example.atm_osphere.utils.api

import org.json.JSONObject

object PayloadBuilders {

    fun buildLoginPayload(sessionId:String , puid: String, userAgent: String, remoteIp: String): String {
        val json = JSONObject()
        json.put("customer_session_id", sessionId)
        json.put("permanent_user_id", puid)
        json.put("user_agent", userAgent)
        json.put("remote_addr", remoteIp)
        return json.toString()
    }

    fun buildAddPayeePayload(puid: String, name: String, country: String, iban: String): String {
        val json = JSONObject()
        json.put("puid", puid)
        json.put("name", name)
        json.put("country", country)
        json.put("iban", iban)
        return json.toString()
    }

    fun buildTransactionPayload(puid: String, payeeId: String, amount: Double, type: String): String {
        val json = JSONObject()
        json.put("puid", puid)
        json.put("payeeId", payeeId)
        json.put("amount", amount)
        json.put("type", type)
        return json.toString()
    }
}