package com.example.atm_osphere.utils.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.util.Log
import com.example.atm_osphere.model.Payee
import net.sqlcipher.database.SQLiteDatabase
import com.example.atm_osphere.utils.database.AppDatabaseHelper
import net.sqlcipher.database.SQLiteOpenHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
                "Payee",
                null,
                "puid = ? AND IsDefault = ?" ,
                arrayOf(puid,"1",),
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
    fun insertPayee(puid: String, payee: Payee, passphrase: String): Boolean {
        val db = appDatabaseHelper.getWritableDatabase(passphrase.toCharArray())
        return try {
            // Prepare content values for insertion
            val values = ContentValues().apply {
                put("puid", puid)                // Link payee to user by puid
                put("name", payee.name)           // Payee's name
                put("country", payee.country)     // Payee's country
                put("iban", payee.iban)           // Payee's IBAN
                put("isDefault", if (payee.isDefault) 1 else 0)
            }


            // Insert the values into the payees table and check for success
            val success = db.insert("Payee", null, values) != -1L
            if (success) {
                Log.d("PayeeDatabaseHelper", "Payee inserted successfully: ${payee.name}")
            } else {
                Log.e("PayeeDatabaseHelper", "Failed to insert payee: ${payee.name}")
            }
            success
        } catch (e: Exception) {
            Log.e("PayeeDatabaseHelper", "Error inserting payee: ${payee.name}", e)
            false  // Return false if there was an exception
        } finally {
            db.close()  // Close the database connection in the finally block
        }
    }




    // function call from the authviewModel
    suspend fun getPayeeIdByName(name: String, passphrase: String): Int? = withContext(Dispatchers.IO) {
        Log.d("PayeeDatabaseHelper", "getPayeeIdByName called with name: $name")
        var payeeId: Int? = null

        try {
            val db = appDatabaseHelper.getReadableDatabase(passphrase.toCharArray())
            val cursor = db.rawQuery("SELECT payeeId FROM Payee WHERE name = ?", arrayOf(name))

            cursor.use {
                if (it.moveToFirst()) {
                    payeeId = it.getInt(0)
                    Log.d("PayeeDatabaseHelper", "Payee ID found: $payeeId")
                } else {
                    Log.d("PayeeDatabaseHelper", "No Payee found with name: $name")
                }
            }
        } catch (e: Exception) {
            Log.e("PayeeDatabaseHelper", "Error querying Payee ID by name: $name", e)
        }

        payeeId
    }



    // Close the database if it's open
    fun closeDatabase(db: SQLiteDatabase?) {
        db?.close()
    }
}
