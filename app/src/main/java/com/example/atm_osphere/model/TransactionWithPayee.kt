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
//{
//    "amount": 35543.7, done
//    "currency_code": "GBP",
//    "target_country_code": "GB",
//    "target_bank_identifier": "abcd1234abcd1234a", done
//    "target_account_identifier": 1234567890123456,
//    "is_new_payee": 1,
//    "transaction_datetime": "2019-02-07 07:52:31",
//    "is_bill_payment": 0,
//    "is_recurring": 0,
//    "transaction_id": "abcdef1234567890abcdef1234567890"
//},
