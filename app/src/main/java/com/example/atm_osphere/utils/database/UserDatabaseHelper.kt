package com.example.atm_osphere.utils.database

import android.content.Context
import android.database.Cursor
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import net.sqlcipher.database.SQLiteDatabase
import com.example.atm_osphere.model.User
import com.example.atm_osphere.utils.workers.UserDatabaseWorker
import net.sqlcipher.database.SQLiteOpenHelper

class UserDatabaseHelper(private val context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "user_database.db"  // Define your database name
        private const val DATABASE_VERSION = 1  // Define the version of the database
    }

    init {
        // This is necessary to load the SQLCipher libraries
        SQLiteDatabase.loadLibs(context)
    }

    override fun onCreate(db: SQLiteDatabase?) {
        // Create your tables here
        val createTableSQL = """
            CREATE TABLE users (
                permanent_user_id TEXT PRIMARY KEY,
                email TEXT UNIQUE,
                password TEXT
            )
        """.trimIndent()
        db?.execSQL(createTableSQL)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        // Handle database schema changes
        db?.execSQL("DROP TABLE IF EXISTS users")
        onCreate(db)
    }


    // Retrieve a user from the encrypted database by email
    fun getUser(email: String, passphrase: String): User? {
        val db = this.getReadableDatabase(passphrase.toCharArray()) // Access readable encrypted database
        var cursor: Cursor? = null
        try {
            // Query the database for the user with the given email
            cursor = db.query("users", null, "email=?", arrayOf(email), null, null, null)

            if (cursor != null && cursor.moveToFirst()) {
                val permanentUserIdIndex = cursor.getColumnIndex("permanent_user_id")
                val storedEmailIndex = cursor.getColumnIndex("email")
                val storedPasswordIndex = cursor.getColumnIndex("password")

                // Retrieve the values, only if the column exists
                val permanentUserId = if (permanentUserIdIndex != -1) cursor.getString(permanentUserIdIndex) else null
                val storedEmail = if (storedEmailIndex != -1) cursor.getString(storedEmailIndex) else null
                val storedPassword = if (storedPasswordIndex != -1) cursor.getString(storedPasswordIndex) else null

                // Return the User object if all fields are valid
                if (permanentUserId != null && storedEmail != null && storedPassword != null) {
                    return User(permanentUserId, storedEmail, storedPassword)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor?.close()  // Close cursor
            db.close()  // Close database
        }

        return null // Return null if user not found
    }

    // Use WorkManager to insert user in the background
    fun insertUserInBackground(user: User, passphrase: String) {
        val inputData = Data.Builder()
            .putString("userId", user.permanentUserId)
            .putString("email", user.email)
            .putString("password", user.password)
            .putString("passphrase", passphrase)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<UserDatabaseWorker>()
            .setInputData(inputData)
            .build()

        // Use the context passed to the constructor for WorkManager
        WorkManager.getInstance(context).enqueue(workRequest)
    }

    // Safely close the database if it's open
    fun closeDatabase(db: SQLiteDatabase?) {
        db?.let {
            if (it.isOpen) {
                it.close()
            }
        }
    }
}
