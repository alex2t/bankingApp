package com.example.atm_osphere.utils.database

import android.content.Context
//import android.database.sqlite.SQLiteDatabase
//import android.database.sqlite.SQLiteOpenHelper
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SQLiteOpenHelper



class AppDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    init {
        SQLiteDatabase.loadLibs(context)  // Load SQLCipher libraries
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Create users table
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS users (
                permanent_user_id TEXT PRIMARY KEY,
                email TEXT UNIQUE,
                password TEXT,
                date TEXT CHECK(date LIKE '____-__-__') -- Date in yyyy-mm-dd format
            )
        """)

        // Create payees table with foreign key referencing users
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS Payee (
                payeeId INTEGER PRIMARY KEY AUTOINCREMENT,
                puid TEXT NOT NULL,
                name TEXT,
                country TEXT,
                iban TEXT UNIQUE,
                isDefault INTEGER DEFAULT 0,  -- New column for isDefault, defaulting to 0 (false)
                FOREIGN KEY (puid) REFERENCES users(permanent_user_id) ON DELETE CASCADE
            )
        """)

        // Create transactions table with foreign keys referencing users and payees
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS transactions (
                transaction_id INTEGER PRIMARY KEY AUTOINCREMENT,
                puid TEXT NOT NULL,
                payee_id INTEGER NOT NULL,
                amount REAL,
                date TEXT,
                transaction_type TEXT,
                FOREIGN KEY (puid) REFERENCES users(permanent_user_id) ON DELETE CASCADE,
                FOREIGN KEY (payee_id) REFERENCES payees(payee_id) ON DELETE CASCADE
            )
        """)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS transactions")
        db.execSQL("DROP TABLE IF EXISTS payees")
        db.execSQL("DROP TABLE IF EXISTS users")
        onCreate(db)
    }

    companion object {
        private const val DATABASE_NAME = "app_database.db"
        private const val DATABASE_VERSION = 1
    }
    fun getEncryptedWritableDatabase(passphrase: CharArray): SQLiteDatabase {
        return getWritableDatabase(passphrase)
    }
}
