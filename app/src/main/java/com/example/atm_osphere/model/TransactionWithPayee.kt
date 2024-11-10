package com.example.atm_osphere.model

data class TransactionWithPayee(
    val transactionId: Int,
    val puid: String,
    val payeeName: String,
    val payeeId: Int,
    val amount: Double,
    val date: String,
    val transactionType: String
)
