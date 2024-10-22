package com.example.atm_osphere.model

data class Transaction(
        val puid: String,
        val name: String,
        val type: String, // "debit" or "credit"
        val amount: Double
    )
