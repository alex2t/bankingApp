package com.example.atm_osphere.view
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding

import androidx.compose.runtime.Composable

import androidx.navigation.NavHostController

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column

import androidx.compose.foundation.layout.fillMaxSize

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp


@Composable
fun TransactionPage(navController: NavHostController, sessionId: String, puid: String) {
    BasePage(
        navController = navController,
        pageTitle = "Transaction Page",
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
                // Transaction page content
                Text(text = "Transaction Page Content")

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


