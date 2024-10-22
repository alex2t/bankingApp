package com.example.atm_osphere.view

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavHostController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import androidx.activity.compose.BackHandler
import androidx.compose.ui.graphics.Color


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BasePage(
    navController: NavHostController,
    pageTitle: String,
    drawerContent: @Composable () -> Unit,
    content: @Composable (PaddingValues) -> Unit,
    sessionId: String?,
    puid: String?
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Handle back button press
    BackHandler {
        navController.navigate("home") {
            popUpTo("mainpage") { inclusive = true }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            // Adjust the drawer height to match the number of menu options
            val menuItemsCount = 4 // Set the number of items in the menu (adjust this if needed)
            val itemHeight = 56.dp // Estimated height for each menu item
            val totalHeight = (menuItemsCount * itemHeight.value).dp + 32.dp // Total height based on items + padding

            Surface(
                modifier = Modifier
                    .wrapContentHeight()
                    .heightIn(max = totalHeight) // Set max height to match the menu content
                    .widthIn(max = 280.dp), // Max width of the drawer
                color = MaterialTheme.colorScheme.background // Non-transparent background
            ) {
                if (sessionId != null && puid != null) {
                    DrawerContent(navController = navController, sessionId = sessionId, puid = puid)
                } else {
                    Text(text = "Session ID or PUID is missing.", color = Color.Red)
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(pageTitle) },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch { drawerState.open() }
                        }) {
                            Icon(Icons.Filled.Menu, contentDescription = "Menu")
                        }
                    }
                )
            },
            content = { paddingValues ->
                Column(modifier = Modifier.fillMaxSize()) {
                    content(paddingValues)
                }
            }
        )
    }
}
