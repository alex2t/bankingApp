package com.example.atm_osphere.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Arrangement
import com.example.atm_osphere.viewmodels.AuthViewModel



@Composable
fun AddPayee(
    navController: NavHostController,
    sessionId: String,
    puid: String,
    authViewModel: AuthViewModel
) {
    BasePage(
        navController = navController,
        pageTitle = "Add Payee Page",
        drawerContent = {
            DrawerContent(navController = navController, sessionId = sessionId, puid = puid, authViewModel = authViewModel)
        },
        content = { paddingValues ->
            Box(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize(),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Add payee page content
                    Text(text = "Add Payee Page Content", modifier = Modifier.padding(16.dp))

                    // SessionId and PUID at the bottom
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(text = "Session ID: $sessionId")
                        Text(text = "PUID: $puid")
                    }
                }
            }
        },
        sessionId = sessionId,
        puid = puid,
        authViewModel = authViewModel
    )
}
