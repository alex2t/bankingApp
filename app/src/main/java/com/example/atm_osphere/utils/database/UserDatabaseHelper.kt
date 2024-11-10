package com.example.atm_osphere.utils.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import net.sqlcipher.database.SQLiteDatabase
import com.example.atm_osphere.model.User
import com.example.atm_osphere.utils.workers.UserDatabaseWorker
import net.sqlcipher.database.SQLiteOpenHelper

class UserDatabaseHelper(private val context: Context)  {

    private val appDatabaseHelper = AppDatabaseHelper(context)


    fun getUser(email: String, passphrase: String): User? {
        val db = appDatabaseHelper.getReadableDatabase(passphrase.toCharArray())
        var cursor: Cursor? = null
        try {
            cursor = db.query("users", null, "email=?", arrayOf(email), null, null, null)
            if (cursor != null && cursor.moveToFirst()) {
                // Retrieve the values, only if the column exists
                val permanentUserId = cursor.getColumnIndex("permanent_user_id").takeIf { it != -1 }?.let { cursor.getString(it) }
                val storedEmail = cursor.getColumnIndex("email").takeIf { it != -1 }?.let { cursor.getString(it) }
                val storedPassword = cursor.getColumnIndex("password").takeIf { it != -1 }?.let { cursor.getString(it) }
                val date = cursor.getColumnIndex("date").takeIf { it != -1 }?.let { cursor.getString(it) }

                // Return the User object if all fields are valid
                if (permanentUserId != null && storedEmail != null && storedPassword != null) {
                    return User(permanentUserId, storedEmail, storedPassword,  date ?: "")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor?.close()
            db.close()
        }
        return null
    }


    // Use WorkManager to insert user in the background
    fun insertUserInBackground(user: User, passphrase: String) {
        val inputData = Data.Builder()
            .putString("userId", user.permanentUserId)
            .putString("email", user.email)
            .putString("password", user.password)
            .putString("date", user.date) // Adding date to background data
            .putString("passphrase", passphrase)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<UserDatabaseWorker>()
            .setInputData(inputData)
            .build()

        WorkManager.getInstance(context).enqueue(workRequest)
    }




}
