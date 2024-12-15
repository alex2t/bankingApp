package com.example.atm_osphere.utils.api

import org.json.JSONObject

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

import kotlinx.serialization.encodeToString



object PayloadBuilders {

    fun buildLoginPayload(sessionId:String , puid: String, userAgent: String, remoteIp: String): String {
        val json = JSONObject()
        json.put("customer_session_id", sessionId)
        json.put("permanent_user_id", puid)
        json.put("user_agent", userAgent)
        json.put("remote_addr", remoteIp)
        return json.toString()
    }

    fun buildAddPayeePayload(
        sessionId: String,
        payeeData: Map<String, Any>,
        userAgent: String,
        remoteIp: String
    ): String {
        val payloadMap = mapOf(
            "sessionId" to sessionId,
            "payeeData" to JsonObject(payeeData.mapValues { JsonPrimitive(it.value.toString()) }),
            "userAgent" to userAgent,
            "remoteIp" to remoteIp
        )
        return Json.encodeToString(payloadMap)
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