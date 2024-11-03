package com.example.atm_osphere.view.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch
import androidx.compose.material3.Icon
import androidx.compose.ui.Alignment
import androidx.activity.compose.BackHandler // This import is essential
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import com.example.atm_osphere.viewmodels.auth.AuthViewModel
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.material3.SnackbarHost


@Composable
fun BasePage(
    navController: NavHostController,
    pageTitle: String,
    drawerContent: @Composable () -> Unit,
    content: @Composable (PaddingValues) -> Unit,
    sessionId: String?,
    puid: String?,
    authViewModel: AuthViewModel,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()  // Create a coroutine scope

    BackHandler {
        navController.navigate("home") {
            authViewModel.logout()
            popUpTo("home") { inclusive = true }

        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .padding(16.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    drawerContent()
                }
            }
        },
        content = {
            Scaffold(
                topBar = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.primary)
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = {
                            coroutineScope.launch {  // Launch a coroutine to open the drawer
                                drawerState.open()
                            }
                        }) {
                            Icon(Icons.Filled.Menu, contentDescription = "Menu", tint = Color.White)
                        }
                        Text(
                            text = pageTitle,
                            color = Color.White,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                },
                snackbarHost = { SnackbarHost(snackbarHostState) },
                content = content
            )
        }
    )
}
