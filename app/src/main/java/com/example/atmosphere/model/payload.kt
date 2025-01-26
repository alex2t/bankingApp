package com.example.atmosphere.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class LoginPayload(
    val activity: String,
    val customerSessionId: String,
    val permanentUserId: String,
    val userAgent: String? = null,
    val remoteAddr: String? = null
)
@Serializable
data class AddPayeePayload(
    val activity: String,
    val permanentUserId: String,
    val sessionId: String,
    val payeeData: JsonObject,
    val userAgent: String? = null,
    val remoteIp: String? = null
)

@Serializable
data class TransactionPayload(
    val activity: String,
    val sessionId: String,
    val permanentUserId: String,
    val payeeIban: String,
    val payeeCountry: String,
    val amount: Double,
    val transactionType: String,
    val userAgent: String? = null,
    val remoteIp: String? = null
)