package com.example.atm_osphere.utils

import kotlin.random.Random
import java.math.BigInteger


fun generateFakeIban(country: String): String {
    val countryCode = getCountryCode(country) ?: throw IllegalArgumentException("Unsupported country")
    val length = getIbanLength(countryCode) ?: throw IllegalArgumentException("Invalid country code")

    // Generate random bank and account number based on country-specific length
    val bankAndAccountNumberLength = length - countryCode.length - 2 // Subtract 2 for check digits
    val randomString = (1..bankAndAccountNumberLength).joinToString("") { Random.nextInt(0, 10).toString() }

    // Calculate check digits
    val checkDigits = calculateCheckDigits(countryCode, randomString)

    return "$countryCode$checkDigits$randomString"
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
    val numericIban = rearranged.map {
        if (it.isDigit()) it.toString() else (it.code - 55).toString()
    }.joinToString("")

    // Use BigInteger to avoid overflow
    val mod = BigInteger(numericIban).mod(BigInteger("97"))
    val checkDigits = (98 - mod.toInt()).toString().padStart(2, '0')
    return checkDigits
}
