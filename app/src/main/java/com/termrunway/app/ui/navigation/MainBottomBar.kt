package com.termrunway.app.ui.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MainBottomBar(
    selectedTab: MainTab,
    onTabSelected: (MainTab) -> Unit,
    onAddTransaction: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
        NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
            NavigationBarItem(
                selected = selectedTab == MainTab.HOME,
                onClick = { onTabSelected(MainTab.HOME) },
                icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
                label = { androidx.compose.material3.Text("Home") },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
            NavigationBarItem(
                selected = selectedTab == MainTab.TRACK,
                onClick = { onTabSelected(MainTab.TRACK) },
                icon = { Icon(Icons.Filled.List, contentDescription = "Track") },
                label = { androidx.compose.material3.Text("Track") }
            )
            Spacer(Modifier.size(72.dp))
            NavigationBarItem(
                selected = selectedTab == MainTab.INSIGHTS,
                onClick = { onTabSelected(MainTab.INSIGHTS) },
                icon = { Icon(Icons.Filled.Info, contentDescription = "Insights") },
                label = { androidx.compose.material3.Text("Insights") }
            )
        }
        FloatingActionButton(
            onClick = onAddTransaction,
            modifier = Modifier.offset(y = (-28).dp).size(60.dp),
            shape = CircleShape,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Add transaction")
        }
    }
}
