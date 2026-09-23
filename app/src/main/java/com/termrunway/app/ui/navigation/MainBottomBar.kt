package com.termrunway.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.List
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun MainBottomBar(selectedTab: MainTab, onTabSelected: (MainTab) -> Unit) {
    NavigationBar {
        NavigationBarItem(
            selected = selectedTab == MainTab.HOME,
            onClick = { onTabSelected(MainTab.HOME) },
            icon = { Icon(Icons.Outlined.Home, contentDescription = "Home") },
            label = { Text("Home") }
        )
        NavigationBarItem(
            selected = selectedTab == MainTab.TRACK,
            onClick = { onTabSelected(MainTab.TRACK) },
            icon = { Icon(Icons.Outlined.List, contentDescription = "Track") },
            label = { Text("Track") }
        )
        NavigationBarItem(
            selected = selectedTab == MainTab.INSIGHTS,
            onClick = { onTabSelected(MainTab.INSIGHTS) },
            icon = { Icon(Icons.Outlined.BarChart, contentDescription = "Insights") },
            label = { Text("Insights") }
        )
    }
}
