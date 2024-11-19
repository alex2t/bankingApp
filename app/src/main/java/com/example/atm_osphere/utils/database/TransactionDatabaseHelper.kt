package com.example.atm_osphere.utils.database

import android.content.Context
import android.database.Cursor
import androidx.work.WorkManager
import android.database.sqlite.SQLiteDatabase
import com.example.atm_osphere.model.Transaction
import android.util.Log
import androidx.work.Data
import com.example.atm_osphere.model.TransactionWithPayee
import androidx.work.OneTimeWorkRequestBuilder
import com.example.atm_osphere.utils.workers.TransactionDatabaseWorker
class TransactionDatabaseHelper(private val context: Context)  {
    private val appDatabaseHelper = AppDatabaseHelper(context)

    fun getTransactionsWithPayeeName(puid: String): List<TransactionWithPayee> {
        val db = appDatabaseHelper.readableDb
        val transactionsWithPayees = mutableListOf<TransactionWithPayee>()

        val query = """
        SELECT  transactions.transaction_id, 
                transactions.payee_id, 
                transactions.amount, 
                transactions.date, 
                transactions.transaction_type,
                Payee.name AS payee_name
                FROM transactions
                INNER JOIN Payee ON transactions.payee_id = Payee.payeeId
                WHERE transactions.puid = ? 
                ORDER BY transactions.date DESC
    """.trimIndent()


        Log.d("DatabaseQuery", "Executing query: $query")

        var cursor: Cursor? = null
        try {
            cursor = db.rawQuery(query, arrayOf(puid))

            if (cursor.moveToFirst()) {
                do {
                    // Retrieve column indices, only if they exist
                    val transactionIdIndex = cursor.getColumnIndex("transaction_id").takeIf { it >= 0 }
                    val payeeNameIndex = cursor.getColumnIndex("payee_name").takeIf { it >= 0 }
                    val payeeIdIndex = cursor.getColumnIndex("payee_id").takeIf { it >= 0 }
                    val amountIndex = cursor.getColumnIndex("amount").takeIf { it >= 0 }
                    val dateIndex = cursor.getColumnIndex("date").takeIf { it >= 0 }
                    val transactionTypeIndex = cursor.getColumnIndex("transaction_type").takeIf { it >= 0 }

                    // Retrieve values only if the column indices are valid (i.e., not -1)
                    val transactionId = transactionIdIndex?.let { cursor.getInt(it) }
                    val payeeName = payeeNameIndex?.let { cursor.getString(it) }
                    val payeeId = payeeIdIndex?.let { cursor.getInt(it) }
                    val amount = amountIndex?.let { cursor.getDouble(it) }
                    val date = dateIndex?.let { cursor.getString(it) }
                    val transactionType = transactionTypeIndex?.let { cursor.getString(it) }

                    // Only add the transaction if all necessary fields are non-null
                    if (transactionId != null && payeeName != null && payeeId != null && amount != null && date != null && transactionType != null) {

                        transactionsWithPayees.add(
                            TransactionWithPayee(
                                transactionId = transactionId,
                                puid = puid,
                                payeeName = payeeName,
                                payeeId = payeeId,
                                amount = amount,
                                date = date,
                                transactionType = transactionType
                            )
                        )
                    } else {
                        Log.e("TransactionDatabaseHelper", "Missing fields for transaction with puid: $puid")
                    }
                } while (cursor.moveToNext())
            }
        } catch (e: Exception) {
            Log.e("TransactionDatabaseHelper", "error", e)
            e.printStackTrace()
        } finally {
            cursor?.close()
            db.close()
        }
        return transactionsWithPayees
    }

    // Background transaction insertion using WorkManager
    fun insertTransactionInBackground(transaction: Transaction) {
        val inputData = Data.Builder()
            .putString("puid", transaction.puid)
            .putInt("payee_id", transaction.payeeId)
            .putDouble("amount", transaction.amount)
            .putString("date", transaction.date)
            .putString("transaction_type", transaction.transactionType)
            .build()
        Log.d("insertTransactionInBackground", "puid:${transaction.puid} payee_id:${transaction.payeeId} amount:${transaction.amount} date:${transaction.date}  transaction_type:${transaction.transactionType}")
        val workRequest = OneTimeWorkRequestBuilder<TransactionDatabaseWorker>()
            .setInputData(inputData)
            .build()

        WorkManager.getInstance(context).enqueue(workRequest)
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
