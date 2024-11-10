package com.example.atm_osphere.utils.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.util.Log
import com.example.atm_osphere.model.Payee
import net.sqlcipher.database.SQLiteDatabase
import com.example.atm_osphere.utils.database.AppDatabaseHelper
import net.sqlcipher.database.SQLiteOpenHelper
class PayeeDatabaseHelper(private val context: Context)  {




    private val appDatabaseHelper = AppDatabaseHelper(context)

    // Retrieve payees for a specific PUID, creating the table if it doesn't exist
    fun getPayeesByPuid(puid: String, passphrase: String): List<Payee> {
        val db = appDatabaseHelper.getReadableDatabase(passphrase.toCharArray())
        val payees = mutableListOf<Payee>()
        var cursor: Cursor? = null

        try {
            // Query the table for payees matching the PUID
            cursor = db.query(
                "payees",
                null,
                "puid = ?",
                arrayOf(puid),
                null,
                null,
                null
            )

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    val payeeId = cursor.getInt(cursor.getColumnIndexOrThrow("payee_id"))
                    val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
                    val country = cursor.getString(cursor.getColumnIndexOrThrow("country"))
                    val iban = cursor.getString(cursor.getColumnIndexOrThrow("iban"))
                    payees.add(Payee(payeeId, puid, name, country, iban))
                } while (cursor.moveToNext())
                Log.d("PayeeDatabaseHelper", "Found ${payees.size} payees for puid: $puid")
            }

            // If no records were found, insert default payees
            if (payees.isEmpty()) {
                Log.d("PayeeDatabaseHelper", "No payees found for puid: $puid, inserting default payees.")
                insertDefaultPayees(puid, db)
                payees.addAll(getDefaultPayees(puid))
            }
        } catch (e: Exception) {
            Log.e("PayeeDatabaseHelper", "Error retrieving payees: ${e.localizedMessage}")
        } finally {
            cursor?.close()
            db.close()
        }

        return payees
    }


    // Insert a payee for a specific PUID
    fun insertPayee(puid: String, payee: Payee, passphrase: String): Boolean {
        val db = appDatabaseHelper.getWritableDatabase(passphrase.toCharArray())

        // Prepare content values for insertion
        val values = ContentValues().apply {
            put("puid", puid)                // Link payee to user by puid
            put("name", payee.name)           // Payee's name
            put("country", payee.country)     // Payee's country
            put("iban", payee.iban)           // Payee's IBAN
        }

        // Insert the values into the payees table and check for success
        val success = db.insert("payees", null, values) != -1L
        db.close() // Close the database connection after insertion
        return success
    }

    private fun insertDefaultPayees(puid: String, db: SQLiteDatabase) {
        val defaultPayees = listOf(
            Payee(0, puid, "Frederick Schmidt", "DE", "DE35201202001934568467"),
            Payee(0, puid, "Peter Hendrik", "NL", "NL38RABO5198491756"),
            Payee(0, puid, "Pat Murphy", "IE", "IE85BOFI900017779245")
        )

        for (payee in defaultPayees) {
            val values = ContentValues().apply {
                put("puid", puid)
                put("name", payee.name)
                put("country", payee.country)
                put("iban", payee.iban)
            }
            db.insert("payees", null, values)
        }
    }

    private fun getDefaultPayees(puid: String): List<Payee> {
        return listOf(
            Payee(0, puid, "Frederick Schmidt", "DE", "DE35201202001934568467"),
            Payee(0, puid, "Peter Hendrik", "NL", "NL38RABO5198491756"),
            Payee(0, puid, "Pat Murphy", "IE", "IE85BOFI900017779245")
        )
    }


    // Close the database if it's open
    fun closeDatabase(db: SQLiteDatabase?) {
        db?.close()
    }
}
