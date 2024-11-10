package com.example.atm_osphere.model

data class Transaction(
    val transactionId: Int? = null,     // Auto-incremented primary key, nullable for new transactions
    val puid: String,                   // Foreign key linking to User
    val payeeId: Int,                   // Foreign key linking to Payee
    val amount: Double,                 // Transaction amount
    val date: String,                   // Date in yyyy-mm-dd format
    val transactionType: String         // Type of transaction, e.g., 'debit' or 'credit'
)
