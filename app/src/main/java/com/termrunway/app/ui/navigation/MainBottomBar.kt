package com.termrunway.app.ui.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.termrunway.app.ui.theme.GrowthGreen
import com.termrunway.app.ui.theme.PrimaryBlue

@Composable
fun MainBottomBar(
    selectedTab: MainTab,
    onTabSelected: (MainTab) -> Unit,
    onAddTransaction: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopCenter
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp
        ) {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NavItem(
                        tab = MainTab.HOME,
                        selectedTab = selectedTab,
                        onTabSelected = onTabSelected,
                        imageVector = Icons.Filled.Home,
                        label = "Home",
                        accent = PrimaryBlue
                    )
                    NavItem(
                        tab = MainTab.PLAN,
                        selectedTab = selectedTab,
                        onTabSelected = onTabSelected,
                        imageVector = Icons.Filled.DateRange,
                        label = "Plan",
                        accent = PrimaryBlue
                    )

                    Spacer(modifier = Modifier.width(76.dp))

                    NavItem(
                        tab = MainTab.ACTIVITY,
                        selectedTab = selectedTab,
                        onTabSelected = onTabSelected,
                        imageVector = Icons.Filled.List,
                        label = "Activity",
                        accent = GrowthGreen
                    )
                    NavItem(
                        tab = MainTab.INSIGHTS,
                        selectedTab = selectedTab,
                        onTabSelected = onTabSelected,
                        imageVector = Icons.Filled.Info,
                        label = "Insights",
                        accent = Color(0xFF7C3AED)
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = onAddTransaction,
            modifier = Modifier
                .offset(y = (-30).dp)
                .size(62.dp)
                .zIndex(2f),
            shape = CircleShape,
            containerColor = PrimaryBlue,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "Add transaction"
            )
        }
    }
}

@Composable
private fun RowScope.NavItem(
    tab: MainTab,
    selectedTab: MainTab,
    onTabSelected: (MainTab) -> Unit,
    imageVector: ImageVector,
    label: String,
    accent: Color
) {
    NavigationBarItem(
        selected = selectedTab == tab,
        onClick = { onTabSelected(tab) },
        icon = {
            Icon(
                imageVector = imageVector,
                contentDescription = label
            )
        },
        label = { Text(label) },
        modifier = Modifier.weight(1f),
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = accent,
            selectedTextColor = accent,
            indicatorColor = accent.copy(alpha = 0.13f),
            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
}
