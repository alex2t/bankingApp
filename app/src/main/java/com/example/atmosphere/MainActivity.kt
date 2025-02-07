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
import com.example.atmosphere.view.navigation.NavigationSetup
import com.example.atmosphere.viewmodels.auth.AuthViewModel
import com.google.firebase.FirebaseApp
import androidx.lifecycle.ProcessLifecycleOwner
import com.example.atmosphere.ui.theme.ATMosphereTheme
import com.example.atmosphere.utils.ApiProvider
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import android.Manifest


import java.util.UUID
class MainActivity : ComponentActivity() {
    private lateinit var authViewModel: AuthViewModel
    private lateinit var navController: NavHostController
    private var inactivityManager: InactivityManager? = null // Change from lateinit to nullable
    private lateinit var lifecycleObserver: InactivityLifecycleObserver

    private val requestPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // Callback for handling the permissions result
        permissions.entries.forEach { entry ->
            val permission = entry.key
            val isGranted = entry.value
            if (isGranted) {
                Toast.makeText(this, "$permission granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "$permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)

        setContent {
            ATMosphereTheme {
                navController = rememberNavController()
                val sessionId = remember { mutableStateOf(UUID.randomUUID().toString()) }
                val apiHelper = ApiProvider.apiHelper

                authViewModel = ViewModelProvider(
                    this,
                    ViewModelFactory(application)
                )[AuthViewModel::class.java]

                NavigationSetup(
                    navController = navController,
                    authViewModel = authViewModel,
                    sessionId = sessionId.value,
                    apiHelper = apiHelper
                )
            }
        }


        checkAndRequestPermissions()
    }

    private fun checkAndRequestPermissions() {
        val requiredPermissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        val deniedPermissions = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (deniedPermissions.isNotEmpty()) {
            requestPermissionsLauncher.launch(deniedPermissions.toTypedArray())
        } else {
            initializeInactivityManager() // Permissions already granted, initialize immediately
        }
    }
    private fun initializeInactivityManager() {
        inactivityManager = InactivityManager(
            context = this,
            logoutAction = { authViewModel.logout() },
            navigateToHome = {
                navController.navigate("home") {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                }
            }
        )

        lifecycleObserver = InactivityLifecycleObserver(inactivityManager!!, authViewModel)
        ProcessLifecycleOwner.get().lifecycle.addObserver(lifecycleObserver)
    }
    override fun onResume() {
        super.onResume()

        if (!::authViewModel.isInitialized || !::navController.isInitialized) {
            Log.e("MainActivity", "AuthViewModel or NavController not initialized yet")
            return
        }
        if (!authViewModel.loggedIn.value) {
            navController.navigate("home") {
                popUpTo(0) // Clear backstack
            }
        }
        inactivityManager?.resetTimer() // Use safe call to avoid crashes
    }


    override fun onUserInteraction() {
        super.onUserInteraction()
        inactivityManager?.resetTimer() // Use safe call
    }
    override fun onStop() {
        super.onStop()
        //  Ensure API resources are cleaned up when app is backgrounded
        ApiProvider.cleanup()
        //  Logout the user when app is placed in the background
        if (authViewModel.loggedIn.value) {
            authViewModel.logout()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        inactivityManager?.cleanup()
        ProcessLifecycleOwner.get().lifecycle.removeObserver(lifecycleObserver)
    }
}
