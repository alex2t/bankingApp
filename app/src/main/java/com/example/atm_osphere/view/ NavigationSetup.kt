package com.example.atm_osphere.view

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModelStoreOwner
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.compose.runtime.collectAsState
import com.example.atm_osphere.viewmodels.AuthViewModel
import com.example.atm_osphere.viewmodels.AuthViewModelFactory
import com.example.atm_osphere.viewmodels.TransactionViewModelFactory
import com.example.atm_osphere.utils.UserDatabaseHelper
import com.example.atm_osphere.viewmodels.TransactionViewModel
import com.example.atm_osphere.utils.TransactionDatabaseHelper
import androidx.lifecycle.viewmodel.compose.viewModel



@Composable
fun NavigationSetup(
    navController: NavHostController,
    sessionId: String, // Passed from MainActivity
    authViewModel: AuthViewModel
) {
    val context = navController.context


    val loggedInState = authViewModel.loggedIn.collectAsState(initial = false)
    val loggedIn = loggedInState.value
    // Log to check if the loggedIn state is correctly updated
    Log.d("NavigationSetup", "loggedIn state: $loggedIn")


    val transactionViewModel: TransactionViewModel = viewModel(
        factory = TransactionViewModelFactory(
            TransactionDatabaseHelper(context),
            "your-secure-passphrase".toCharArray()
        )
    )

    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomePage(navController)
        }

        composable("signIn/{sessionId}") { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getString("sessionId") ?: ""
            SignInScreen(viewModel = authViewModel, navController = navController, sessionId = sessionId)
        }

        composable("createAccount/{sessionId}") { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getString("sessionId") ?: ""
            CreateAccountScreen(viewModel = authViewModel, navController = navController, sessionId = sessionId)
        }

        composable("mainpage/{sessionId}/{puid}") { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getString("sessionId") ?: ""
            val puid = backStackEntry.arguments?.getString("puid") ?: ""
            if (loggedIn) {
                MainPage(
                    navController = navController,
                    sessionId = sessionId,
                    puid = puid,
                    authViewModel = authViewModel,
                    viewModel = transactionViewModel
                )
            } else {
                // If puid is null or blank, navigate to the homepage first
                Log.d("NavigationSetup", "User not logged in, redirecting to home.")
                //navController.navigate("home")
            }
        }

        composable("transaction/{sessionId}/{puid}") { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getString("sessionId") ?: ""
            val puid = backStackEntry.arguments?.getString("puid") ?: ""
            TransactionPage(
                navController = navController,
                sessionId = sessionId,
                puid = puid,
                authViewModel = authViewModel
            )
        }

        composable("addpayee/{sessionId}/{puid}") { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getString("sessionId") ?: ""
            val puid = backStackEntry.arguments?.getString("puid") ?: ""
            AddPayee(
                navController = navController,
                sessionId = sessionId,
                puid = puid,
                authViewModel = authViewModel
            )
        }
    }
}
