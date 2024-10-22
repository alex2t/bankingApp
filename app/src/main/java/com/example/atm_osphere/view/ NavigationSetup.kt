package com.example.atm_osphere.view

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModelStoreOwner
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.atm_osphere.viewmodels.AuthViewModel
import com.example.atm_osphere.viewmodels.AuthViewModelFactory
import com.example.atm_osphere.utils.UserDatabaseHelper
import com.example.atm_osphere.viewmodels.TransactionViewModel
import com.example.atm_osphere.utils.TransactionDatabaseHelper
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModelProvider




@Composable
fun NavigationSetup(
    navController: NavHostController,
    viewModelStoreOwner: ViewModelStoreOwner,
    sessionId: String
) {
    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomePage(navController)
        }

        composable(
            "signIn/{sessionId}",
            arguments = listOf(navArgument("sessionId") { type = NavType.StringType })
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getString("sessionId") ?: ""
            val context = navController.context
            val viewModel: AuthViewModel = viewModel(
                factory = AuthViewModelFactory(UserDatabaseHelper(context), "your-secure-passphrase")
            )
            SignInScreen(viewModel = viewModel, navController = navController, sessionId = sessionId)
        }

        composable(
            "createAccount/{sessionId}",
            arguments = listOf(navArgument("sessionId") { type = NavType.StringType })
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getString("sessionId") ?: ""
            val context = navController.context
            val viewModel: AuthViewModel = viewModel(
                factory = AuthViewModelFactory(UserDatabaseHelper(context), "your-secure-passphrase")
            )
            CreateAccountScreen(viewModel = viewModel, navController = navController, sessionId = sessionId)
        }


        // Post-login screens (with burger menu)
        composable(
            "mainpage/{sessionId}/{puid}",
            arguments = listOf(
                navArgument("sessionId") { type = NavType.StringType },
                navArgument("puid") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getString("sessionId") ?: ""
            val puid = backStackEntry.arguments?.getString("puid") ?: ""

            // Instantiate TransactionViewModel (or use ViewModelFactory if needed)
            val transactionDatabaseHelper = TransactionDatabaseHelper(context = navController.context) // Ensure you pass the context
            val passphrase = "your-secure-passphrase".toCharArray()  // Replace with your actual passphrase
            val transactionViewModel = TransactionViewModel(transactionDatabaseHelper, passphrase)

            // Pass the viewModel to MainPage
            MainPage(navController = navController, sessionId = sessionId, puid = puid, viewModel = transactionViewModel)
        }
        composable(
            "transaction/{sessionId}/{puid}",
            arguments = listOf(
                navArgument("sessionId") { type = NavType.StringType },
                navArgument("puid") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getString("sessionId") ?: ""
            val puid = backStackEntry.arguments?.getString("puid") ?: ""
            TransactionPage(navController = navController, sessionId = sessionId, puid = puid)
        }
        composable(
            "addpayee/{sessionId}/{puid}",
            arguments = listOf(
                navArgument("sessionId") { type = NavType.StringType },
                navArgument("puid") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getString("sessionId") ?: ""
            val puid = backStackEntry.arguments?.getString("puid") ?: ""
            AddPayee(navController = navController, sessionId = sessionId, puid = puid)
        }
    }
}
