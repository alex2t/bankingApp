package com.example.atm_osphere.utils.workers

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.atm_osphere.model.Payee
import com.example.atm_osphere.utils.database.AppDatabaseHelper
import com.example.atm_osphere.utils.database.PayeeDatabaseHelper

class PayeeDatabaseWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    override fun doWork(): Result {
        Log.d("TestWorker", "TestWorker is running")
        val puid = inputData.getString("puid") ?: return Result.failure()
        val name = inputData.getString("name") ?: return Result.failure()
        val country = inputData.getString("country") ?: return Result.failure()
        val iban = inputData.getString("iban") ?: return Result.failure()
        val isDefault = inputData.getInt("isDefault", 0) == 1
        Log.d("Payeeworker", ", puid: $puid , name: $name ")

        // Use AppDatabaseHelper directly to open the database
        val appDatabaseHelper = AppDatabaseHelper(applicationContext)
        val db = appDatabaseHelper.writableDb

        return try {
            val dbHelper = PayeeDatabaseHelper(applicationContext)
            val payee = Payee(payeeId = null, puid = puid, name = name, country = country, iban = iban, isDefault = isDefault) // Create a Payee instance
            Log.d("PayeeWorker", "Data.Builder: puid=$puid, name=${name} country = ${country}, iban = ${iban}, isDefault = $isDefault")
            Log.d("PayeeWorker", "addPayee: ,${isDefault}")
            // Insert the payee using PayeeDatabaseHelper
            val success = dbHelper.insertPayee(puid, payee)

            if (success) Result.success() else Result.failure()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        } finally {
            db.close()  // Ensure database is closed in the finally block
        }
    }
}
