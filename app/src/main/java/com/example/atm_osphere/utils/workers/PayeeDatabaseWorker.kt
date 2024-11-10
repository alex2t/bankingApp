package com.example.atm_osphere.utils.workers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.atm_osphere.model.Payee
import com.example.atm_osphere.utils.database.AppDatabaseHelper
import com.example.atm_osphere.utils.database.PayeeDatabaseHelper

class PayeeDatabaseWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val passphrase = inputData.getString("passphrase")?.toCharArray() ?: return Result.failure()
        val puid = inputData.getString("puid") ?: return Result.failure()
        val name = inputData.getString("name") ?: return Result.failure()
        val country = inputData.getString("country") ?: return Result.failure()
        val iban = inputData.getString("iban") ?: return Result.failure()

        // Use AppDatabaseHelper directly to open the database
        val appDatabaseHelper = AppDatabaseHelper(applicationContext)
        val db = appDatabaseHelper.getWritableDatabase(passphrase)

        return try {
            val dbHelper = PayeeDatabaseHelper(applicationContext)
            val payee = Payee(payeeId = null, puid = puid, name = name, country = country, iban = iban) // Create a Payee instance

            // Insert the payee using PayeeDatabaseHelper
            val success = dbHelper.insertPayee(puid, payee, passphrase.joinToString(""))

            if (success) Result.success() else Result.failure()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        } finally {
            db.close()  // Ensure database is closed in the finally block
        }
    }
}
