package com.example.atm_osphere.utils.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.util.Log
import com.example.atm_osphere.model.Payee
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SQLiteOpenHelper

class PayeeDatabaseHelper(private val context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "payee_database.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_NAME = "payees"
    }

    init {
        // Load SQLCipher libraries within the initializer
        SQLiteDatabase.loadLibs(context)
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Create a fixed payee table structure
        val createTableSQL = """
            CREATE TABLE IF NOT EXISTS $TABLE_NAME (
                puid TEXT,
                name TEXT,
                country TEXT,
                iban TEXT
            )
        """.trimIndent()

        // Execute the SQL command to create the table
        db.execSQL(createTableSQL)
        Log.d("PayeeDatabaseHelper", "Database table created successfully.")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Drop the table if it exists and recreate it
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
        Log.d("PayeeDatabaseHelper", "Database upgraded from version $oldVersion to $newVersion.")
    }

    // Retrieve payees for a specific PUID, creating the table if it doesn't exist
    fun getPayeesByPuid(puid: String, passphrase: String): List<Payee> {
        val db = this.getReadableDatabase(passphrase.toCharArray())
        val payees = mutableListOf<Payee>()
        var cursor: Cursor? = null

        try {
            // Query the table for payees matching the PUID
            cursor = db.query(TABLE_NAME, null, "puid=?", arrayOf(puid), null, null, null)
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
                    val country = cursor.getString(cursor.getColumnIndexOrThrow("country"))
                    val iban = cursor.getString(cursor.getColumnIndexOrThrow("iban"))
                    payees.add(Payee(name, country, iban))
                } while (cursor.moveToNext())
                Log.d("PayeeDatabaseHelper", "Found ${payees.size} payees for puid: $puid")
            }

            // Insert default payees if no records exist for this PUID
            if (payees.isEmpty()) {
                Log.d("PayeeDatabaseHelper", "No payees found for puid: $puid, inserting default payees.")
                insertDefaultPayees(db, puid)
                payees.addAll(getDefaultPayees(puid))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor?.close()
            db.close()
        }

        return payees
    }

    // Insert a payee for a specific PUID
    fun insertPayee(puid: String, payee: Payee, passphrase: String): Boolean {
        val db = this.getWritableDatabase(passphrase.toCharArray())

        val values = ContentValues().apply {
            put("puid", puid)
            put("name", payee.name)
            put("country", payee.country)
            put("iban", payee.iban)
        }

        val success = db.insert(TABLE_NAME, null, values) != -1L
        db.close()
        return success
    }

    // Insert default payees if none exist for a PUID
    private fun insertDefaultPayees(db: SQLiteDatabase, puid: String) {
        val defaultPayees = getDefaultPayees(puid)
        db.beginTransaction()
        try {
            for (payee in defaultPayees) {
                val values = ContentValues().apply {
                    put("puid", puid)
                    put("name", payee.name)
                    put("country", payee.country)
                    put("iban", payee.iban)
                }
                db.insert(TABLE_NAME, null, values)
            }
            db.setTransactionSuccessful()
            Log.d("PayeeDatabaseHelper", "Inserted ${defaultPayees.size} default payees for puid: $puid")
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            db.endTransaction()
        }
    }

    // Default payees to be inserted when no records exist for a PUID
    private fun getDefaultPayees(puid: String): List<Payee> {
        return listOf(
            Payee("Frederick Schmidt", "DE", "DE35201202001934568467"),
            Payee("Peter Hendrik", "NL", "NL38RABO5198491756"),
            Payee("Pat Murphy", "IE", "IE85BOFI900017779245")
        )
    }

    // Close the database if it's open
    fun closeDatabase(db: SQLiteDatabase?) {
        db?.close()
    }
}
