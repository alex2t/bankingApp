package com.example.atm_osphere.utils.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.util.Log
import com.example.atm_osphere.model.Payee
import android.database.sqlite.SQLiteDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PayeeDatabaseHelper( context: Context)  {

    private val appDatabaseHelper = AppDatabaseHelper(context)

    // Retrieve payees for a specific PUID, creating the table if it doesn't exist
    fun getPayeesByPuid(puid: String): List<Payee> {
        val db = appDatabaseHelper.readableDb
        val payees = mutableListOf<Payee>()
        var cursor: Cursor? = null

        try {
            // Query the table for payees matching the PUID
            cursor = db.query(
                "Payee",
                null,
                "puid = ? AND IsDefault = ?" ,
                arrayOf(puid,"1"),
                null,
                null,
                null
            )

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    val payeeId = cursor.getInt(cursor.getColumnIndexOrThrow("payeeId"))
                    val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
                    val country = cursor.getString(cursor.getColumnIndexOrThrow("country"))
                    val iban = cursor.getString(cursor.getColumnIndexOrThrow("iban"))
                    payees.add(Payee(payeeId, puid, name, country, iban))
                } while (cursor.moveToNext())
                Log.d("PayeeDatabaseHelper", "Found ${payees.size} payees for puid: $puid")
            }

        } catch (e: Exception) {
            Log.e("PayeeDatabaseHelper", "Error retrieving payees: ${e.localizedMessage}")
        } finally {
            cursor?.close()
            db.close()
        }

        return payees
    }


    // Insert default payee from authviewmodel and from AddPayee via it viewModel and worker
    fun insertPayee(puid: String, payee: Payee): Boolean {
        val db = appDatabaseHelper.writableDb
        return try {
            val values = ContentValues().apply {
                put("puid", puid)
                put("name", payee.name)
                put("country", payee.country)
                put("iban", payee.iban)
                put("isDefault", if (payee.isDefault) 1 else 0)
            }
            Log.d("PayeeDatabaseHelper", "Insert called with: puid=${payee.puid}, name=${payee.name}, iban=${payee.iban} isDefault=${payee.isDefault}")

            val rowId = db.insert("Payee", null, values) // Directly get row ID
            if (rowId == -1L) {
                throw Exception("Insert failed for payee: ${values.getAsString("name")}")
            }

            Log.d("Database", "Insert successful for payee: ${values.getAsString("name")}, Row ID: $rowId")
            true // Return true on success
        } catch (e: Exception) {
            Log.e("Database", "Error during insert operation", e)
            false // Return false on failure
        } finally {
            db.close() // Always close the database
        }
    }


    // function call from the authviewModel
    suspend fun getPayeeIdByName(name: String): Int? = withContext(Dispatchers.IO) {
        Log.d("getPayeeIdByName", "getPayeeIdByName called with name: $name")
        var payeeId: Int? = null

        try {
            val db = appDatabaseHelper.readableDb
            val cursor = db.rawQuery("SELECT payeeId FROM Payee WHERE name = ?", arrayOf(name))

            cursor.use {
                if (it.moveToFirst()) {
                    payeeId = it.getInt(0)
                    Log.d("getPayeeIdByName", "Payee ID found: $payeeId")
                } else {
                    Log.d("getPayeeIdByName", "No Payee found with name: $name")
                }
            }
        } catch (e: Exception) {
            Log.e("getPayeeIdByName", "Error querying Payee ID by name: $name", e)
        }

        payeeId
    }
    suspend fun deletePayee(puid: String, payee: Payee): Boolean = withContext(Dispatchers.IO) {
        Log.d("deletePayee", "deletePayee called for puid: $puid and payee: ${payee.name}")
        var isDeleted = false

        try {
            val db = appDatabaseHelper.writableDb
            val rowsAffected = db.delete(
                "Payee",
                "puid = ? AND name = ? AND country = ? AND iban = ?",
                arrayOf(puid, payee.name, payee.country, payee.iban)
            )

            isDeleted = rowsAffected > 0
            Log.d("deletePayee", if (isDeleted) "Payee deleted successfully" else "Payee deletion failed")
        } catch (e: Exception) {
            Log.e("deletePayee", "Error deleting payee: ${payee.name}", e)
        }

        isDeleted
    }



    // Close the database if it's open
    fun closeDatabase(db: SQLiteDatabase?) {
        db?.close()
    }
}
