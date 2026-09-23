package com.termrunway.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.termrunway.app.data.AppData
import com.termrunway.app.domain.trackingTotals
import com.termrunway.app.ui.components.EmptyState
import com.termrunway.app.ui.components.MetricCard
import com.termrunway.app.util.addDays
import com.termrunway.app.util.formatDay
import com.termrunway.app.util.formatRupees
import com.termrunway.app.util.startOfDay

@Composable
fun InsightsScreen(data: AppData, contentPadding: PaddingValues, onSettings: () -> Unit) {
    var selectedDays by remember { mutableIntStateOf(7) }
    val today = startOfDay(System.currentTimeMillis())
    val start = addDays(today, -(selectedDays - 1))
    val totals = trackingTotals(data.expenses, data.incomes, start, today)

    androidx.compose.foundation.lazy.LazyColumn(
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = contentPadding.calculateTopPadding() + 8.dp,
            bottom = contentPadding.calculateBottomPadding() + 24.dp
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Insights", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text("Understand your real spending", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = onSettings) {
                    Icon(Icons.Outlined.Settings, contentDescription = "Settings")
                }
            }
        }

        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(7, 30, 90).forEach { days ->
                    FilterChip(
                        selected = selectedDays == days,
                        onClick = { selectedDays = days },
                        label = { Text("${days}d") }
                    )
                }
            }
        }

        if (totals.transactionCount == 0) {
            item {
                EmptyState(
                    title = "Not enough data yet",
                    description = "Record a few transactions and this section will turn them into useful spending patterns."
                )
            }
        } else {
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MetricCard("Spent", formatRupees(totals.totalSpentCents), Modifier.weight(1f))
                    MetricCard("Income", formatRupees(totals.totalIncomeCents), Modifier.weight(1f))
                    MetricCard("Net", formatRupees(totals.netCents), Modifier.weight(1f))
                }
            }

            item {
                Card(shape = RoundedCornerShape(20.dp)) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Activity", fontWeight = FontWeight.SemiBold)
                        Text(
                            totals.activeDays.toString() + " active spending days · average " +
                                formatRupees(totals.averageActiveDaySpentCents) + " per active day",
                            modifier = Modifier.padding(top = 4.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (totals.highestSpendingDayMillis != null) {
                            Text(
                                "Highest spending day: " + formatDay(totals.highestSpendingDayMillis) +
                                    " · " + formatRupees(totals.highestSpendingDayCents),
                                modifier = Modifier.padding(top = 10.dp),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            item {
                Card(shape = RoundedCornerShape(20.dp)) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Spending by category", fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(12.dp))
                        val max = totals.categoryTotals.maxOfOrNull { it.second } ?: 0L
                        totals.categoryTotals.take(8).forEach { (category, amount) ->
                            Column(Modifier.padding(bottom = 12.dp)) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(category, style = MaterialTheme.typography.bodyMedium)
                                    Text(formatRupees(amount), fontWeight = FontWeight.SemiBold)
                                }
                                LinearProgressIndicator(
                                    progress = { if (max == 0L) 0f else (amount.toDouble() / max.toDouble()).toFloat() },
                                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
