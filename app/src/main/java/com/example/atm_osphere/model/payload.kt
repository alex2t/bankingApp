package com.example.atm_osphere.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class LoginPayload(
    val activity: String,
    val customerSessionId: String,
    val permanentUserId: String,
    val userAgent: String,
    val remoteAddr: String
)
@Serializable
data class AddPayeePayload(
    val activity: String,
    val permanentUserId: String,
    val sessionId: String,
    val payeeData: JsonObject,
    val userAgent: String,
    val remoteIp: String
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
    val userAgent: String,
    val remoteIp: String
)