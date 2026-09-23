package com.termrunway.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.termrunway.app.data.AppData
import com.termrunway.app.domain.dailyTotals
import com.termrunway.app.ui.components.EditorRequest
import com.termrunway.app.ui.components.EmptyState
import com.termrunway.app.ui.components.MetricCard
import com.termrunway.app.ui.components.QuickActionCard
import com.termrunway.app.ui.components.TransactionItem
import com.termrunway.app.ui.components.TransactionRow
import com.termrunway.app.util.formatDay
import com.termrunway.app.util.formatRupees
import com.termrunway.app.util.formatSignedRupees
import com.termrunway.app.util.startOfDay

@Composable
fun HomeScreen(
    data: AppData,
    contentPadding: PaddingValues,
    onSettings: () -> Unit,
    onOpenTrack: () -> Unit,
    onOpenInsights: () -> Unit,
    onAddExpense: () -> Unit,
    onAddIncome: () -> Unit,
    onEdit: (EditorRequest) -> Unit
) {
    val today = startOfDay(System.currentTimeMillis())
    val totals = dailyTotals(data.expenses, data.incomes, today)
    val recent = buildList {
        data.expenses.forEach {
            add(TransactionItem(it.id, it.category, formatDay(it.dateMillis) + " · expense", it.amountCents, false, expense = it))
        }
        data.incomes.forEach {
            add(TransactionItem(it.id, it.source, formatDay(it.dateMillis) + " · income", it.amountCents, true, income = it))
        }
    }.sortedByDescending { item -> item.expense?.dateMillis ?: item.income?.dateMillis ?: 0L }.take(6)

    LazyColumn(
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = contentPadding.calculateTopPadding() + 12.dp,
            bottom = contentPadding.calculateBottomPadding() + 24.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("TermRunway", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                    Text("Daily money tracker", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = onSettings) {
                    Icon(Icons.Outlined.Settings, contentDescription = "Settings")
                }
            }
        }

        item {
            Column {
                Text("Hi, ${data.username} 👋", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(formatDay(today), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        item {
            ElevatedCard(
                onClick = onOpenTrack,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(26.dp),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("TODAY", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                    Text(formatRupees(totals.spentCents), style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
                    Text("spent today", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        MetricCard("Income", formatRupees(totals.incomeCents), Modifier.weight(1f))
                        MetricCard("Net", formatSignedRupees(totals.netCents), Modifier.weight(1f))
                    }
                    if (data.dailyLimitCents > 0L) {
                        val remaining = data.dailyLimitCents - totals.spentCents
                        val ratio = (totals.spentCents.toDouble() / data.dailyLimitCents.toDouble()).toFloat().coerceIn(0f, 1f)
                        LinearProgressIndicator(progress = { ratio }, modifier = Modifier.fillMaxWidth())
                        Text(
                            if (remaining >= 0L) "${formatRupees(remaining)} left of your daily limit"
                            else "${formatRupees(-remaining)} above your daily limit",
                            color = if (remaining < 0L) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelMedium
                        )
                    } else {
                        Text("Set a daily limit in Settings for an optional spending reference.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(18.dp)
            ) {
                Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.size(10.dp))
                    Text("Tracking records what actually happened. It never blocks a transaction.", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                QuickActionCard("Add expense", Modifier.weight(1f), onAddExpense)
                QuickActionCard("Add income", Modifier.weight(1f), onAddIncome)
            }
        }

        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Recent activity", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                TextButton(onClick = onOpenTrack) { Text("See all") }
            }
        }

        if (recent.isEmpty()) {
            item {
                EmptyState(
                    title = "Nothing recorded yet",
                    description = "Add your first expense or income to start building your real spending history."
                )
            }
        } else {
            items(recent, key = { item -> item.id + if (item.isIncome) "-i" else "-e" }) { item ->
                TransactionRow(item) {
                    onEdit(
                        if (item.isIncome) {
                            EditorRequest(income = item.income, defaultDateMillis = item.income!!.dateMillis)
                        } else {
                            EditorRequest(expense = item.expense, defaultDateMillis = item.expense!!.dateMillis)
                        }
                    )
                }
            }
        }

        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onOpenTrack, modifier = Modifier.weight(1f)) { Text("Open Track") }
                TextButton(onClick = onOpenInsights, modifier = Modifier.weight(1f)) { Text("Open Insights") }
            }
        }
    }
}
