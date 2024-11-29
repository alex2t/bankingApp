package com.example.atm_osphere.model

import kotlinx.serialization.Serializable

@Serializable
data class PayeeData(
    val puid: String,
    val name: String,
    val country: String,
    val iban: String,
    val isDefault: Boolean
)

@Serializable
data class AddPayeePayload(
    val sessionId: String,
    val payeeData: PayeeData,
    val userAgent: String,
    val remoteIp: String
)