package com.example.atm_osphere.utils.workers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.atm_osphere.model.User
import com.example.atm_osphere.utils.database.UserDatabaseHelper

class UserDatabaseWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {
        // Retrieve input data (e.g., user details and passphrase)
        val userId = inputData.getString("userId") ?: return Result.failure()
        val email = inputData.getString("email") ?: return Result.failure()
        val password = inputData.getString("password") ?: return Result.failure()
        val passphrase = inputData.getString("passphrase") ?: return Result.failure()

        // Create User object
        val user = User(userId, email, password)

        // Perform the database insertion directly inside this worker
        val dbHelper = UserDatabaseHelper(applicationContext)
        val db = dbHelper.getWritableDatabase(passphrase.toCharArray())

        val insertSQL = "INSERT INTO users (permanent_user_id, email, password) VALUES (?, ?, ?)"
        val statement = db.compileStatement(insertSQL)

        statement.bindString(1, user.permanentUserId)
        statement.bindString(2, user.email)
        statement.bindString(3, user.password)

        statement.executeInsert()

        db.close() // Close database connection

        return Result.success() // Return success if operation completed
    }
}


