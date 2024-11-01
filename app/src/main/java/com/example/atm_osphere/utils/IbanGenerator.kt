package com.example.atm_osphere.utils

import kotlin.random.Random


fun generateFakeIban(country: String, bankAndAccountNumberLength: Int = 10): String {
    val countryCode = getCountryCode(country) ?: throw IllegalArgumentException("Unsupported country")
    val length = getIbanLength(countryCode) ?: throw IllegalArgumentException("Invalid country code")

    // Generate random bank and account number
    val bankAndAccount = (1..bankAndAccountNumberLength).joinToString("") { Random.nextInt(0, 10).toString() }

    // Calculate check digits
    val checkDigits = calculateCheckDigits(countryCode, bankAndAccount)

    return "$countryCode$checkDigits$bankAndAccount"
}

private fun getCountryCode(country: String): String? {
    val map = mapOf(
        "ISRAEL" to "IL",
        "UK" to "GB",
        "FRANCE" to "FR",
        "SPAIN" to "ES",
        "USA" to "US",
        "JAPAN" to "JP"
    )
    return map[country.uppercase()]
}

private fun getIbanLength(countryCode: String): Int? {
    val map = mapOf(
        "IL" to 23,
        "GB" to 22,
        "FR" to 27,
        "ES" to 24,
        "US" to 24, // for testing purposes
        "JP" to 24  // for testing purposes
    )
    return map[countryCode]
}

fun calculateCheckDigits(countryCode: String, bankAndAccount: String): String {
    val rearranged = bankAndAccount + countryCode + "00"
    val numericIban = rearranged.map { it.toInt() - if (Character.isDigit(it)) 0 else 55 }.joinToString("")

    val mod = numericIban.toInt() % 97
    val checkDigits = (98 - mod).toString().padStart(2, '0')
    return checkDigits
}