package com.example.atm_osphere.utils

import android.content.Context
import android.database.Cursor
import androidx.work.WorkManager
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SQLiteOpenHelper
import com.example.atm_osphere.model.Transaction
import android.util.Log

class TransactionDatabaseHelper(private val context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "transaction_database.db"
        private const val DATABASE_VERSION = 1
    }

    init {
        SQLiteDatabase.loadLibs(context) // Load SQLCipher libraries
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTableSQL = """
            CREATE TABLE transactions (
                puid TEXT,
                name TEXT,
                type TEXT,
                amount REAL
            )
        """.trimIndent()
        db?.execSQL(createTableSQL)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS transactions")
        onCreate(db)
    }

    // Retrieve transactions for a given puid and insert default if none found
    fun getTransactionsByPuid(puid: String, passphrase: String): List<Transaction> {
        val db = this.getReadableDatabase(passphrase.toCharArray())
        var cursor: Cursor? = null
        val transactions = mutableListOf<Transaction>()

        try {
            // Query transactions by puid
            cursor = db.query(
                "transactions", null,
                "puid=?", arrayOf(puid),
                null, null, null
            )

            // Iterate through the cursor and build a list of transactions
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    val nameIndex = cursor.getColumnIndex("name")
                    val typeIndex = cursor.getColumnIndex("type")
                    val amountIndex = cursor.getColumnIndex("amount")

                    // Ensure that column indices are valid
                    val name = cursor.getString(nameIndex)
                    val type = cursor.getString(typeIndex)
                    val amount = cursor.getDouble(amountIndex)

                    transactions.add(Transaction(puid, name, type, amount))
                } while (cursor.moveToNext())
                Log.d("TransactionDatabaseHelper", "Found ${transactions.size} transactions for puid: $puid")

            }

            // If no transactions exist for this puid, insert default transactions
            if (transactions.isEmpty()) {
                Log.d("TransactionDatabaseHelper", "transactions is Empty for puid: $puid")
                val defaultTransactions = listOf(
                    Transaction(puid, "Netflix", "debit", 25.00),
                    Transaction(puid, "Salary", "credit", 5000.00),
                    Transaction(puid, "John Doe", "credit", 500.00),
                    Transaction(puid, "Gym", "debit", 20.00),
                    Transaction(puid, "Sponsor", "credit", 70.00)
                )
                // Insert default transactions and add them to the list
                defaultTransactions.forEach { insertTransaction(it, passphrase) }
                transactions.addAll(defaultTransactions) // Add defaults to the returned list
                Log.d("TransactionDatabaseHelper", "Inserted ${transactions.size} transactions for puid: $puid")
            }

        } catch (e: Exception) {
            e.printStackTrace() // Log any potential errors
        } finally {
            cursor?.close()  // Safely close the cursor
            db.close()  // Safely close the database
        }

        return transactions // Return the list of transactions
    }

    // Insert a transaction into the database
    fun insertTransaction(transaction: Transaction, passphrase: String) {
        val db = this.getWritableDatabase(passphrase.toCharArray())
        val contentValues = android.content.ContentValues().apply {
            put("puid", transaction.puid)
            put("name", transaction.name)
            put("type", transaction.type)
            put("amount", transaction.amount)
        }
        db.insert("transactions", null, contentValues)  // Insert the transaction
        db.close()  // Close the database after insertion
    }

    // Background transaction insertion using WorkManager
    fun insertTransactionInBackground(transaction: Transaction, passphrase: String) {
        val inputData = androidx.work.Data.Builder()
            .putString("puid", transaction.puid)
            .putString("name", transaction.name)
            .putString("type", transaction.type)
            .putDouble("amount", transaction.amount)
            .putString("passphrase", passphrase)
            .build()

        val workRequest = androidx.work.OneTimeWorkRequestBuilder<TransactionDatabaseWorker>()
            .setInputData(inputData)
            .build()

        WorkManager.getInstance(context).enqueue(workRequest)  // Enqueue the background task
    }
    fun insertDefaultTransactionsForPuid(puid: String, passphrase: String) {
        val defaultTransactions = listOf(
            Transaction(puid, "Netflix", "debit", 25.00),
            Transaction(puid, "Salary", "credit", 5000.00),
            Transaction(puid, "John Doe", "credit", 500.00),
            Transaction(puid, "Gym", "debit", 20.00),
            Transaction(puid, "Sponsor", "credit", 70.00)
        )

        val db = this.getWritableDatabase(passphrase.toCharArray())
        db.beginTransaction()
        try {
            for (transaction in defaultTransactions) {
                val contentValues = android.content.ContentValues().apply {
                    put("puid", transaction.puid)
                    put("name", transaction.name)
                    put("type", transaction.type)
                    put("amount", transaction.amount)
                }
                db.insert("transactions", null, contentValues)
            }
            db.setTransactionSuccessful()
            Log.d("TransactionDatabaseHelper", "Inserted ${defaultTransactions.size} transactions for puid: $puid")
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            db.endTransaction()
            db.close()
        }
    }



    // Safely close the database if open
    fun closeDatabase(db: SQLiteDatabase?) {
        db?.let {
            if (it.isOpen) {
                it.close()
            }
        }
    }
}
