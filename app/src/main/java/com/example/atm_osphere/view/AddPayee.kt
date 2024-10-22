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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.fillMaxSize


@OptIn(ExperimentalMaterial3Api::class)  // Allow experimental Material3 APIs
@Composable
fun AddPayee(navController: NavHostController, sessionId: String, puid: String) {
    BasePage(
        navController = navController,
        pageTitle = "Add Payee Page",
        drawerContent = {
            DrawerContent(navController = navController, sessionId = sessionId, puid = puid)
        },
        content = { paddingValues ->
            Box(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize(),
                contentAlignment = Alignment.TopCenter
            ) {
                // Add payee page content
                Text(text = "Add Payee Page Content")

                // Display sessionId and puid at the bottom left
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.BottomStart
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "Session ID: $sessionId")
                        Text(text = "PUID: $puid")
                    }
                }
            }
        },
        sessionId = sessionId,
        puid = puid
    )
}

