package com.example.atm_osphere

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.rememberNavController
import androidx.work.WorkManager
import com.example.atm_osphere.view.navigation.NavigationSetup
import com.example.atm_osphere.viewmodels.auth.AuthViewModel
import com.example.atm_osphere.viewmodels.auth.AuthViewModelFactory
import com.example.atm_osphere.utils.database.UserDatabaseHelper
import com.example.atm_osphere.utils.database.PayeeDatabaseHelper
import com.example.atm_osphere.utils.database.TransactionDatabaseHelper
import com.example.atm_osphere.utils.openDatabase

import java.util.UUID

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Open the database for Debugging via Database Inspector

        openDatabase(this)

        setContent {
            val navController = rememberNavController()
            var sessionId = remember { mutableStateOf(UUID.randomUUID().toString()) }
            var puid = remember { mutableStateOf<String?>(null) }

            // Initialize AuthViewModel using ViewModelProvider
            val authViewModel = ViewModelProvider(
                this,
                AuthViewModelFactory(
                    UserDatabaseHelper(this),
                    PayeeDatabaseHelper(this),
                    TransactionDatabaseHelper(this),
                    WorkManager.getInstance(this)
                )
            ).get(AuthViewModel::class.java)
            // Set up navigation for both pre-login and post-login
            NavigationSetup(
                navController = navController,
                authViewModel = authViewModel,
                sessionId = sessionId.value,
            )
        }
    }
}