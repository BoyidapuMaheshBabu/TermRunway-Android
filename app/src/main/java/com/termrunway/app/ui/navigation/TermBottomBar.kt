package com.termrunway.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun TermBottomBar(
    selectedTab: TermTab,
    onTabSelected: (TermTab) -> Unit
) {
    NavigationBar {
        NavigationBarItem(
            selected = selectedTab == TermTab.OVERVIEW,
            onClick = { onTabSelected(TermTab.OVERVIEW) },
            icon = { Icon(Icons.Outlined.Home, contentDescription = "Overview") },
            label = { Text("Overview") }
        )
        NavigationBarItem(
            selected = selectedTab == TermTab.PLAN,
            onClick = { onTabSelected(TermTab.PLAN) },
            icon = { Icon(Icons.Outlined.List, contentDescription = "Plan") },
            label = { Text("Plan") }
        )
        NavigationBarItem(
            selected = selectedTab == TermTab.RUNWAY,
            onClick = { onTabSelected(TermTab.RUNWAY) },
            icon = { Icon(Icons.Outlined.ShowChart, contentDescription = "Runway") },
            label = { Text("Runway") }
        )
        NavigationBarItem(
            selected = selectedTab == TermTab.INSIGHTS,
            onClick = { onTabSelected(TermTab.INSIGHTS) },
            icon = { Icon(Icons.Outlined.Info, contentDescription = "Insights") },
            label = { Text("Insights") }
        )
    }
}
