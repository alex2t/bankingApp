package com.example.atm_osphere.utils.workers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.atm_osphere.model.User
import com.example.atm_osphere.utils.database.AppDatabaseHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class UserDatabaseWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {
        // Retrieve input data (e.g., user details and )
        val userId = inputData.getString("userId") ?: return Result.failure()
        val email = inputData.getString("email") ?: return Result.failure()
        val password = inputData.getString("password") ?: return Result.failure()


        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val user = User(userId, email, password, date = currentDate)

        // Use AppDatabaseHelper to interact with the unified database
        val dbHelper = AppDatabaseHelper(applicationContext)
        val db = dbHelper.writableDatabase

        // Insert the user into the users table
        val insertSQL = "INSERT INTO users (permanent_user_id, email, password, date) VALUES (?, ?, ?, ?)"
        val statement = db.compileStatement(insertSQL)

        statement.bindString(1, user.permanentUserId)
        statement.bindString(2, user.email)
        statement.bindString(3, user.password)
        statement.bindString(4, user.date) // Include the date field

        // Execute insertion and handle any exceptions
        return try {
            statement.executeInsert()
            Result.success() // Return success if the operation completes
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure() // Return failure in case of an exception
        } finally {
            db.close() // Close the database connection
        }
    }
}
