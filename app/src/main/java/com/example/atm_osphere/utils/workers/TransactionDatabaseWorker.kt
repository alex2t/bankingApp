package com.example.atm_osphere.utils.workers

import android.content.ContentValues
import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.atm_osphere.utils.database.AppDatabaseHelper

class TransactionDatabaseWorker (context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    override fun doWork(): Result {


        val dbHelper = AppDatabaseHelper(applicationContext)
        val db = dbHelper.writableDb

        try {
            val contentValues = ContentValues().apply {
                put("puid", inputData.getString("puid"))
                put("payee_id", inputData.getInt("payee_id", -1))
                put("amount", inputData.getDouble("amount", 0.0))
                put("date", inputData.getString("date"))
                put("transaction_type", inputData.getString("transaction_type"))
            }
            db.insert("transactions", null, contentValues)
        } catch (e: Exception) {
            e.printStackTrace()
            return Result.failure()
        } finally {
            db.close()
        }

        return Result.success()
    }
}
