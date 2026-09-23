package com.termrunway.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Paid
import androidx.compose.material.icons.outlined.Wallet
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.termrunway.app.data.Expense
import com.termrunway.app.data.Income
import com.termrunway.app.ui.daily.dailySpendingMessage
import com.termrunway.app.ui.daily.formatDateTime
import com.termrunway.app.ui.daily.formatLongDate
import com.termrunway.app.ui.daily.formatRupees
import com.termrunway.app.ui.daily.formatSignedRupees
import com.termrunway.app.ui.daily.greeting
import com.termrunway.app.ui.daily.recentAverage
import com.termrunway.app.ui.daily.startOfDay
import java.util.Locale

private data class HomeTransaction(
    val id: String,
    val title: String,
    val amountCents: Long,
    val dateMillis: Long,
    val income: Boolean
)

@Composable
fun HomeScreen(
    username: String,
    expenses: List<Expense>,
    incomes: List<Income>,
    dailyLimitCents: Long,
    onOpenTrack: () -> Unit,
    onOpenInsights: () -> Unit,
    onOpenSettings: () -> Unit,
    onAddExpense: () -> Unit,
    onAddIncome: () -> Unit,
    modifier: Modifier = Modifier
) {
    val today = startOfDay(System.currentTimeMillis())
    val spentToday = expenses.filter { startOfDay(it.dateMillis) == today }.sumOf { it.amountCents }
    val incomeToday = incomes.filter { startOfDay(it.dateMillis) == today }.sumOf { it.amountCents }
    val todayCount = expenses.count { startOfDay(it.dateMillis) == today } + incomes.count { startOfDay(it.dateMillis) == today }
    val average = recentAverage(expenses)
    val indication = dailySpendingMessage(spentToday, average)

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text("TermRunway", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                }
                IconButton(onClick = onOpenSettings) {
                    Icon(Icons.Outlined.Settings, contentDescription = "Settings")
                }
            }
        }

        item {
            Column {
                Text(greeting() + ", " + username + " 👋", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(formatLongDate(today), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        item {
            Text("DAILY TRACKER", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }

        item {
            ElevatedCard(
                onClick = onOpenTrack,
                modifier = Modifier.fillMaxWidth(),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(26.dp),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp)
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Wallet, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Text("Today", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.weight(1f))
                        Text(todayCount.toString() + " entries", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(formatRupees(spentToday), style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
                    Text("spent today", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        HomeMetric("Income today", formatRupees(incomeToday))
                        HomeMetric(
                            "Today’s balance",
                            if (dailyLimitCents > 0L) formatSignedRupees(dailyLimitCents - spentToday) else "No limit"
                        )
                    }
                    if (dailyLimitCents > 0L) {
                        val over = spentToday > dailyLimitCents
                        Text(
                            if (over) formatRupees(spentToday - dailyLimitCents) + " above today's limit"
                            else formatRupees(dailyLimitCents - spentToday) + " left of today's limit",
                            color = if (over) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
        }

        if (indication != null) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp)
                ) {
                    Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Info, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(10.dp))
                        Text(indication, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }

        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                QuickCard("Add expense", Icons.Outlined.Add, Modifier.weight(1f), onAddExpense)
                QuickCard("Add income", Icons.Outlined.Paid, Modifier.weight(1f), onAddIncome)
            }
        }

        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Recent activity", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                TextButton(onClick = onOpenTrack) { Text("See all") }
            }
        }

        val recent = buildList {
            expenses.forEach { add(HomeTransaction(it.id, it.category, it.amountCents, it.dateMillis, false)) }
            incomes.forEach { add(HomeTransaction(it.id, it.source, it.amountCents, it.dateMillis, true)) }
        }.sortedByDescending { it.dateMillis }.take(5)

        if (recent.isEmpty()) {
            item {
                Card(Modifier.fillMaxWidth(), shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp)) {
                    Column(Modifier.padding(20.dp)) {
                        Text("Nothing recorded yet", fontWeight = FontWeight.SemiBold)
                        Text("Add your first transaction to start tracking.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        } else {
            items(recent, key = { it.id }) { item ->
                CompactRow(item)
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                TextButton(onClick = onOpenTrack, modifier = Modifier.weight(1f)) { Text("Open Track") }
                TextButton(onClick = onOpenInsights, modifier = Modifier.weight(1f)) { Text("Open Insights") }
            }
        }
    }
}

@Composable
private fun HomeMetric(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun QuickCard(title: String, icon: ImageVector, modifier: Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier,
        onClick = onClick,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(8.dp))
            Text(title, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun CompactRow(item: HomeTransaction) {
    Card(Modifier.fillMaxWidth(), shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp)) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(if (item.income) Icons.Outlined.Paid else Icons.Outlined.Wallet, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(item.title, fontWeight = FontWeight.SemiBold)
                Text(formatDateTime(item.dateMillis), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text((if (item.income) "+" else "-") + formatRupees(item.amountCents), fontWeight = FontWeight.SemiBold)
        }
    }
}
