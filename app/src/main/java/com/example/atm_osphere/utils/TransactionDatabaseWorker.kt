package com.example.atm_osphere.utils

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.atm_osphere.model.Transaction

class TransactionDatabaseWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val passphrase = inputData.getString("passphrase")?.toCharArray() ?: return Result.failure()
        val puid = inputData.getString("puid") ?: return Result.failure()
        val name = inputData.getString("name") ?: return Result.failure()
        val type = inputData.getString("type") ?: return Result.failure()
        val amount = inputData.getDouble("amount", -1.0)

        val dbHelper = TransactionDatabaseHelper(applicationContext)

        return try {
            val db = dbHelper.getWritableDatabase(passphrase)
            val transaction = Transaction(puid, name, type, amount)
            dbHelper.insertTransaction(transaction, passphrase.joinToString(""))
            db.close()
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        } finally {
            dbHelper.closeDatabase(null)
        }
    }
}
