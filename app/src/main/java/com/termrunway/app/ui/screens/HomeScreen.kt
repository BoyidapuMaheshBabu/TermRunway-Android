package com.termrunway.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import com.termrunway.app.ui.components.ModeSwitcher
import com.termrunway.app.ui.components.MetricCard
import com.termrunway.app.ui.components.QuickActionCard
import com.termrunway.app.ui.components.TransactionItem
import com.termrunway.app.ui.components.TransactionRow
import com.termrunway.app.util.addDays
import com.termrunway.app.util.formatDay
import com.termrunway.app.util.formatRupees
import com.termrunway.app.util.formatSignedRupees
import com.termrunway.app.util.startOfDay

@Composable
fun HomeScreen(
    data: AppData,
    contentPadding: PaddingValues,
    onSettings: () -> Unit,
    onModeChange: (Boolean) -> Unit,
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
    }.sortedByDescending { item -> item.expense?.dateMillis ?: item.income?.dateMillis ?: 0L }.take(5)

    val pulseDays = (0..6).map { addDays(today, -it) }.reversed()
    val pulse = pulseDays.map { day ->
        val dayTotals = dailyTotals(data.expenses, data.incomes, day)
        Pair(dayTotals.spentCents, dayTotals.incomeCents)
    }
    val maxPulse = pulse.flatMap { listOf(it.first, it.second) }.maxOrNull()?.coerceAtLeast(1L) ?: 1L

    LazyColumn(
        contentPadding = PaddingValues(
            start = 18.dp,
            end = 18.dp,
            top = contentPadding.calculateTopPadding() + 10.dp,
            bottom = contentPadding.calculateBottomPadding() + 24.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("TERM RUNWAY", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("Private • Offline", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onSettings) {
                    Icon(Icons.Outlined.Settings, contentDescription = "Settings")
                }
            }
        }

        item {
            ModeSwitcher(planSelected = false, onSelectPlan = onModeChange)
        }

        item {
            Column {
                Text("Good to see you, " + data.username, style = MaterialTheme.typography.headlineSmall)
                Text(formatDay(today), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("TODAY", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.weight(1f))
                        Text("ACTUAL", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(formatRupees(totals.spentCents), style = MaterialTheme.typography.displaySmall)
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
                            if (remaining >= 0L) {
                                formatRupees(remaining) + " left of daily reference"
                            } else {
                                formatRupees(-remaining) + " above daily reference"
                            },
                            color = if (remaining < 0L) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            "No daily limit is required in Tracking Mode.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(22.dp)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text("No pressure", fontWeight = FontWeight.SemiBold)
                            Text(
                                "Tracking records what actually happened. Nothing is blocked.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
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
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(Modifier.fillMaxWidth()) {
                        Column(Modifier.weight(1f)) {
                            Text("7-day pulse", fontWeight = FontWeight.SemiBold)
                            Text("Spending vs income", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().height(88.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        pulse.forEachIndexed { index, value ->
                            Row(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                val expenseHeight = (value.first.toDouble() / maxPulse.toDouble() * 70.0).toFloat().coerceAtLeast(if (value.first > 0) 6f else 2f)
                                val incomeHeight = (value.second.toDouble() / maxPulse.toDouble() * 70.0).toFloat().coerceAtLeast(if (value.second > 0) 6f else 2f)
                                Box(
                                    Modifier.width(7.dp).height(expenseHeight.dp).background(MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                                )
                                Spacer(Modifier.width(3.dp))
                                Box(
                                    Modifier.width(7.dp).height(incomeHeight.dp).background(MaterialTheme.colorScheme.secondary, RoundedCornerShape(8.dp))
                                )
                            }
                        }
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Expense", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        Text("Income", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                    }
                }
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
                    description = "Add your first expense or income to start building your real history."
                )
            }
        } else {
            items(recent, key = { it.id + if (it.isIncome) "-i" else "-e" }) { item ->
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
