package com.example.atmosphere

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.work.WorkManager
import com.example.atmosphere.view.navigation.NavigationSetup
import com.example.atmosphere.viewmodels.auth.AuthViewModel
import com.example.atmosphere.viewmodels.auth.AuthViewModelFactory
import com.example.atmosphere.utils.database.UserDatabaseHelper
import com.example.atmosphere.utils.database.PayeeDatabaseHelper
import com.example.atmosphere.utils.database.TransactionDatabaseHelper
import com.example.atmosphere.utils.openDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import com.google.firebase.FirebaseApp
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.example.atmosphere.ui.theme.ATMosphereTheme
import com.example.atmosphere.utils.ApiProvider

import kotlinx.coroutines.delay
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.Manifest
import androidx.appcompat.app.AppCompatActivity



import java.util.UUID

// to do improve MainActivity split feature in seperate file for readibility

class MainActivity : ComponentActivity() {
    private lateinit var authViewModel: AuthViewModel
    private lateinit var navController: NavHostController
    private lateinit var handler: Handler
    private lateinit var inactivityRunnable: Runnable



    private val inactivityTimeout: Long = 5 * 60 * 1000 // 5 minutes in millisecond


    private val requestPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // Callback for handling the permissions result
        permissions.entries.forEach { entry ->
            val permission = entry.key
            val isGranted = entry.value
            if (isGranted) {
                Toast.makeText(this, "$permission granted", Toast.LENGTH_SHORT).show()
                // You can handle granted permissions here
            } else {
                Toast.makeText(this, "$permission denied", Toast.LENGTH_SHORT).show()
                // Handle denied permissions here
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)

        // Open the database for Debugging via Database Inspector
        openDatabase(this)

        handler = Handler(Looper.getMainLooper())
        val lifecycleOwner = this

        inactivityRunnable = Runnable {
            CoroutineScope(Dispatchers.Main).launch {
                if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                    authViewModel.logout()
                    delay(100)
                    navController.navigate("home") {
                        popUpTo(navController.graph.startDestinationId) {
                            inclusive = true
                        }
                    }
                } else {
                    Log.e("NavigationError", "Cannot navigate as the lifecycle is not in a valid state.")
                }
            }
        }


        // Start observing lifecycle changes
        setupLifecycleObserver()
        checkAndRequestPermissions()
        //to do create a toggle light or dark theme with saving  option in datastore
        setContent {
            ATMosphereTheme {
                navController = rememberNavController()
                val sessionId = remember { mutableStateOf(UUID.randomUUID().toString()) }
                val apiHelper = ApiProvider.apiHelper
                authViewModel = ViewModelProvider(
                    this,
                    AuthViewModelFactory(
                        UserDatabaseHelper(this),
                        PayeeDatabaseHelper(this),
                        TransactionDatabaseHelper(this),
                        WorkManager.getInstance(this),
                        apiHelper
                    )
                )[AuthViewModel::class.java]
                NavigationSetup(
                    navController = navController,
                    authViewModel = authViewModel,
                    sessionId = sessionId.value,
                    apiHelper = apiHelper
                )
            }
        }
    }
    private fun checkAndRequestPermissions() {
        val requiredPermissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        val deniedPermissions = requiredPermissions.filter { permission ->
            ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED
        }

        if (deniedPermissions.isNotEmpty()) {
            // Request permissions using Activity Result API
            requestPermissionsLauncher.launch(deniedPermissions.toTypedArray())
        }
    }


    private fun resetInactivityTimer() {
        handler.removeCallbacks(inactivityRunnable) // Cancel previous timer
        handler.postDelayed(inactivityRunnable, inactivityTimeout) // Schedule new timer
    }

    private fun cancelInactivityTimer() {
        handler.removeCallbacks(inactivityRunnable) // Stop the timer
    }

    private fun setupLifecycleObserver() {
        val lifecycleObserver = object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                // App is back in the foreground, restart the inactivity timer
                resetInactivityTimer()
            }

            override fun onPause(owner: LifecycleOwner) {
                // Cancel the inactivity timer when the app is not in the foreground
                cancelInactivityTimer()
            }
        }

        ProcessLifecycleOwner.get().lifecycle.addObserver(lifecycleObserver)
    }

    override fun onStop() {
        super.onStop()
        // Call the logout function from AuthViewModel
        ApiProvider.cleanup()
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
        resetInactivityTimer()
    }

    //onUserInteraction() is triggered by any user input in the activity
    override fun onUserInteraction() {
        super.onUserInteraction()
        // Reset the inactivity timer on user interaction
        resetInactivityTimer()
    }
    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(inactivityRunnable) // Cleanup
        ApiProvider.cleanup() // releases resources used by OkHttp
    }
}