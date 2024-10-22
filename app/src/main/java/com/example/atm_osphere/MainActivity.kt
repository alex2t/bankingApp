package com.example.atm_osphere

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.compose.rememberNavController
import com.example.atm_osphere.view.NavigationSetup
import java.util.UUID

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val navController = rememberNavController()
            val sessionId = remember { mutableStateOf(UUID.randomUUID().toString()) }

            // Set up navigation for both pre-login and post-login
            NavigationSetup(
                navController = navController,
                viewModelStoreOwner = this,
                sessionId = sessionId.value
            )
        }
    }
}
