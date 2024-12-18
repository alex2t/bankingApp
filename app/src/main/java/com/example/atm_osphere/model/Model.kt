package com.example.atm_osphere.model

data class Payee(
    val payeeId: Int? = null,        // Auto-incremented primary key, nullable for new payees
    val puid: String,                // Foreign key linking to User
    val name: String,
    val country: String,
    val iban: String ,
    val isDefault: Boolean = false// Unique IBAN for each payee
)

data class Transaction(
    val transactionId: Int? = null,     // Auto-incremented primary key, nullable for new transactions
    val puid: String,                   // Foreign key linking to User
    val payeeId: Int,                   // Foreign key linking to Payee
    val amount: Double,                 // Transaction amount
    val date: String,                   // Date in yyyy-mm-dd format
    val transactionType: String         // Type of transaction, e.g., 'debit' or 'credit'
)

data class TransactionWithPayee(
    val transactionId: Int,
    val puid: String,
    val payeeName: String,
    val payeeId: Int,
    val amount: Double,
    val date: String,
    val transactionType: String
)
data class User(
    val permanentUserId: String,
    val email: String,
    val password: String,
    val date: String
)