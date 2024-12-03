package com.example.atm_osphere.view.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.compose.runtime.collectAsState
import com.example.atm_osphere.viewmodels.auth.AuthViewModel
import com.example.atm_osphere.viewmodels.transaction.TransactionViewModelFactory
import com.example.atm_osphere.viewmodels.transaction.TransactionViewModel
import com.example.atm_osphere.viewmodels.payee.PayeeViewModel
import com.example.atm_osphere.viewmodels.payee.PayeeViewModelFactory
import com.example.atm_osphere.utils.database.TransactionDatabaseHelper
import com.example.atm_osphere.utils.api.ApiHelper
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.atm_osphere.utils.api.ApiFactory
import com.example.atm_osphere.view.postLogin.AddPayee
import com.example.atm_osphere.view.auth.CreateAccountScreen
import com.example.atm_osphere.view.auth.HomePage
import com.example.atm_osphere.view.postLogin.MainPage
import com.example.atm_osphere.view.auth.SignInScreen
import com.example.atm_osphere.view.postLogin.PayPayee


@Composable
fun NavigationSetup(
    navController: NavHostController,
    sessionId: String, // The sessionId is passed to routes as part of the NavHost configuration in placeHolder and not
    authViewModel: AuthViewModel,
    apiHelper: ApiHelper
) {
    val context = navController.context

    val loggedInState = authViewModel.loggedIn.collectAsState(initial = false)
    val loggedIn = loggedInState.value
    // Log to check if the loggedIn state is correctly updated
    Log.d("NavigationSetup", "loggedIn state: $loggedIn")

    val transactionViewModel: TransactionViewModel = viewModel(
        factory = TransactionViewModelFactory(
            databaseHelper = TransactionDatabaseHelper(context),
            context = context
        )
    )

    val payeeViewModel: PayeeViewModel = viewModel(
        factory = PayeeViewModelFactory(
            context = context,
            apiHelper = apiHelper
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
            val navSessionId = backStackEntry.arguments?.getString("sessionId") ?: ""
            SignInScreen(
                viewModel = authViewModel,
                navController = navController,
                sessionId = navSessionId
            )
        }

        composable("createAccount/{sessionId}") { backStackEntry ->
            val navSessionId = backStackEntry.arguments?.getString("sessionId") ?: ""
            CreateAccountScreen(
                viewModel = authViewModel,
                navController = navController,
                sessionId = navSessionId
            )
        }

        composable("mainpage/{sessionId}/{puid}") { backStackEntry ->
            val navSessionId = backStackEntry.arguments?.getString("sessionId") ?: ""
            val puid = backStackEntry.arguments?.getString("puid") ?: ""
            if (loggedIn) {
                MainPage(
                    navController = navController,
                    sessionId = navSessionId,
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

        composable("payPayee/{sessionId}/{puid}") { backStackEntry ->
            val navSessionId = backStackEntry.arguments?.getString("sessionId") ?: ""
            val puid = backStackEntry.arguments?.getString("puid") ?: ""
            PayPayee(
                navController = navController,
                sessionId = navSessionId,
                puid = puid,
                authViewModel = authViewModel,
                payeeViewModel = payeeViewModel,
                transactionviewModel = transactionViewModel
            )
        }

        composable("addpayee/{sessionId}/{puid}") { backStackEntry ->
            val navSessionId = backStackEntry.arguments?.getString("sessionId") ?: ""
            val puid = backStackEntry.arguments?.getString("puid") ?: ""
            AddPayee(
                navController = navController,
                sessionId = navSessionId,
                puid = puid,
                authViewModel = authViewModel,
                payeeViewModel = payeeViewModel
            )
        }
    }
}
