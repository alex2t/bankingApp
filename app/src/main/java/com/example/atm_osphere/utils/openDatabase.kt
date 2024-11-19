package com.example.atm_osphere.utils

import android.content.Context
import android.util.Log
import com.example.atm_osphere.utils.database.AppDatabaseHelper


fun openDatabase(context: Context) {
    val dbHelper = AppDatabaseHelper(context)
    val db = dbHelper.writableDb // Open the encrypted database
    Log.d("DatabaseInspector", "Database is now open.")

    // Access each table to ensure they are open in Database Inspector
    try {
        db.rawQuery("SELECT * FROM users LIMIT 1", null).use {
            Log.d("DatabaseInspector", "Accessed 'users' table.")
        }

        db.rawQuery("SELECT * FROM Payee LIMIT 1", null).use {
            Log.d("DatabaseInspector", "Accessed 'Payee' table.")
        }

        db.rawQuery("SELECT * FROM transactions LIMIT 1", null).use {
            Log.d("DatabaseInspector", "Accessed 'transactions' table.")
        }

        Log.d("DatabaseInspector", "All tables are now accessible in Database Inspector.")
    } catch (e: Exception) {
        Log.e("DatabaseInspector", "Error accessing database tables: ${e.message}", e)
    }
}
