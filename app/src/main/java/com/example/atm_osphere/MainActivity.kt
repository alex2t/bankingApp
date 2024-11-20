package com.example.atm_osphere

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.work.WorkManager
import com.example.atm_osphere.view.navigation.NavigationSetup
import com.example.atm_osphere.viewmodels.auth.AuthViewModel
import com.example.atm_osphere.viewmodels.auth.AuthViewModelFactory
import com.example.atm_osphere.utils.database.UserDatabaseHelper
import com.example.atm_osphere.utils.database.PayeeDatabaseHelper
import com.example.atm_osphere.utils.database.TransactionDatabaseHelper
import com.example.atm_osphere.utils.openDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import java.util.UUID

class MainActivity : ComponentActivity() {
    private lateinit var authViewModel: AuthViewModel
    private lateinit var navController: NavHostController
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Open the database for Debugging via Database Inspector

        openDatabase(this)

        setContent {
             navController = rememberNavController()
            val sessionId = remember { mutableStateOf(UUID.randomUUID().toString()) }
            var puid = remember { mutableStateOf<String?>(null) }

            // Initialize AuthViewModel using ViewModelProvider
             authViewModel = ViewModelProvider(
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
    override fun onStop() {
        super.onStop()
        // Call the logout function from AuthViewModel
        CoroutineScope(Dispatchers.Main).launch {
            authViewModel.logout()
        }
    }
    override fun onResume() {
        super.onResume()

        // Check login state and navigate if needed
        if (!this::authViewModel.isInitialized) return
        if (!authViewModel.loggedIn.value) {
            navController.navigate("home") {
                popUpTo(0) // Clear backstack
            }
        }
    }
}