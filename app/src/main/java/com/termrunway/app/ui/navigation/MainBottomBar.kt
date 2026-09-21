package com.termrunway.app.ui.navigation

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.weight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun MainBottomBar(
    selectedTab: MainTab,
    onTabSelected: (MainTab) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(modifier = modifier) {
        NavigationBarItem(
            selected = selectedTab == MainTab.HOME,
            onClick = { onTabSelected(MainTab.HOME) },
            icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
            label = { Text("Home") }
        )

        NavigationBarItem(
            selected = selectedTab == MainTab.CHARTS,
            onClick = { onTabSelected(MainTab.CHARTS) },
            icon = { Icon(Icons.Filled.BarChart, contentDescription = "Charts") },
            label = { Text("Charts") }
        )

        Spacer(modifier = Modifier.weight(1f))

        NavigationBarItem(
            selected = selectedTab == MainTab.REPORTS,
            onClick = { onTabSelected(MainTab.REPORTS) },
            icon = { Icon(Icons.Filled.Description, contentDescription = "Reports") },
            label = { Text("Reports") }
        )

        NavigationBarItem(
            selected = selectedTab == MainTab.PROFILE,
            onClick = { onTabSelected(MainTab.PROFILE) },
            icon = { Icon(Icons.Filled.Person, contentDescription = "Profile") },
            label = { Text("Profile") }
        )
    }
}
