package com.example.atm_osphere.view.postLogin.payee

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.atm_osphere.viewmodels.payee.PayeeViewModel

@Composable
fun AddOrDeletePayeePage(
    sessionId: String,
    puid: String,
    payeeViewModel: PayeeViewModel,
    paddingValues: PaddingValues
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Add Payee, 1: Delete Payee
    val tabs = listOf("Add Payee", "Delete Payee")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)

    ) {

        TabRow(
            selectedTabIndex = selectedTab,
            modifier = Modifier.background(MaterialTheme.colorScheme.onPrimary),
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier
                        .tabIndicatorOffset(tabPositions[selectedTab])
                        .height(4.dp),
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    modifier = Modifier
                        .background(if (selectedTab == index) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onPrimary)
                        .padding(8.dp),
                    text = {
                        Text(
                            text = title,
                            color = if (selectedTab == index) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                )
            }
        }



        // Display the corresponding view based on the selected tab
        when (selectedTab) {
            0 -> AddPayee(
                //navController = navController,
                sessionId = sessionId,
                puid = puid,
                //authViewModel = authViewModel,
                payeeViewModel = payeeViewModel
            )
            1 -> DeletePayee(
               // navController = navController,
                sessionId = sessionId,
                puid = puid,
                payeeViewModel = payeeViewModel
            )
        }
    }
}
