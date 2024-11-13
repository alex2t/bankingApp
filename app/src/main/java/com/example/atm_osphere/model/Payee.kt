package com.example.atm_osphere.model

data class Payee(
    val payeeId: Int? = null,        // Auto-incremented primary key, nullable for new payees
    val puid: String,                // Foreign key linking to User
    val name: String,
    val country: String,
    val iban: String ,
    val isDefault: Boolean = false// Unique IBAN for each payee
)