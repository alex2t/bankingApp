package com.example.atm_osphere.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class LoginPayload(
    val customerSessionId: String,
    val permanentUserId: String,
    val userAgent: String,
    val remoteAddr: String
)
@Serializable
data class AddPayeePayload(
    val sessionId: String,
    val payeeData: JsonObject,
    val userAgent: String,
    val remoteIp: String
)

@Serializable
data class TransactionPayload(
    val sessionId: String,
    val permanentUserId: String,
    val payeeIban: String,
    val payeeCountry: String,
    val amount: Double,
    val transactionType: String,
    val userAgent: String,
    val remoteIp: String
)