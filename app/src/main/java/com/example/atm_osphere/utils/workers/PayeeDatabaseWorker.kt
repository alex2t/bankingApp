package com.example.atm_osphere.utils.workers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.atm_osphere.model.Payee
import com.example.atm_osphere.utils.database.PayeeDatabaseHelper
import kotlin.text.toCharArray


class PayeeDatabaseWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val passphrase = inputData.getString("passphrase")?.toCharArray() ?: return Result.failure()
        val puid = inputData.getString("puid") ?: return Result.failure()
        val name = inputData.getString("name") ?: return Result.failure()
        val country = inputData.getString("country") ?: return Result.failure()
        val iban = inputData.getString("iban") ?: return Result.failure()

        val dbHelper = PayeeDatabaseHelper(applicationContext)

        return try {
            val db = dbHelper.getWritableDatabase(passphrase)
            val payee = Payee(name, country, iban)
            val success = dbHelper.insertPayee(puid, payee, passphrase.joinToString(""))
            db.close()
            if (success) Result.success() else Result.failure()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        } finally {
            dbHelper.closeDatabase(null)
        }
    }
}


